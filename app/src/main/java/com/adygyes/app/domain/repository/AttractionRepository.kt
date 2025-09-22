package com.adygyes.app.domain.repository

import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for attraction data operations
 */
interface AttractionRepository {
    
    /**
     * Get all attractions as a Flow
     */
    fun getAllAttractions(): Flow<List<Attraction>>
    
    /**
     * Get a specific attraction by ID
     */
    suspend fun getAttractionById(attractionId: String): Attraction?
    
    /**
     * Get attractions by category
     */
    fun getAttractionsByCategory(category: AttractionCategory): Flow<List<Attraction>>
    
    /**
     * Get favorite attractions
     */
    fun getFavoriteAttractions(): Flow<List<Attraction>>
    
    /**
     * Search attractions by query
     */
    fun searchAttractions(query: String): Flow<List<Attraction>>
    
    /**
     * Update favorite status of an attraction
     */
    suspend fun updateFavoriteStatus(attractionId: String, isFavorite: Boolean)
    
    /**
     * Load initial data from JSON file
     */
    suspend fun loadInitialData()
    
    /**
     * Check if initial data is loaded
     */
    suspend fun isDataLoaded(): Boolean
}
