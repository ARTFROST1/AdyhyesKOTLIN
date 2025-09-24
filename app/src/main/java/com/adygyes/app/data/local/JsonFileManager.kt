package com.adygyes.app.data.local

import android.content.Context
import com.adygyes.app.data.remote.dto.AttractionsResponse
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Упрощенный менеджер для чтения данных только из assets/attractions.json
 * Никаких кэшей, сохранений или Developer Mode - только чистое чтение из файла проекта
 */
@Singleton
class JsonFileManager @Inject constructor(
    private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = false
    }
    
    /**
     * Инициализация не требуется - всегда читаем из assets
     */
    suspend fun initializeEditableJson() {
        Timber.d("✅ Using assets/attractions.json as single source of truth")
    }
    
    /**
     * Читаем данные только из assets/attractions.json
     */
    fun readAttractions(): AttractionsResponse? {
        return try {
            val jsonString = context.assets.open("attractions.json")
                .bufferedReader()
                .use { it.readText() }
            
            json.decodeFromString<AttractionsResponse>(jsonString)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to read attractions.json from assets")
            null
        }
    }
}
