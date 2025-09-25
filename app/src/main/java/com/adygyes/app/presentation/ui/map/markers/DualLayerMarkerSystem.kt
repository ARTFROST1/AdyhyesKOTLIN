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
    modifier: Modifier = Modifier
) {
    // IMPORTANT: Box to ensure proper layering
    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Native visual markers (bottom layer)
        // Persist provider across navigation via registry to avoid re-creating markers
        val visualMarkerProvider = remember(mapView) {
            mapView?.let { mv ->
                VisualMarkerRegistry.getOrCreate(mv, imageCacheManager)
            }
        }

        // Incremental sync of markers on attractions changes
        LaunchedEffect(mapView, attractions) {
            if (mapView != null && visualMarkerProvider != null) {
                visualMarkerProvider.updateVisualMarkers(attractions)
                VisualMarkerRegistry.setLastIds(mapView, attractions.map { it.id }.toSet())
                Timber.d("📍 Incremental sync of visual markers: ${attractions.size}")
            }
        }

        // Update visual selection state
        LaunchedEffect(selectedAttraction) {
            visualMarkerProvider?.updateSelectedMarker(selectedAttraction)
        }
        
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
        
        // Log system state
        LaunchedEffect(attractions.size) {
            Timber.d("🎯 DualLayerMarkerSystem: ${attractions.size} attractions")
            Timber.d("  ├─ Visual Layer: Native MapKit markers (perfect binding)")
            Timber.d("  └─ Interactive Layer: Transparent Compose overlays (1.1x hit area)")
            Timber.d("  └─ Overlay is on TOP for click detection")
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
