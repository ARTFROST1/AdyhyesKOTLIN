package com.adygyes.app.presentation.ui.screens.map

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.adygyes.app.data.local.cache.ImageCacheManager
import com.adygyes.app.presentation.ui.map.markers.DualLayerMarkerSystem
import com.adygyes.app.presentation.ui.util.MapPreloadManager
import com.adygyes.app.presentation.viewmodel.MapPreloadViewModel
import com.adygyes.app.presentation.viewmodel.MapStateViewModel
import com.adygyes.app.presentation.viewmodel.ThemeViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.mapview.MapView
import timber.log.Timber

/**
 * CompositionLocal to access persistent MapView, preload manager and simple map controls
 */
class MapHostController(
    val mapView: MapView,
    val preloadManager: MapPreloadManager
)

val LocalMapHostController = compositionLocalOf<MapHostController?> { null }

/**
 * Renders markers on the background map before user navigates to MapScreen
 * This ensures markers are visible immediately when transitioning to map
 */
@Composable
private fun BackgroundMarkerRenderer(
    mapView: MapView,
    preloadManager: MapPreloadManager,
    modifier: Modifier = Modifier
) {
    val attractions by preloadManager.attractions.collectAsState()
    val preloadState by preloadManager.preloadState.collectAsState()
    val imageCacheManager: ImageCacheManager = remember { 
        ImageCacheManager(mapView.context) 
    }
    
    // Only render when attractions are loaded and markers are ready
    if (attractions.isNotEmpty() && preloadState.allMarkersReady) {
        DualLayerMarkerSystem(
            mapView = mapView,
            attractions = attractions,
            selectedAttraction = null, // No selection in background
            imageCacheManager = imageCacheManager,
            onMarkerClick = { }, // No clicks in background
            modifier = modifier,
            composeVisualMode = false // Use native markers for performance
        )
        
        Timber.d("🎯 BackgroundMarkerRenderer: Displaying ${attractions.size} markers on background map")
    }
}

@Composable
fun MapHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Get preload manager via ViewModel for Hilt injection
    val preloadViewModel: MapPreloadViewModel = hiltViewModel()
    val preloadManager = preloadViewModel.preloadManager

    // Persistent MapView instance
    val mapView = remember {
        MapView(context).apply {
            // Initial default camera (will be adjusted by state below)
            this.map.move(
                CameraPosition(Point(44.6098, 40.1006), 10.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 0.0f),
                null
            )
            Timber.d("🗺️ MapHost: Created MapView with hashCode: ${this.hashCode()}")
        }
    }
    
    // Start preloading data as soon as MapView is created
    LaunchedEffect(mapView) {
        preloadManager.startPreload(mapView)
    }

    val themeViewModel: ThemeViewModel = hiltViewModel()
    val themeMode by themeViewModel.themeMode.collectAsState(initial = "system")
    val isDarkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val mapStateViewModel: MapStateViewModel = hiltViewModel()
    val cameraState by mapStateViewModel.cameraState.collectAsState()

    // Apply theme-based map style whenever theme changes
    LaunchedEffect(isDarkTheme) {
        MapStyleProvider.applyMapStyle(mapView, isDarkTheme)
        MapStyleProvider.configureMapInteraction(mapView)
    }

    // Apply initial camera from persisted state once
    var appliedInitialCamera by remember { mutableStateOf(false) }
    LaunchedEffect(cameraState) {
        if (!appliedInitialCamera) {
            val pos = CameraPosition(
                Point(cameraState.lat, cameraState.lon),
                cameraState.zoom,
                cameraState.azimuth,
                cameraState.tilt
            )
            mapView.map.move(pos, Animation(Animation.Type.SMOOTH, 0.0f), null)
            appliedInitialCamera = true
        }
    }

    // Camera listener to persist updates
    DisposableEffect(mapView) {
        val cameraListener = CameraListener { _, position, _, _ ->
            mapStateViewModel.onCameraPositionChanged(position)
        }
        mapView.map.addCameraListener(cameraListener)
        onDispose {
            mapView.map.removeCameraListener(cameraListener)
        }
    }

    // Manage MapKit & MapView lifecycle
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> {
                    try {
                        MapKitFactory.getInstance().onStart()
                    } catch (_: Exception) {}
                    mapView.onStart()
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    try {
                        MapKitFactory.getInstance().onStop()
                    } catch (_: Exception) {}
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CompositionLocalProvider(LocalMapHostController provides MapHostController(mapView, preloadManager)) {
        Box(modifier = modifier.fillMaxSize()) {
            // Bottom layer: persistent MapView - ALWAYS visible and ready
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )
            
            // Background marker rendering - this makes markers visible BEFORE navigation
            BackgroundMarkerRenderer(
                mapView = mapView,
                preloadManager = preloadManager,
                modifier = Modifier.fillMaxSize()
            )
            
            // Top layer: app content (NavHost and overlays)
            content()
        }
    }
}
