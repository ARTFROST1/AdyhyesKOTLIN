package com.adygyes.app.presentation.ui.screens.map

import android.content.Context
import android.graphics.Color
import com.adygyes.app.domain.model.GeoObject
import com.adygyes.app.domain.model.TouristTrail
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.map.PolygonMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import timber.log.Timber

/**
 * Provider for adding geo-objects (polygons) and tourist trails (polylines) to the map
 */
object GeoObjectProvider {
    
    /**
     * Add geo-objects (polygons) to the map
     */
    fun addGeoObjects(
        mapView: MapView,
        geoObjects: List<GeoObject>,
        isDarkTheme: Boolean
    ) {
        try {
            geoObjects.forEach { geoObject ->
                if (geoObject.isVisible && geoObject.polygon.isNotEmpty()) {
                    addGeoObjectToMap(mapView, geoObject, isDarkTheme)
                }
            }
            Timber.d("Added ${geoObjects.size} geo-objects to map")
        } catch (e: Exception) {
            Timber.e(e, "Failed to add geo-objects to map")
        }
    }
    
    /**
     * Add tourist trails (polylines) to the map
     */
    fun addTouristTrails(
        mapView: MapView,
        trails: List<TouristTrail>,
        isDarkTheme: Boolean
    ) {
        try {
            trails.forEach { trail ->
                if (trail.isVisible && trail.polyline.isNotEmpty()) {
                    addTrailToMap(mapView, trail, isDarkTheme)
                }
            }
            Timber.d("Added ${trails.size} tourist trails to map")
        } catch (e: Exception) {
            Timber.e(e, "Failed to add tourist trails to map")
        }
    }
    
    /**
     * Add a single geo-object polygon to the map
     */
    private fun addGeoObjectToMap(
        mapView: MapView,
        geoObject: GeoObject,
        isDarkTheme: Boolean
    ) {
        try {
            // Convert domain locations to Yandex Points
            val points = geoObject.polygon.map { location ->
                Point(location.latitude, location.longitude)
            }
            
            // Create linear ring for polygon
            val linearRing = LinearRing(points)
            val polygon = Polygon(linearRing, emptyList())
            
            // Add polygon to map
            val polygonMapObject = mapView.map.mapObjects.addPolygon(polygon)
            
            // Apply styling
            val fillColor = adjustColorForTheme(geoObject.fillColor, isDarkTheme, geoObject.opacity)
            val strokeColor = adjustColorForTheme(geoObject.strokeColor, isDarkTheme, 1.0f)
            
            polygonMapObject.apply {
                this.fillColor = fillColor
                this.strokeColor = strokeColor
                this.strokeWidth = geoObject.strokeWidth
                this.userData = geoObject
            }
            
            Timber.d("Added geo-object: ${geoObject.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to add geo-object: ${geoObject.name}")
        }
    }
    
    /**
     * Add a single tourist trail polyline to the map
     */
    private fun addTrailToMap(
        mapView: MapView,
        trail: TouristTrail,
        isDarkTheme: Boolean
    ) {
        try {
            // Convert domain locations to Yandex Points
            val points = trail.polyline.map { location ->
                Point(location.latitude, location.longitude)
            }
            
            // Create polyline
            val polyline = Polyline(points)
            
            // Add polyline to map
            val polylineMapObject = mapView.map.mapObjects.addPolyline(polyline)
            
            // Apply styling based on trail difficulty
            val trailColor = getTrailColorForTheme(trail, isDarkTheme)
            
            polylineMapObject.apply {
                this.setStrokeColor(trailColor)
                this.strokeWidth = trail.width
                this.userData = trail
                
                // Add dashed pattern for certain trail types
                if (trail.difficulty.name == "EXTREME") {
                    this.dashLength = 10.0f
                    this.gapLength = 5.0f
                }
            }
            
            // Add waypoint markers if any
            addWaypointMarkers(mapView, trail, isDarkTheme)
            
            Timber.d("Added tourist trail: ${trail.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to add tourist trail: ${trail.name}")
        }
    }
    
    /**
     * Add waypoint markers for a trail
     */
    private fun addWaypointMarkers(
        mapView: MapView,
        trail: TouristTrail,
        isDarkTheme: Boolean
    ) {
        trail.waypoints.forEach { waypoint ->
            try {
                val point = Point(waypoint.location.latitude, waypoint.location.longitude)
                val placemark = mapView.map.mapObjects.addPlacemark(point)
                
                // Create waypoint marker
                val waypointIcon = WaypointMarkerProvider.getWaypointMarker(
                    waypoint.type,
                    isDarkTheme
                )
                
                placemark.setIcon(waypointIcon)
                placemark.userData = waypoint
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to add waypoint: ${waypoint.name}")
            }
        }
    }
    
