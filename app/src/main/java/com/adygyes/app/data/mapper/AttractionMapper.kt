package com.adygyes.app.data.mapper

import com.adygyes.app.data.local.entities.AttractionEntity
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.domain.model.ContactInfo
import com.adygyes.app.domain.model.Location

/**
 * Mapper for converting between domain models and data entities
 */
object AttractionMapper {
    
    /**
     * Convert AttractionEntity to Attraction domain model
     */
    fun AttractionEntity.toDomainModel(): Attraction {
        return Attraction(
            id = id,
            name = name,
            description = description,
            category = AttractionCategory.valueOf(category),
            location = Location(
                latitude = latitude,
                longitude = longitude,
                address = address,
                directions = directions
            ),
            images = images,
            rating = rating,
            workingHours = workingHours,
            contactInfo = if (phoneNumber != null || email != null || website != null) {
                ContactInfo(
                    phone = phoneNumber,
                    email = email,
                    website = website
                )
            } else null,
            isFavorite = isFavorite,
            tags = tags,
            priceInfo = priceInfo,
            amenities = amenities
        )
    }
    
    /**
     * Convert Attraction domain model to AttractionEntity
     */
    fun Attraction.toEntity(): AttractionEntity {
        return AttractionEntity(
            id = id,
            name = name,
            description = description,
            category = category.name,
            latitude = location.latitude,
            longitude = location.longitude,
            address = location.address,
            directions = location.directions,
            images = images,
            rating = rating,
            workingHours = workingHours,
            phoneNumber = contactInfo?.phone,
            email = contactInfo?.email,
            website = contactInfo?.website,
            isFavorite = isFavorite,
            tags = tags,
            priceInfo = priceInfo,
            amenities = amenities
        )
    }
    
    /**
     * Convert list of AttractionEntity to list of Attraction domain models
     */
    fun List<AttractionEntity>.toDomainModels(): List<Attraction> {
        return map { it.toDomainModel() }
    }
    
    /**
     * Convert list of Attraction domain models to list of AttractionEntity
     */
    fun List<Attraction>.toEntities(): List<AttractionEntity> {
        return map { it.toEntity() }
    }
}
