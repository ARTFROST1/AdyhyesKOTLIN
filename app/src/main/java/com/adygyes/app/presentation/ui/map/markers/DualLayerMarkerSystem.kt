package com.adygyes.app.presentation.ui.map.markers

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.data.local.cache.ImageCacheManager
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.map.PlacemarkMapObject
import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.awaitCancellation

/**
 * Dual-layer marker system that combines:
 * 1. Native MapKit markers for perfect visual synchronization
 * 2. Transparent Compose overlays for reliable click detection
 * 
 * This provides the best of both worlds:
 * - Native markers that are perfectly "glued" to map coordinates
 * - Compose click detection that works 100% of the time
 */
@Composable
fun DualLayerMarkerSystem(
    mapView: MapView?,
    attractions: List<Attraction>,
    selectedAttraction: Attraction?,
    imageCacheManager: ImageCacheManager,
    onMarkerClick: (Attraction) -> Unit,
    modifier: Modifier = Modifier,
    composeVisualMode: Boolean = false, // When true, render visual markers with Compose and skip native markers
    enableAppearAnimation: Boolean = true, // Enable smooth appearance animation for initial markers
    userLocation: Pair<Double, Double>? = null, // Местоположение пользователя
    showUserLocationMarker: Boolean = false // Показывать ли маркер пользователя
) {
    // IMPORTANT: Box to ensure proper layering
    Box(modifier = modifier.fillMaxSize()) {
        if (!composeVisualMode) {
            // Layer 1: Native visual markers (bottom layer)
            // Persist provider across navigation via registry to avoid re-creating markers
            val visualMarkerProvider = remember(mapView) {
                mapView?.let { mv ->
                    val provider = VisualMarkerRegistry.getOrCreate(mv, imageCacheManager)
                    Timber.d("🎯 DualLayerMarkerSystem: Got VisualMarkerProvider for MapView: ${mv.hashCode()}")
                    provider
                }
            }

            // Configure animation settings
            LaunchedEffect(enableAppearAnimation) {
                visualMarkerProvider?.setAppearAnimation(enableAppearAnimation)
            }

            // Handle markers - either use preloaded or create new ones
            LaunchedEffect(mapView, attractions) {
                if (mapView != null && visualMarkerProvider != null) {
                    val currentIds = VisualMarkerRegistry.getLastIds(mapView)
                    val newIds = attractions.map { it.id }.toSet()
                    
                    Timber.d("🔍 MARKER UPDATE: Current markers: ${currentIds.size}, New attractions: ${newIds.size}")
                    
                    when {
                        // Case 1: Check if we have preloaded markers that match
                        visualMarkerProvider.hasPreloadedMarkers() && currentIds == newIds -> {
                            Timber.d("📍 Using preloaded markers: ${currentIds.size}")
                            // Markers are already preloaded and match - do nothing
                        }
                        // Case 2: No markers exist, need to create them
                        currentIds.isEmpty() && newIds.isNotEmpty() -> {
                            Timber.d("📍 Creating initial markers (no preload) - ${newIds.size} attractions")
                            visualMarkerProvider.setAppearAnimation(enableAppearAnimation)
                            visualMarkerProvider.addVisualMarkers(attractions)
                            VisualMarkerRegistry.setLastIds(mapView, newIds)
                        }
                        // Case 3: Filtering - update existing markers
                        currentIds != newIds && currentIds.isNotEmpty() -> {
                            Timber.d("📍 Filtering markers: ${currentIds.size} → ${newIds.size}")
                            visualMarkerProvider.setAppearAnimation(false)
                            visualMarkerProvider.updateVisualMarkers(attractions)
                            VisualMarkerRegistry.setLastIds(mapView, newIds)
                        }
                        // Case 4: Force recreate if no preloaded markers but we have attractions (after version update)
                        !visualMarkerProvider.hasPreloadedMarkers() && newIds.isNotEmpty() && currentIds.isEmpty() -> {
                            Timber.d("📍 Force creating markers after version update: ${newIds.size} attractions")
                            visualMarkerProvider.setAppearAnimation(enableAppearAnimation)
                            visualMarkerProvider.addVisualMarkers(attractions)
                            VisualMarkerRegistry.setLastIds(mapView, newIds)
                        }
                        // Case 5: Markers already match
                        else -> {
                            Timber.d("📍 Markers already exist: ${currentIds.size}")
                        }
                    }
                }
            }

            // Update visual selection state
            LaunchedEffect(selectedAttraction) {
                visualMarkerProvider?.updateSelectedMarker(selectedAttraction)
            }
            
            // Force markers to be visible when DualLayerMarkerSystem first appears
            LaunchedEffect(mapView) {
                if (mapView != null) {
                    val existingIds = VisualMarkerRegistry.getLastIds(mapView)
                    if (existingIds.isNotEmpty()) {
                        Timber.d("🎯 DualLayerMarkerSystem: Found ${existingIds.size} preloaded markers, forcing visibility")
                        // Force a small delay and then refresh
                        delay(50)
                        try {
                            mapView.invalidate()
                            Timber.d("🔄 Forced MapView invalidation for preloaded markers")
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to invalidate MapView")
                        }
                    }
                }
            }
        } else {
            // Compose visual mode: render visual markers using Compose overlay
            MarkerOverlay(
                mapView = mapView,
                attractions = attractions,
                selectedAttraction = selectedAttraction,
                onMarkerClick = onMarkerClick,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(500f),
                enableClustering = false,
                animationDuration = 0,
                transparentMode = false // visual markers visible
            )
        }
        
        // Layer 3: User location marker management
        if (mapView != null) {
            UserLocationMarkerManager(
                mapView = mapView,
                showMarker = showUserLocationMarker,
                userLocation = userLocation
            )
        }
        
        if (!composeVisualMode) {
            // Layer 2: Transparent overlay for click detection (top layer)
            // MUST be rendered AFTER native markers to be on top
            TransparentClickOverlay(
                mapView = mapView,
                attractions = attractions,
                selectedAttraction = selectedAttraction,
                onMarkerClick = onMarkerClick,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1000f) // Ensure it's on top of everything
            )
        }
        
        // Log system state
        LaunchedEffect(attractions.size, composeVisualMode) {
            Timber.d("🎯 DualLayerMarkerSystem: ${attractions.size} attractions")
            if (composeVisualMode) {
                Timber.d("  ├─ Visual Layer: Compose markers (easter egg mode)")
                Timber.d("  └─ Interactive Layer: Click handled by visual markers")
            } else {
                Timber.d("  ├─ Visual Layer: Native MapKit markers (perfect binding)")
                Timber.d("  └─ Interactive Layer: Transparent Compose overlays (1.1x hit area)")
                Timber.d("  └─ Overlay is on TOP for click detection")
            }
        }
    }
}

