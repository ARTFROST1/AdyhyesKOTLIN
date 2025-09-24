package com.adygyes.app.data.repository

import android.content.Context
import com.adygyes.app.data.local.dao.AttractionDao
import com.adygyes.app.data.local.JsonFileManager
import com.adygyes.app.data.local.preferences.PreferencesManager
import com.adygyes.app.data.mapper.AttractionMapper.toDomainModel
import com.adygyes.app.data.mapper.AttractionMapper.toDomainModels
import com.adygyes.app.data.mapper.AttractionMapper.toEntitiesFromDto
import com.adygyes.app.data.mapper.AttractionMapper.toEntity
import com.adygyes.app.data.remote.dto.AttractionDto
import com.adygyes.app.data.remote.dto.AttractionsResponse
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.domain.repository.AttractionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AttractionRepository
 */
@Singleton
class AttractionRepositoryImpl @Inject constructor(
    private val attractionDao: AttractionDao,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : AttractionRepository {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = false
    }
    
    override fun getAllAttractions(): Flow<List<Attraction>> {
        return attractionDao.getAllAttractions()
            .map { entities -> entities.toDomainModels() }
    }
    
    override suspend fun getAttractionById(attractionId: String): Attraction? {
        return attractionDao.getAttractionById(attractionId)?.toDomainModel()
    }
    
    override fun getAttractionsByCategory(category: AttractionCategory): Flow<List<Attraction>> {
        return attractionDao.getAttractionsByCategory(category.name)
            .map { entities -> entities.toDomainModels() }
    }
    
    override fun getFavoriteAttractions(): Flow<List<Attraction>> {
        return attractionDao.getFavoriteAttractions()
            .map { entities -> entities.toDomainModels() }
    }
    
    override fun searchAttractions(query: String): Flow<List<Attraction>> {
        return attractionDao.searchAttractions(query)
            .map { entities -> entities.toDomainModels() }
    }
    
    override suspend fun updateFavoriteStatus(attractionId: String, isFavorite: Boolean) {
        attractionDao.updateFavoriteStatus(attractionId, isFavorite)
    }
    
    override suspend fun loadInitialData() {
        try {
            Timber.d("🔄 Starting loadInitialData() - checking version...")
            
            // Load from JSON file in assets
            val jsonString = try {
                context.assets.open("attractions.json").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to read attractions.json from assets")
                // Fallback to sample data if JSON file not found
                loadSampleData()
                return
            }
            
            Timber.d("✅ Successfully read attractions.json from assets")
            
            // Parse JSON to get version
            val attractionsResponse = try {
                json.decodeFromString<AttractionsResponse>(jsonString)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse attractions JSON")
                // Fallback to sample data if parsing fails
                loadSampleData()
                return
            }
            
            // Get current data version from preferences
            val currentVersion = preferencesManager.userPreferencesFlow.first().dataVersion
            val jsonVersion = attractionsResponse.version
            val hasData = attractionDao.getAttractionsCount() > 0
            
            Timber.d("📊 Version check: stored='$currentVersion', json='$jsonVersion', hasData=$hasData")
            
            // Check if we need to update data
            val needsUpdate = when {
                !hasData -> {
                    Timber.d("🆕 No data in database, loading initial data")
                    true
                }
                currentVersion == null -> {
                    Timber.d("🔄 No version stored, updating to version $jsonVersion")
                    true
                }
                currentVersion != jsonVersion -> {
                    Timber.d("🔄 Version mismatch: stored='$currentVersion', json='$jsonVersion'. Updating data...")
                    true
                }
                else -> {
                    Timber.d("✅ Data is up to date (version $currentVersion)")
                    false
                }
            }
            
            Timber.d("🎯 Update decision: needsUpdate=$needsUpdate")
            
            if (needsUpdate) {
                // Clear existing data and reload
                Timber.d("Clearing existing data and reloading from JSON")
                attractionDao.deleteAll()
                
                // Convert DTOs to entities and insert into database
                val entities = attractionsResponse.attractions.toEntitiesFromDto()
                attractionDao.insertAttractions(entities)
                
                // Update stored version
                preferencesManager.updateDataVersion(jsonVersion)
                
                Timber.d("✅ Data updated: ${entities.size} attractions loaded (version $jsonVersion)")
            } else {
                Timber.d("✅ Data is current, no update needed")
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to load initial data")
            // Fallback to sample data
            loadSampleData()
        }
    }
    
    /**
     * Load sample data as fallback when JSON loading fails
     */
    private suspend fun loadSampleData() {
        try {
            val sampleAttractions = createSampleAttractions()
            sampleAttractions.forEach { attraction ->
                attractionDao.insertAttraction(attraction.toEntity())
            }
            Timber.d("Loaded ${sampleAttractions.size} sample attractions as fallback")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load sample data")
        }
    }
    
    override suspend fun isDataLoaded(): Boolean {
        return attractionDao.getAttractionsCount() > 0
    }
    
    /**
     * Force reload data from JSON file (ignores version check)
     */
    suspend fun forceReloadData(): Boolean {
        return try {
            Timber.d("🔄 Force reloading data from JSON...")
            
            // Load from JSON file in assets
            val jsonString = try {
                context.assets.open("attractions.json").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Timber.e(e, "Failed to read attractions.json from assets")
                return false
            }
            
            // Parse JSON
            val attractionsResponse = try {
                json.decodeFromString<AttractionsResponse>(jsonString)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse attractions JSON")
                return false
            }
            
            // Clear existing data and reload
            attractionDao.deleteAll()
            
            // Convert DTOs to entities and insert into database
            val entities = attractionsResponse.attractions.toEntitiesFromDto()
            attractionDao.insertAttractions(entities)
            
            // Update stored version
            preferencesManager.updateDataVersion(attractionsResponse.version)
            
            Timber.d("✅ Force reload completed: ${entities.size} attractions loaded (version ${attractionsResponse.version})")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Force reload failed")
            false
        }
    }
    
    /**
     * Create sample attractions for testing
     * This will be replaced with actual JSON parsing
     */
    private fun createSampleAttractions(): List<Attraction> {
        return listOf(
            Attraction(
                id = "1",
                name = "Хаджохская теснина",
                description = "Живописное ущелье реки Белой с каменными стенами высотой до 35 метров",
                category = AttractionCategory.NATURE,
                location = com.adygyes.app.domain.model.Location(
                    latitude = 44.2877,
                    longitude = 40.1749,
                    address = "пос. Каменномостский, Республика Адыгея"
                ),
                images = listOf("hadzhoh_gorge.jpg"),
                rating = 4.8f,
                workingHours = "Ежедневно: 9:00 - 18:00",
                tags = listOf("природа", "ущелье", "река"),
                priceInfo = "Взрослый: 500₽, Детский: 250₽"
            ),
            Attraction(
                id = "2",
                name = "Водопады Руфабго",
                description = "Каскад из 16 водопадов в живописном ущелье",
                category = AttractionCategory.NATURE,
                location = com.adygyes.app.domain.model.Location(
                    latitude = 44.2653,
                    longitude = 40.1714,
                    address = "пос. Каменномостский, Республика Адыгея"
                ),
                images = listOf("rufabgo_waterfalls.jpg"),
                rating = 4.7f,
                workingHours = "Ежедневно: 8:00 - 20:00",
                tags = listOf("водопады", "природа", "треккинг"),
                priceInfo = "Вход: 400₽"
            ),
            Attraction(
                id = "3",
                name = "Плато Лаго-Наки",
                description = "Высокогорное плато с альпийскими лугами и панорамными видами",
                category = AttractionCategory.NATURE,
                location = com.adygyes.app.domain.model.Location(
                    latitude = 44.0000,
                    longitude = 40.0000,
                    address = "Майкопский район, Республика Адыгея"
                ),
                images = listOf("lagonaki_plateau.jpg"),
                rating = 4.9f,
                tags = listOf("горы", "плато", "треккинг", "панорама"),
                priceInfo = "Бесплатно"
            ),
            Attraction(
                id = "4",
                name = "Национальный музей Республики Адыгея",
                description = "Музей истории и культуры адыгского народа",
                category = AttractionCategory.CULTURE,
                location = com.adygyes.app.domain.model.Location(
                    latitude = 44.6098,
                    longitude = 40.1006,
                    address = "г. Майкоп, ул. Советская, 229"
                ),
                images = listOf("national_museum.jpg"),
                rating = 4.5f,
                workingHours = "Вт-Вс: 10:00 - 18:00",
                tags = listOf("музей", "культура", "история"),
                priceInfo = "Взрослый: 150₽, Студенты: 75₽"
            ),
            Attraction(
                id = "5",
                name = "Кавказский биосферный заповедник",
                description = "Один из крупнейших горно-лесных заповедников Европы",
                category = AttractionCategory.NATURE,
                location = com.adygyes.app.domain.model.Location(
                    latitude = 43.9800,
                    longitude = 40.2100,
                    address = "Майкопский район, Республика Адыгея"
                ),
                images = listOf("biosphere_reserve.jpg"),
                rating = 4.8f,
                tags = listOf("заповедник", "природа", "экотуризм"),
                priceInfo = "Экскурсии от 800₽"
            )
        )
    }
    
    override suspend fun addAttraction(attraction: Attraction): Boolean {
        return try {
            // Add to database only - no JSON editing in simplified mode
            attractionDao.insertAttraction(attraction.toEntity())
            
            Timber.d("✅ Added attraction: ${attraction.name}")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to add attraction")
            false
        }
    }
    
    override suspend fun updateAttraction(attraction: Attraction): Boolean {
        return try {
            // Update in database only - no JSON editing in simplified mode
            attractionDao.updateAttraction(attraction.toEntity())
            
            Timber.d("✅ Updated attraction: ${attraction.name}")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to update attraction")
            false
        }
    }
    
    override suspend fun deleteAttraction(attractionId: String): Boolean {
        return try {
            // Delete from database only - no JSON editing in simplified mode
            attractionDao.deleteAttraction(attractionId)
            
            Timber.d("✅ Deleted attraction: $attractionId")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to delete attraction")
            false
        }
    }
    
    override suspend fun reloadFromJson(): Boolean {
        return try {
            // Clear database
            attractionDao.deleteAll()
            
            // Reload from JSON
            val jsonManager = JsonFileManager(context)
            jsonManager.initializeEditableJson()
            
            val attractionsResponse = jsonManager.readAttractions()
            if (attractionsResponse != null) {
                val entities = attractionsResponse.attractions.toEntitiesFromDto()
                attractionDao.insertAttractions(entities)
                Timber.d("✅ Reloaded ${entities.size} attractions from JSON")
                true
            } else {
                // Fallback to assets
                loadInitialData()
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to reload from JSON")
            false
        }
    }
    
    /**
     * Extension function to convert Attraction to AttractionDto
     */
    private fun Attraction.toDto(): AttractionDto {
        return AttractionDto(
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
}
