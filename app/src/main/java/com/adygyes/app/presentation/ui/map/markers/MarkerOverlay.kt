package com.adygyes.app.presentation.ui.map.markers

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import android.view.Choreographer
import com.adygyes.app.domain.model.Attraction
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.mapview.MapView
import timber.log.Timber
import kotlin.math.roundToInt

/**
 * Overlay container that positions circular markers on top of the map
 */
@Composable
fun MarkerOverlay(
    mapView: MapView?,
    attractions: List<Attraction>,
    selectedAttraction: Attraction?,
    onMarkerClick: (Attraction) -> Unit,
    modifier: Modifier = Modifier,
    enableClustering: Boolean = false,
    clusteringThreshold: Int = 5,
    animationDuration: Int = 300,
    transparentMode: Boolean = false // For dual-layer system
) {
    val density = LocalDensity.current
    val view = LocalView.current
    
    // Track camera position changes WITHOUT delay for tight binding
    var cameraVersion by remember { mutableStateOf(0) }
    
    // High-frequency update system for ultra-smooth marker binding
    var frameUpdateCounter by remember { mutableStateOf(0) }
    
    // Use derivedStateOf for instant recalculation on camera change
    val markerPositions by remember(mapView, attractions) {
        derivedStateOf {
            if (mapView == null || attractions.isEmpty()) {
                emptyMap()
            } else {
                // Force recalculation on every frame for ultra-smooth binding
                cameraVersion // Camera-based updates
                frameUpdateCounter // Frame-based updates for maximum smoothness
                attractions.associateWith { attraction ->
                    MapCoordinateConverter.geoToScreen(
                        mapView = mapView,
                        latitude = attraction.location.latitude,
                        longitude = attraction.location.longitude
                    )
                }.filterValues { it != null }
            }
        }
    }
    
    // Force initial update when attractions are loaded
    LaunchedEffect(attractions, mapView) {
        if (mapView != null && attractions.isNotEmpty()) {
            cameraVersion++ // Trigger marker position recalculation
            Timber.d("🔄 Force marker update: ${attractions.size} attractions loaded")
        }
    }
    
    // Ultra-smooth 60 FPS marker updates using Choreographer
    DisposableEffect(mapView) {
        var isUpdating = false
        
        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!isUpdating && mapView != null) {
                    isUpdating = true
                    // Trigger marker position recalculation on every frame
                    frameUpdateCounter++
                    if (frameUpdateCounter % 2 == 0) { // Update every 2nd frame (30 FPS) for performance
                        cameraVersion++
                    }
                    isUpdating = false
                }
                // Schedule next frame
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        
        if (mapView != null) {
            Choreographer.getInstance().postFrameCallback(frameCallback)
            Timber.d("🎬 Started 60 FPS marker updates")
        }
        
        onDispose {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
            Timber.d("🎬 Stopped 60 FPS marker updates")
        }
    }
    
    // Listen for camera changes - INSTANT update, no delay
    DisposableEffect(mapView) {
        val cameraListener = object : CameraListener {
            override fun onCameraPositionChanged(
                map: com.yandex.mapkit.map.Map,
                cameraPosition: com.yandex.mapkit.map.CameraPosition,
                cameraUpdateReason: com.yandex.mapkit.map.CameraUpdateReason,
                finished: Boolean
            ) {
                // INSTANT update - no coroutine, no delay
                cameraVersion++
            }
        }
        
        mapView?.map?.addCameraListener(cameraListener)
        
        onDispose {
            mapView?.map?.removeCameraListener(cameraListener)
        }
    }
    
    // Render marker overlay - DIRECT rendering without sorting for speed
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Direct iteration over positions for instant updates
        markerPositions.forEach { (attraction, screenPosition) ->
            if (screenPosition != null) {
                key(attraction.id) {
                    // Calculate offset to center marker on position
                    val markerSizePx = with(density) { 52.dp.toPx() }
                    val hitAreaSizePx = if (transparentMode) {
                        with(density) { (52.dp * 1.1f).toPx() } // Slightly larger hit area in transparent mode
                    } else {
                        markerSizePx
                    }
                    val offsetX = (screenPosition.x - hitAreaSizePx / 2).roundToInt()
                    val offsetY = (screenPosition.y - hitAreaSizePx / 2).roundToInt() // Center vertically on marker
                    
                    // Transparent markers positioned (debug logs removed for performance)
                    
                    // NO ANIMATION for instant binding to coordinates
                    // Direct positioning for tight map binding
                    Box(
                        modifier = Modifier.offset { IntOffset(offsetX, offsetY) }
                    ) {
                        if (transparentMode) {
                            // Direct rendering without AnimatedVisibility for transparent mode
                            CircularImageMarker(
                                attraction = attraction,
                                screenPosition = screenPosition,
                                onClick = {
                                    Timber.d("✅ Transparent marker clicked: ${attraction.name}")
                                    onMarkerClick(attraction)
                                },
                                isSelected = attraction.id == selectedAttraction?.id,
                                animateAppearance = false,
                                transparentMode = true
                            )
                        } else {
                            // Animated visibility for visual mode
                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                CircularImageMarker(
                                    attraction = attraction,
                                    screenPosition = screenPosition,
                                    onClick = {
                                        Timber.d("✅ Visual marker clicked: ${attraction.name}")
                                        onMarkerClick(attraction)
                                    },
                                    isSelected = attraction.id == selectedAttraction?.id,
                                    animateAppearance = true,
                                    transparentMode = false
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Log marker overlay statistics with detailed info
        LaunchedEffect(markerPositions.size, attractions.size, transparentMode) {
            val mode = if (transparentMode) "TRANSPARENT" else "VISUAL"
            Timber.d("📍 MarkerOverlay ($mode): ${markerPositions.size}/${attractions.size} markers positioned (mapView: ${mapView != null})")
        }
    }
}

/**
 * Extension function to animate IntOffset changes smoothly
 */
@Composable
fun animateIntOffsetAsState(
    targetValue: IntOffset,
    animationSpec: AnimationSpec<IntOffset> = spring(),
    label: String = "IntOffsetAnimation",
    finishedListener: ((IntOffset) -> Unit)? = null
): State<IntOffset> {
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = IntOffset.VectorConverter,
        animationSpec = animationSpec,
        label = label,
        finishedListener = finishedListener
    )
}