    /**
     * Adjust color based on theme and opacity
     */
    private fun adjustColorForTheme(
        colorHex: String,
        isDarkTheme: Boolean,
        opacity: Float
    ): Int {
        return try {
            val baseColor = Color.parseColor(colorHex)
            val alpha = (opacity * 255).toInt()
            
            if (isDarkTheme) {
                // Make colors slightly brighter in dark theme
                val red = Color.red(baseColor)
                val green = Color.green(baseColor)
                val blue = Color.blue(baseColor)
                
                val brighterRed = minOf(255, (red * 1.2).toInt())
                val brighterGreen = minOf(255, (green * 1.2).toInt())
                val brighterBlue = minOf(255, (blue * 1.2).toInt())
                
                Color.argb(alpha, brighterRed, brighterGreen, brighterBlue)
            } else {
                Color.argb(alpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            }
        } catch (e: Exception) {
            // Fallback to default color
            if (isDarkTheme) Color.argb((opacity * 255).toInt(), 100, 200, 100)
            else Color.argb((opacity * 255).toInt(), 76, 175, 80)
        }
    }
    
    /**
     * Get trail color based on difficulty and theme
     */
    private fun getTrailColorForTheme(trail: TouristTrail, isDarkTheme: Boolean): Int {
        return try {
            val baseColor = Color.parseColor(trail.difficulty.color)
            
            if (isDarkTheme) {
                // Make trail colors more visible in dark theme
                when (trail.difficulty.name) {
                    "EASY" -> Color.parseColor("#66BB6A")
                    "MODERATE" -> Color.parseColor("#FFB74D")
                    "HARD" -> Color.parseColor("#EF5350")
                    "EXTREME" -> Color.parseColor("#AB47BC")
                    else -> Color.parseColor("#90A4AE")
                }
            } else {
                baseColor
            }
        } catch (e: Exception) {
            // Fallback color
            if (isDarkTheme) Color.parseColor("#90A4AE") else Color.parseColor("#607D8B")
        }
    }
    
    /**
     * Clear all geo-objects and trails from the map
     */
    fun clearGeoObjects(mapView: MapView) {
        try {
            // Remove all polygons and polylines
            mapView.map.mapObjects.clear()
            Timber.d("Cleared all geo-objects from map")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear geo-objects from map")
        }
    }
    
    /**
     * Toggle visibility of geo-objects
     */
    fun toggleGeoObjectVisibility(mapView: MapView, show: Boolean) {
        try {
            mapView.map.mapObjects.traverse(object : com.yandex.mapkit.map.MapObjectVisitor {
                override fun onPlacemarkVisited(placemark: com.yandex.mapkit.map.PlacemarkMapObject) {}
                override fun onPolylineVisited(polyline: com.yandex.mapkit.map.PolylineMapObject) {}
                override fun onPolygonVisited(polygon: com.yandex.mapkit.map.PolygonMapObject) {
                    polygon.isVisible = show
                }
                override fun onCircleVisited(circle: com.yandex.mapkit.map.CircleMapObject) {}
                override fun onCollectionVisitStart(collection: com.yandex.mapkit.map.MapObjectCollection): Boolean = true
                override fun onCollectionVisitEnd(collection: com.yandex.mapkit.map.MapObjectCollection) {}
                override fun onClusterizedCollectionVisitStart(collection: com.yandex.mapkit.map.ClusterizedPlacemarkCollection): Boolean = true
                override fun onClusterizedCollectionVisitEnd(collection: com.yandex.mapkit.map.ClusterizedPlacemarkCollection) {}
            })
            Timber.d("Toggled geo-object visibility: $show")
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle geo-object visibility")
        }
    }
    
    /**
     * Toggle visibility of tourist trails
     */
    fun toggleTrailVisibility(mapView: MapView, show: Boolean) {
        try {
            mapView.map.mapObjects.traverse(object : com.yandex.mapkit.map.MapObjectVisitor {
                override fun onPlacemarkVisited(placemark: com.yandex.mapkit.map.PlacemarkMapObject) {}
                override fun onPolylineVisited(polyline: com.yandex.mapkit.map.PolylineMapObject) {
                    polyline.isVisible = show
                }
                override fun onPolygonVisited(polygon: com.yandex.mapkit.map.PolygonMapObject) {}
                override fun onCircleVisited(circle: com.yandex.mapkit.map.CircleMapObject) {}
                override fun onCollectionVisitStart(collection: com.yandex.mapkit.map.MapObjectCollection): Boolean = true
                override fun onCollectionVisitEnd(collection: com.yandex.mapkit.map.MapObjectCollection) {}
                override fun onClusterizedCollectionVisitStart(collection: com.yandex.mapkit.map.ClusterizedPlacemarkCollection): Boolean = true
                override fun onClusterizedCollectionVisitEnd(collection: com.yandex.mapkit.map.ClusterizedPlacemarkCollection) {}
            })
            Timber.d("Toggled trail visibility: $show")
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle trail visibility")
        }
    }
}
