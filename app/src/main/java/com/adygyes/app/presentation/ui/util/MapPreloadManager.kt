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
    private val imageCacheManager: ImageCacheManager,
    private val preferencesManager: com.adygyes.app.data.local.preferences.PreferencesManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _preloadState = MutableStateFlow(PreloadState())
    val preloadState: StateFlow<PreloadState> = _preloadState.asStateFlow()
    
    private val _attractions = MutableStateFlow<List<Attraction>>(emptyList())
    val attractions: StateFlow<List<Attraction>> = _attractions.asStateFlow()
    
    private var preloadJob: Job? = null
    private var visualMarkerProvider: VisualMarkerProvider? = null
    private var lastKnownDataVersion: String? = null
    
    init {
        // Monitor data version changes and reset when needed
        scope.launch {
            preferencesManager.userPreferencesFlow.collect { preferences ->
                val currentVersion = preferences.dataVersion
                if (lastKnownDataVersion != null && lastKnownDataVersion != currentVersion) {
                    Timber.d("🔄 Data version changed from '$lastKnownDataVersion' to '$currentVersion', starting data update process")
                    
                    // Show data updating overlay
                    _preloadState.value = _preloadState.value.copy(
                        dataUpdating = true,
                        progress = 0.1f
                    )
                    
                    // Small delay to show the overlay
                    delay(500)
                    
                    // Update progress
                    _preloadState.value = _preloadState.value.copy(progress = 0.3f)
                    
                    // Force reset all components
                    forceReset()
                    VisualMarkerRegistry.forceResetAll()
                    
                    // Update progress
                    _preloadState.value = _preloadState.value.copy(progress = 0.7f)
                    
                    // Wait a bit more for cleanup to complete
                    delay(1000)
                    
                    // Complete the update process
                    _preloadState.value = _preloadState.value.copy(
                        dataUpdating = false,
                        progress = 1.0f
                    )
                    
                    Timber.d("✅ Data update process completed")
                    
                    // CRITICAL: Restart preload process with new data after version update
                    delay(500) // Small delay to ensure UI updates
                    
                    // Find the current MapView and restart preload
                    val currentMapView = VisualMarkerRegistry.getCurrentMapView()
                    if (currentMapView != null) {
                        Timber.d("🔄 Restarting preload process with new data after version update")
                        startPreload(currentMapView)
                    } else {
                        Timber.w("⚠️ No MapView available for restarting preload after version update")
                    }
                }
                lastKnownDataVersion = currentVersion
            }
        }
    }
    
    data class PreloadState(
        val isLoading: Boolean = false,
        val dataLoaded: Boolean = false,
        val markersCreated: Boolean = false,
        val imagesPreloaded: Boolean = false,
        val allMarkersReady: Boolean = false,  // New field for fully loaded markers with images
        val dataUpdating: Boolean = false,  // New field for data version update process
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
                
                // Step 2: Preload visual markers (invisible) using VisualMarkerProvider
                // This creates the native MapKit markers but keeps them invisible for later animation
                withContext(Dispatchers.Main) {
                    // Ensure we're on the main thread for MapKit operations
                    visualMarkerProvider = VisualMarkerRegistry.getOrCreate(mapView, imageCacheManager)
                    
                    visualMarkerProvider?.let { provider ->
                        // Preload all visual markers (invisible, ready for animation)
                        provider.preloadMarkers(loadedAttractions)
                        // Store the IDs so they won't be recreated later
                        VisualMarkerRegistry.setLastIds(mapView, loadedAttractions.map { it.id }.toSet())
                        
                        Timber.d("✅ Preloaded ${loadedAttractions.size} invisible markers on MapView: ${mapView.hashCode()}")
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
                
                // Step 4: Wait for marker creation and image preloading to complete
                // Images are now preloaded during marker creation, so less wait time needed
                delay(500) // Reduced wait time since images are preloaded
                
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
        _attractions.value = emptyList()
        visualMarkerProvider = null
        Timber.d("Preload data cleared")
    }
    
    /**
     * Force reset when data version changes
     */
    fun forceReset() {
        Timber.d("🔄 Force resetting MapPreloadManager due to data version change")
        
        // Cancel any active preload job
        preloadJob?.cancel()
        
        // Reset state but preserve dataUpdating status
        val currentState = _preloadState.value
        _preloadState.value = PreloadState(
            dataUpdating = currentState.dataUpdating,
            progress = currentState.progress
        )
        _attractions.value = emptyList()
        
        // Clear visual marker provider reference
        visualMarkerProvider = null
        
        Timber.d("✅ MapPreloadManager force reset completed")
    }
    
    /**
     * Check if preload is complete
     */
    fun isPreloadComplete(): Boolean {
        val state = _preloadState.value
        return state.dataLoaded && state.markersCreated && !state.isLoading
    }
    
    /**
     * Animate preloaded markers when user navigates to map
     */
    fun animatePreloadedMarkers() {
        visualMarkerProvider?.animatePreloadedMarkers()
        Timber.d("🎬 Triggered animation for preloaded markers")
    }
    
    /**
     * Show preloaded markers immediately (fallback)
     */
    fun showPreloadedMarkers() {
        visualMarkerProvider?.showPreloadedMarkers()
        Timber.d("👁️ Showed preloaded markers immediately")
    }
    
    /**
     * Check if markers are preloaded and ready
     */
    fun hasPreloadedMarkers(): Boolean {
        return visualMarkerProvider?.hasPreloadedMarkers() == true
    }
    
    fun onDestroy() {
        scope.cancel()
    }
}
