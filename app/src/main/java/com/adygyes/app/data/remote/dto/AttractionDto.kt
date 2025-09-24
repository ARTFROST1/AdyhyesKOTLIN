package com.adygyes.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for attractions from JSON
 */
@Serializable
data class AttractionDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("category")
    val category: String,
    
    @SerialName("latitude")
    val latitude: Double,
    
    @SerialName("longitude")
    val longitude: Double,
    
    @SerialName("address")
    val address: String? = null,
    
    @SerialName("directions")
    val directions: String? = null,
    
    @SerialName("images")
    val images: List<String> = emptyList(),
    
    @SerialName("rating")
    val rating: Float? = null,
    
    @SerialName("workingHours")
    val workingHours: String? = null,
    
    @SerialName("phoneNumber")
    val phoneNumber: String? = null,
    
    @SerialName("email")
    val email: String? = null,
    
    @SerialName("website")
    val website: String? = null,
    
    @SerialName("isFavorite")
    val isFavorite: Boolean = false,
    
    @SerialName("tags")
    val tags: List<String> = emptyList(),
    
    @SerialName("priceInfo")
    val priceInfo: String? = null,
    
    @SerialName("amenities")
    val amenities: List<String> = emptyList()
)

/**
 * Root object for JSON parsing
 */
@Serializable
data class AttractionsResponse(
    @SerialName("version")
    val version: String,
    
    @SerialName("lastUpdated")
    val lastUpdated: String,
    
    @SerialName("attractions")
    val attractions: List<AttractionDto>
)
