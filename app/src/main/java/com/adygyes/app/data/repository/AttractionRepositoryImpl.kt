package com.adygyes.app.data.repository

import android.content.Context
import com.adygyes.app.data.local.dao.AttractionDao
import com.adygyes.app.data.mapper.AttractionMapper.toDomainModel
import com.adygyes.app.data.mapper.AttractionMapper.toDomainModels
import com.adygyes.app.data.mapper.AttractionMapper.toEntity
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.domain.repository.AttractionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
    @ApplicationContext private val context: Context
) : AttractionRepository {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
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
            // Check if data already exists
            if (attractionDao.getAttractionsCount() > 0) {
                Timber.d("Data already loaded, skipping initial load")
                return
            }
            
            // Load from JSON file in raw resources
            val jsonString = context.assets.open("attractions.json").bufferedReader().use { it.readText() }
            
            // Parse JSON and insert into database
            // Note: We'll need to create the JSON parsing logic
            Timber.d("Loading initial attraction data from JSON")
            
            // For now, let's create some sample data
            val sampleAttractions = createSampleAttractions()
            sampleAttractions.forEach { attraction ->
                attractionDao.insertAttraction(attraction.toEntity())
            }
            
            Timber.d("Initial data loaded: ${sampleAttractions.size} attractions")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load initial data")
        }
    }
    
    override suspend fun isDataLoaded(): Boolean {
        return attractionDao.getAttractionsCount() > 0
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
}
