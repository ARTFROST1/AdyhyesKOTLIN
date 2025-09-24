package com.adygyes.app.presentation.ui.map.markers

import androidx.compose.ui.geometry.Offset
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import timber.log.Timber
import kotlin.math.sqrt

/**
 * Utility class for converting between geographic coordinates and screen pixels
 * Essential for positioning overlay markers correctly on the map
 * Optimized for high-frequency updates with caching
 */
object MapCoordinateConverter {
    
    // Cache for high-frequency coordinate conversions
    private data class CacheKey(val lat: Double, val lng: Double, val cameraHash: Int)
    private val positionCache = mutableMapOf<CacheKey, Offset?>()
    private var lastCameraHash = 0
    
    private fun getCameraHash(mapView: MapView): Int {
        return try {
            val pos = mapView.map.cameraPosition
            "${pos.target.latitude}_${pos.target.longitude}_${pos.zoom}".hashCode()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Convert geographic coordinates to screen pixels
     * @param mapView The MapView instance
     * @param latitude Geographic latitude
     * @param longitude Geographic longitude
     * @return Screen position as Offset, or null if point is not visible
     */
    fun geoToScreen(
        mapView: MapView?,
        latitude: Double,
        longitude: Double
    ): Offset? {
        if (mapView == null) {
            return null
        }
        
        return try {
            // Check if map is properly initialized
            if (mapView.mapWindow == null) {
                return null
            }
            
            // Use caching for high-frequency updates
            val currentCameraHash = getCameraHash(mapView)
            val cacheKey = CacheKey(latitude, longitude, currentCameraHash)
            
            // Clear cache if camera changed significantly
            if (currentCameraHash != lastCameraHash) {
                positionCache.clear()
                lastCameraHash = currentCameraHash
            }
            
            // Check cache first
            positionCache[cacheKey]?.let { return it }
            
            // Calculate new position
            val geoPoint = Point(latitude, longitude)
            val screenPoint = mapView.mapWindow.worldToScreen(geoPoint)
            
            val result = screenPoint?.let { Offset(it.x, it.y) }
            
            // Cache result
            positionCache[cacheKey] = result
            
            result
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Convert screen coordinates to geographic coordinates
     * @param mapView The MapView instance
     * @param screenX Screen X coordinate in pixels
     * @param screenY Screen Y coordinate in pixels
     * @return Geographic point, or null if conversion fails
     */
    fun screenToGeo(
        mapView: MapView?,
        screenX: Float,
        screenY: Float
    ): Point? {
        if (mapView == null) {
            Timber.w("MapView is null, cannot convert coordinates")
            return null
        }
        
        return try {
            val screenPoint = ScreenPoint(screenX, screenY)
            mapView.mapWindow.screenToWorld(screenPoint)
        } catch (e: Exception) {
            Timber.e(e, "Error converting screen to geo coordinates")
            null
        }
    }
    
    /**
     * Check if a geographic point is visible on the current map view
     * @param mapView The MapView instance
     * @param latitude Geographic latitude
     * @param longitude Geographic longitude
     * @return true if point is visible, false otherwise
     */
    fun isPointVisible(
        mapView: MapView?,
        latitude: Double,
        longitude: Double
    ): Boolean {
        if (mapView == null) return false
        
        return try {
            val point = Point(latitude, longitude)
            val visibleRegion = mapView.map.visibleRegion
            
            // Check if point is within visible bounds
            val topLeft = visibleRegion.topLeft
            val topRight = visibleRegion.topRight
            val bottomLeft = visibleRegion.bottomLeft
            val bottomRight = visibleRegion.bottomRight
            
            // Simple bounds check (works for most cases)
            val minLat = minOf(bottomLeft.latitude, bottomRight.latitude)
            val maxLat = maxOf(topLeft.latitude, topRight.latitude)
            val minLon = minOf(bottomLeft.longitude, topLeft.longitude)
            val maxLon = maxOf(bottomRight.longitude, topRight.longitude)
            
            point.latitude in minLat..maxLat && point.longitude in minLon..maxLon
        } catch (e: Exception) {
            Timber.e(e, "Error checking point visibility")
            false
        }
    }
    
    /**
     * Calculate pixel distance between two geographic points on screen
     * @param mapView The MapView instance
     * @param point1 First geographic point
     * @param point2 Second geographic point
     * @return Distance in pixels, or Float.MAX_VALUE if calculation fails
     */
    fun pixelDistance(
        mapView: MapView?,
        point1: Point,
        point2: Point
    ): Float {
        if (mapView == null) return Float.MAX_VALUE
        
        val screen1 = geoToScreen(mapView, point1.latitude, point1.longitude)
        val screen2 = geoToScreen(mapView, point2.latitude, point2.longitude)
        
        return if (screen1 != null && screen2 != null) {
            val dx = screen2.x - screen1.x
            val dy = screen2.y - screen1.y
            sqrt(dx * dx + dy * dy)
        } else {
            Float.MAX_VALUE
        }
    }
    
    /**
     * Get all visible points from a list
     * @param mapView The MapView instance
     * @param points List of geographic points
     * @return List of visible points only
     */
    fun getVisiblePoints(
        mapView: MapView?,
        points: List<Point>
    ): List<Point> {
        if (mapView == null) return emptyList()
        
        return points.filter { point ->
            isPointVisible(mapView, point.latitude, point.longitude)
        }
    }
    
    /**
     * Convert a list of geographic points to screen positions
     * @param mapView The MapView instance
     * @param points List of geographic points
     * @return Map of points to their screen positions (null for invisible points)
     */
    fun batchGeoToScreen(
        mapView: MapView?,
        points: List<Point>
    ): Map<Point, Offset?> {
        if (mapView == null) return points.associateWith { null }
        
        return points.associateWith { point ->
            geoToScreen(mapView, point.latitude, point.longitude)
        }
    }
}
