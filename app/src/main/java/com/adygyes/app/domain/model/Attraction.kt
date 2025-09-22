package com.adygyes.app.domain.model

import java.util.UUID

/**
 * Domain model representing a tourist attraction or point of interest
 */
data class Attraction(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: AttractionCategory,
    val location: Location,
    val images: List<String>,
    val rating: Float? = null,
    val workingHours: String? = null,
    val contactInfo: ContactInfo? = null,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val priceInfo: String? = null,
    val amenities: List<String> = emptyList()
)

/**
 * Location data for an attraction
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val directions: String? = null
)

/**
 * Contact information for an attraction
 */
data class ContactInfo(
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val socialMedia: Map<String, String> = emptyMap()
)

/**
 * Categories of attractions
 */
enum class AttractionCategory(val displayName: String, val colorHex: String) {
    NATURE("Природа", "#4CAF50"),
    CULTURE("Культура", "#9C27B0"),
    HISTORY("История", "#795548"),
    ADVENTURE("Приключения", "#FF5722"),
    RECREATION("Отдых", "#03A9F4"),
    GASTRONOMY("Гастрономия", "#FF9800"),
    RELIGIOUS("Религиозные места", "#607D8B"),
    ENTERTAINMENT("Развлечения", "#E91E63")
}

/**
 * Extension functions for attractions
 */
fun Attraction.distanceTo(latitude: Double, longitude: Double): Float {
    // Simple distance calculation (Haversine formula)
    val earthRadius = 6371000.0 // meters
    val lat1Rad = Math.toRadians(location.latitude)
    val lat2Rad = Math.toRadians(latitude)
    val deltaLatRad = Math.toRadians(latitude - location.latitude)
    val deltaLonRad = Math.toRadians(longitude - location.longitude)
    
    val a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
            Math.cos(lat1Rad) * Math.cos(lat2Rad) *
            Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    
    return (earthRadius * c).toFloat()
}
