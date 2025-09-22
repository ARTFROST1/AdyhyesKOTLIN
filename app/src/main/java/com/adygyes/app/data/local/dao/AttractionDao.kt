package com.adygyes.app.data.local.dao

import androidx.room.*
import com.adygyes.app.data.local.entities.AttractionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for attraction-related database operations
 */
@Dao
interface AttractionDao {
    
    @Query("SELECT * FROM attractions")
    fun getAllAttractions(): Flow<List<AttractionEntity>>
    
    @Query("SELECT * FROM attractions WHERE id = :attractionId")
    suspend fun getAttractionById(attractionId: String): AttractionEntity?
    
    @Query("SELECT * FROM attractions WHERE category = :category")
    fun getAttractionsByCategory(category: String): Flow<List<AttractionEntity>>
    
    @Query("SELECT * FROM attractions WHERE isFavorite = 1")
    fun getFavoriteAttractions(): Flow<List<AttractionEntity>>
    
    @Query("SELECT * FROM attractions WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchAttractions(query: String): Flow<List<AttractionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttraction(attraction: AttractionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttractions(attractions: List<AttractionEntity>)
    
    @Update
    suspend fun updateAttraction(attraction: AttractionEntity)
    
    @Query("UPDATE attractions SET isFavorite = :isFavorite WHERE id = :attractionId")
    suspend fun updateFavoriteStatus(attractionId: String, isFavorite: Boolean)
    
    @Delete
    suspend fun deleteAttraction(attraction: AttractionEntity)
    
    @Query("DELETE FROM attractions")
    suspend fun deleteAllAttractions()
    
    @Query("SELECT COUNT(*) FROM attractions")
    suspend fun getAttractionsCount(): Int
}
