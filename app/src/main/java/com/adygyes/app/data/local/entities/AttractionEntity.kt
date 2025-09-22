package com.adygyes.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * Room entity for storing attraction data
 */
@Entity(tableName = "attractions")
@TypeConverters(Converters::class)
data class AttractionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val directions: String?,
    val images: List<String>,
    val rating: Float?,
    val workingHours: String?,
    val phoneNumber: String?,
    val email: String?,
    val website: String?,
    val isFavorite: Boolean,
    val tags: List<String>,
    val priceInfo: String?,
    val amenities: List<String>,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Type converters for Room database
 */
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(separator = ",") ?: ""
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