/**
 * Transparent overlay that only handles click events
 * Visual representation is handled by native markers
 */
@Composable
fun TransparentClickOverlay(
    mapView: MapView?,
    attractions: List<Attraction>,
    selectedAttraction: Attraction?,
    onMarkerClick: (Attraction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use existing MarkerOverlay with transparent markers
    MarkerOverlay(
        mapView = mapView,
        attractions = attractions,
        selectedAttraction = selectedAttraction,
        onMarkerClick = onMarkerClick,
        modifier = modifier,
        enableClustering = false,
        animationDuration = 0, // No animations for click layer
        transparentMode = true // Enable transparent mode for click-only handling
    )
}

/**
 * Менеджер маркера местоположения пользователя
 * Управляет одним экземпляром провайдера для отображения и скрытия маркера
 */
@Composable
fun UserLocationMarkerManager(
    mapView: MapView,
    showMarker: Boolean,
    userLocation: Pair<Double, Double>?
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Создаем провайдер маркера только один раз для MapView
    val markerProvider = remember(mapView) {
        UserLocationMarkerProvider(mapView, context)
    }
    
    // Управляем маркером в зависимости от состояния
    LaunchedEffect(showMarker, userLocation) {
        if (showMarker && userLocation != null) {
            Timber.d("📍 Showing user location marker at: ${userLocation.first}, ${userLocation.second}")
            markerProvider.showUserLocationMarker(userLocation)
        } else {
            Timber.d("🚫 Hiding user location marker (showMarker=$showMarker, location=$userLocation)")
            markerProvider.hideUserLocationMarker()
        }
    }
    
    // Немедленно скрываем маркер при отключении showMarker
    LaunchedEffect(showMarker) {
        if (!showMarker) {
            Timber.d("🚫 Marker disabled, hiding immediately")
            markerProvider.hideUserLocationMarker()
        }
    }
    
    // Отображаем анимацию пульсации только когда маркер видим
    if (showMarker && userLocation != null) {
        UserLocationPulseAnimation(
            mapView = mapView,
            userLocation = userLocation,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(999f) // Ниже основного маркера
        )
    }
    
    // Очистка при размонтировании
    DisposableEffect(mapView) {
        onDispose {
            markerProvider.cleanup()
        }
    }
}


/**
 * Анимация пульсации для маркера местоположения пользователя
 * Отображается под основным нативным маркером
 */
@Composable
fun UserLocationPulseAnimation(
    mapView: MapView,
    userLocation: Pair<Double, Double>,
    modifier: Modifier = Modifier
) {
    var screenPosition by remember { mutableStateOf<Offset?>(null) }
    
    // Отслеживаем изменения местоположения И движения камеры для синхронизации
    LaunchedEffect(mapView, userLocation) {
        val point = com.yandex.mapkit.geometry.Point(userLocation.first, userLocation.second)
        
        fun updatePosition() {
            try {
                val worldToScreen = mapView.mapWindow.worldToScreen(point)
                worldToScreen?.let { screenPoint ->
                    screenPosition = Offset(screenPoint.x, screenPoint.y)
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to convert user location to screen coordinates for pulse animation")
            }
        }
        
        // Обновляем позицию сразу
        updatePosition()
        
        // Добавляем слушатель изменений камеры для синхронизации
        val cameraListener = object : com.yandex.mapkit.map.CameraListener {
            override fun onCameraPositionChanged(
                map: com.yandex.mapkit.map.Map,
                cameraPosition: com.yandex.mapkit.map.CameraPosition,
                cameraUpdateReason: com.yandex.mapkit.map.CameraUpdateReason,
                finished: Boolean
            ) {
                updatePosition()
            }
        }
        
        mapView.map.addCameraListener(cameraListener)
        
        // Очистка при размонтировании
        try {
            awaitCancellation()
        } finally {
            mapView.map.removeCameraListener(cameraListener)
        }
    }
    
    // Отображаем анимацию пульсации в вычисленной позиции
    screenPosition?.let { position ->
        val density = LocalDensity.current
        Box(modifier = modifier) {
            // Анимация пульсации
            val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.5f, // Уменьшенный радиус свечения
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1500,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )
            
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1500,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_alpha"
            )
            
            // Пульсирующий круг
            Canvas(
                modifier = Modifier
                    .absoluteOffset(
                        x = (position.x / density.density - 20).dp, // Обновлено для нового размера
                        y = (position.y / density.density - 20).dp
                    )
                    .size(40.dp) // Уменьшенный размер
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.width / 2) * pulseScale
                
                drawCircle(
                    color = Color(0xFF4CAF50).copy(alpha = pulseAlpha),
                    radius = radius,
                    center = center
                )
            }
        }
    }
}
