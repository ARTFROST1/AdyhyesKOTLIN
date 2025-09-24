package com.adygyes.app.presentation.ui.map.markers

import androidx.compose.ui.geometry.Offset
import com.adygyes.app.domain.model.Attraction

/**
 * Represents the state of a single marker on the map
 */
data class MarkerState(
    val attraction: Attraction,
    val screenPosition: Offset? = null,
    val isVisible: Boolean = false,
    val isSelected: Boolean = false,
    val isLoading: Boolean = false,
    val imageUrl: String? = null,
    val clusterGroup: Int? = null
)

/**
 * Represents the overall state of the marker overlay system
 */
data class MarkerOverlayState(
    val markers: List<MarkerState> = emptyList(),
    val clusters: List<MarkerCluster> = emptyList(),
    val selectedMarkerId: String? = null,
    val isUpdating: Boolean = false,
    val enableClustering: Boolean = false,
    val zoomLevel: Float = 0f
)

/**
 * Represents a cluster of nearby markers
 */
data class MarkerCluster(
    val id: String,
    val centerPosition: Offset,
    val markerIds: List<String>,
    val displayCount: Int,
    val bounds: ClusterBounds
)

/**
 * Represents the bounds of a marker cluster
 */
data class ClusterBounds(
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float
) {
    fun contains(position: Offset): Boolean {
        return position.x in minX..maxX && position.y in minY..maxY
    }
    
    fun overlaps(other: ClusterBounds): Boolean {
        return !(maxX < other.minX || minX > other.maxX || 
                maxY < other.minY || minY > other.maxY)
    }
}
