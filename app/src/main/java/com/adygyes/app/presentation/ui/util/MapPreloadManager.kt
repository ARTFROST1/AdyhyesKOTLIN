package com.adygyes.app.presentation.ui.util

import com.adygyes.app.data.local.cache.ImageCacheManager
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.repository.AttractionRepository
import com.adygyes.app.presentation.ui.map.markers.VisualMarkerProvider
import com.adygyes.app.presentation.ui.map.markers.VisualMarkerRegistry
import com.yandex.mapkit.Animation
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages preloading of map data and markers in background while splash screen is shown
 */
@Singleton
class MapPreloadManager @Inject constructor(
    private val repository: AttractionRepository,
    private val imageCacheManager: ImageCacheManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _preloadState = MutableStateFlow(PreloadState())
    val preloadState: StateFlow<PreloadState> = _preloadState.asStateFlow()
    
    private val _attractions = MutableStateFlow<List<Attraction>>(emptyList())
    val attractions: StateFlow<List<Attraction>> = _attractions.asStateFlow()
    
    private var preloadJob: Job? = null
    private var visualMarkerProvider: VisualMarkerProvider? = null
    
    data class PreloadState(
        val isLoading: Boolean = false,
        val dataLoaded: Boolean = false,
        val markersCreated: Boolean = false,
        val imagesPreloaded: Boolean = false,
        val allMarkersReady: Boolean = false,  // New field for fully loaded markers with images
        val error: String? = null,
        val progress: Float = 0f  // 0.0 to 1.0
    )
    
    /**
     * Start preloading data and creating markers
     * Call this as soon as MapView is created
     */
    fun startPreload(mapView: MapView) {
        // Prevent multiple preload attempts
        if (preloadJob?.isActive == true) {
            Timber.d("Preload already in progress")
            return
        }
        
        // Ensure MapView is properly initialized
        if (mapView.mapWindow == null) {
            Timber.w("MapView not ready yet, retrying in 100ms")
            scope.launch {
                delay(100)
                startPreload(mapView)
            }
            return
        }
        
        Timber.d("Starting preload with MapView: ${mapView.hashCode()}")
        
        preloadJob = scope.launch {
            try {
                _preloadState.value = PreloadState(isLoading = true, progress = 0.1f)
                Timber.d("Starting map preload")
                
                // Step 1: Load attractions data (30% of progress)
                val loadedAttractions = withContext(Dispatchers.IO) {
                    repository.getAllAttractions().first() // Convert Flow to List
                }
                _attractions.value = loadedAttractions
                _preloadState.value = _preloadState.value.copy(
                    dataLoaded = true, 
                    progress = 0.3f
                )
                Timber.d("Loaded ${loadedAttractions.size} attractions")
                
                // Step 2: Create visual markers using VisualMarkerProvider
                // This creates the native MapKit markers that will be visible immediately
                withContext(Dispatchers.Main) {
                    // Ensure we're on the main thread for MapKit operations
                    visualMarkerProvider = VisualMarkerRegistry.getOrCreate(mapView, imageCacheManager)
                    
                    visualMarkerProvider?.let { provider ->
                        // Create all visual markers (this happens synchronously)
                        provider.updateVisualMarkers(loadedAttractions)
                        // Store the IDs so they won't be recreated later
                        VisualMarkerRegistry.setLastIds(mapView, loadedAttractions.map { it.id }.toSet())
                        
                        Timber.d("✅ Created ${loadedAttractions.size} visual markers on MapView: ${mapView.hashCode()}")
                        
                        // Force map to refresh and show markers immediately
                        try {
                            mapView.invalidate()
                            // Trigger a small camera movement to force refresh
                            val currentPosition = mapView.map.cameraPosition
                            mapView.map.move(
                                CameraPosition(
                                    currentPosition.target,
                                    currentPosition.zoom,
                                    currentPosition.azimuth,
                                    currentPosition.tilt
                                ),
                                Animation(Animation.Type.SMOOTH, 0.0f),
                                null
                            )
                            Timber.d("🔄 Forced MapView refresh and camera update")
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to force map refresh")
                        }
                    }
                }
                
                _preloadState.value = _preloadState.value.copy(
                    markersCreated = true,
                    progress = 0.5f
                )
                Timber.d("Visual markers creation completed")
                
                // Step 3: Preload images in cache AND wait for markers to load them
                val imageUrls: List<String> = loadedAttractions.mapNotNull { attraction -> 
                    attraction.images.firstOrNull() // Get first image URL from List<String>
                }
                if (imageUrls.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        imageCacheManager.preloadImages(imageUrls)
                    }
                    Timber.d("Preloaded ${imageUrls.size} images")
                }
                
                _preloadState.value = _preloadState.value.copy(
                    imagesPreloaded = true,
                    progress = 0.8f
                )
                
                // Step 4: Wait a bit for markers to actually load their images
                // Visual markers load images asynchronously, we need to give them time
                delay(1500) // Give time for image loading in markers
                
                _preloadState.value = _preloadState.value.copy(
                    allMarkersReady = true,
                    isLoading = false,
                    progress = 1.0f
                )
                Timber.d("Map preload completed successfully with all markers and images ready")
                
            } catch (e: Exception) {
                Timber.e(e, "Error during map preload")
                _preloadState.value = PreloadState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Clear preload data when no longer needed
     */
    fun clearPreloadData() {
        preloadJob?.cancel()
        _preloadState.value = PreloadState()
        Timber.d("Preload data cleared")
    }
    
    /**
     * Check if preload is complete
     */
    fun isPreloadComplete(): Boolean {
        val state = _preloadState.value
        return state.dataLoaded && state.markersCreated && !state.isLoading
    }
    
    fun onDestroy() {
        scope.cancel()
    }
}
