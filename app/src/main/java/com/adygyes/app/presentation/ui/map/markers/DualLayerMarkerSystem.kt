package com.adygyes.app.presentation.ui.map.markers

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.data.local.cache.ImageCacheManager
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.map.PlacemarkMapObject
import timber.log.Timber
import kotlinx.coroutines.delay

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
    enableAppearAnimation: Boolean = true // Enable smooth appearance animation for initial markers
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

            // Incremental sync of markers on attractions changes
            // Only update if the markers haven't been preloaded or attractions have changed
            LaunchedEffect(mapView, attractions) {
                if (mapView != null && visualMarkerProvider != null) {
                    val currentIds = VisualMarkerRegistry.getLastIds(mapView)
                    val newIds = attractions.map { it.id }.toSet()
                    
                    Timber.d("🔍 FILTER UPDATE: Current markers: ${currentIds.size}, New filtered: ${newIds.size}")
                    Timber.d("🔍 FILTER UPDATE: Attractions: ${attractions.map { it.name }.joinToString(", ")}")
                    
                    // Only update if there's a difference in attractions
                    if (currentIds != newIds) {
                        // For initial load (empty -> populated), use animation
                        // For filtering (populated -> different populated), don't animate
                        val isInitialLoad = currentIds.isEmpty() && newIds.isNotEmpty()
                        if (isInitialLoad && enableAppearAnimation) {
                            visualMarkerProvider.setAppearAnimation(true)
                            visualMarkerProvider.addVisualMarkers(attractions)
                        } else {
                            visualMarkerProvider.setAppearAnimation(false)
                            visualMarkerProvider.updateVisualMarkers(attractions)
                        }
                        VisualMarkerRegistry.setLastIds(mapView, newIds)
                        Timber.d("📍 FILTER APPLIED: Updated visual markers from ${currentIds.size} to ${attractions.size}")
                    } else {
                        Timber.d("📍 Markers already match filter: ${attractions.size}, skipping sync")
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
