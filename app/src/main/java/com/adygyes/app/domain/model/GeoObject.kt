package com.adygyes.app.domain.model

import java.util.UUID

/**
 * Domain model representing geographic objects like parks, protected areas, etc.
 */
data class GeoObject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val type: GeoObjectType,
    val polygon: List<Location>,
    val fillColor: String,
    val strokeColor: String,
    val strokeWidth: Float = 2.0f,
    val opacity: Float = 0.3f,
    val isVisible: Boolean = true,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Domain model representing tourist trails and routes
 */
data class TouristTrail(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val difficulty: TrailDifficulty,
    val polyline: List<Location>,
    val color: String,
    val width: Float = 3.0f,
    val length: Float, // in kilometers
    val duration: String, // estimated time
    val isVisible: Boolean = true,
    val waypoints: List<Waypoint> = emptyList(),
    val tags: List<String> = emptyList()
)

/**
 * Waypoint along a tourist trail
 */
data class Waypoint(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val location: Location,
    val type: WaypointType,
    val icon: String? = null
)

/**
 * Types of geographic objects
 */
enum class GeoObjectType(val displayName: String, val defaultColor: String) {
    NATIONAL_PARK("Национальный парк", "#4CAF50"),
    NATURE_RESERVE("Заповедник", "#2E7D32"),
    PROTECTED_AREA("Охраняемая территория", "#66BB6A"),
    FOREST("Лес", "#388E3C"),
    WATER_BODY("Водоем", "#03A9F4"),
    MOUNTAIN_RANGE("Горный хребет", "#795548"),
    RECREATIONAL_ZONE("Зона отдыха", "#FF9800"),
    CULTURAL_SITE("Культурный объект", "#9C27B0")
}

/**
 * Trail difficulty levels
 */
enum class TrailDifficulty(val displayName: String, val color: String) {
    EASY("Легкий", "#4CAF50"),
    MODERATE("Средний", "#FF9800"),
    HARD("Сложный", "#F44336"),
    EXTREME("Экстремальный", "#9C27B0")
}

/**
 * Types of waypoints along trails
 */
enum class WaypointType(val displayName: String, val icon: String) {
    START("Начало маршрута", "🚩"),
    FINISH("Конец маршрута", "🏁"),
    VIEWPOINT("Смотровая площадка", "👁️"),
    REST_AREA("Место отдыха", "🏕️"),
    WATER_SOURCE("Источник воды", "💧"),
    DANGER("Опасный участок", "⚠️"),
    PHOTO_SPOT("Место для фото", "📸"),
    LANDMARK("Ориентир", "📍")
}

/**
 * Extension functions for geo-objects
 */
fun GeoObject.getBounds(): Pair<Location, Location> {
    if (polygon.isEmpty()) return Pair(Location(0.0, 0.0), Location(0.0, 0.0))
    
    val minLat = polygon.minOf { it.latitude }
    val maxLat = polygon.maxOf { it.latitude }
    val minLon = polygon.minOf { it.longitude }
    val maxLon = polygon.maxOf { it.longitude }
    
    return Pair(
        Location(minLat, minLon),
        Location(maxLat, maxLon)
    )
}

fun TouristTrail.getBounds(): Pair<Location, Location> {
    if (polyline.isEmpty()) return Pair(Location(0.0, 0.0), Location(0.0, 0.0))
    
    val minLat = polyline.minOf { it.latitude }
    val maxLat = polyline.maxOf { it.latitude }
    val minLon = polyline.minOf { it.longitude }
    val maxLon = polyline.maxOf { it.longitude }
    
    return Pair(
        Location(minLat, minLon),
        Location(maxLat, maxLon)
    )
}
