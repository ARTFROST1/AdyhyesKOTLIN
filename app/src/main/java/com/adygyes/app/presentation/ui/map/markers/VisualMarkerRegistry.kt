package com.adygyes.app.presentation.ui.map.markers

import com.adygyes.app.data.local.cache.ImageCacheManager
import com.yandex.mapkit.mapview.MapView

/**
 * Keeps VisualMarkerProvider instances and last rendered attraction ids per MapView
 * so that markers are not force-cleared/re-added when navigating away and back.
 */
object VisualMarkerRegistry {
    private data class Entry(
        val provider: VisualMarkerProvider,
        var lastIds: Set<String> = emptySet()
    )

    private val map = mutableMapOf<MapView, Entry>()

    fun getOrCreate(mapView: MapView, imageCacheManager: ImageCacheManager): VisualMarkerProvider {
        val entry = map[mapView]
        return if (entry != null) {
            entry.provider
        } else {
            val provider = VisualMarkerProvider(mapView, imageCacheManager)
            map[mapView] = Entry(provider)
            provider
        }
    }

    fun getLastIds(mapView: MapView): Set<String> = map[mapView]?.lastIds ?: emptySet()

    fun setLastIds(mapView: MapView, ids: Set<String>) {
        val entry = map[mapView]
        if (entry != null) {
            entry.lastIds = ids
        } else {
            // Should not happen: create empty provider-less entry
            map[mapView] = Entry(provider = VisualMarkerProvider(mapView, ImageCacheManager(mapView.context)), lastIds = ids)
        }
    }
}
