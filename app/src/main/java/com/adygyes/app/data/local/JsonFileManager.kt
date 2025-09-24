package com.adygyes.app.data.local

import android.content.Context
import android.os.Environment
import com.adygyes.app.data.remote.dto.AttractionsResponse
import com.adygyes.app.data.remote.dto.AttractionDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for reading and writing attractions JSON file
 * For development mode, we copy the asset file to internal storage for editing
 */
@Singleton
class JsonFileManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "adygyes_developer_prefs"
        private const val KEY_CUSTOM_JSON = "custom_attractions_json"
        private const val KEY_USE_CUSTOM = "use_custom_json"
    }
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = false
    }
    
    private val internalJsonFile: File
        get() = File(context.filesDir, "attractions_editable.json")
    
    /**
     * Initialize editable JSON file from assets if not exists
     */
    suspend fun initializeEditableJson() {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val useCustom = prefs.getBoolean(KEY_USE_CUSTOM, false)
            
            if (!internalJsonFile.exists()) {
                if (useCustom) {
                    // Try to restore from SharedPreferences
                    val savedJson = prefs.getString(KEY_CUSTOM_JSON, null)
                    if (savedJson != null) {
                        internalJsonFile.writeText(savedJson)
                        Timber.d("✅ Restored custom JSON from preferences")
                        return
                    }
                }
                
                // Copy from assets to internal storage
                val assetJson = context.assets.open("attractions.json")
                    .bufferedReader()
                    .use { it.readText() }
                    
                internalJsonFile.writeText(assetJson)
                Timber.d("✅ Copied attractions.json to internal storage for editing")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to initialize editable JSON")
        }
    }
    
    /**
     * Read attractions from editable JSON file
     */
    fun readAttractions(): AttractionsResponse? {
        return try {
            if (!internalJsonFile.exists()) {
                // Fallback to assets
                val assetJson = context.assets.open("attractions.json")
                    .bufferedReader()
                    .use { it.readText() }
                return json.decodeFromString<AttractionsResponse>(assetJson)
            }
            
            val jsonString = internalJsonFile.readText()
            json.decodeFromString<AttractionsResponse>(jsonString)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to read attractions JSON")
            null
        }
    }
    
    /**
     * Write attractions to editable JSON file
     */
    fun writeAttractions(attractionsResponse: AttractionsResponse): Boolean {
        return try {
            val jsonString = json.encodeToString(attractionsResponse)
            internalJsonFile.writeText(jsonString)
            
            // Also save to SharedPreferences for persistence
            saveToPreferences(jsonString)
            
            Timber.d("✅ Successfully wrote ${attractionsResponse.attractions.size} attractions to JSON")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to write attractions JSON")
            false
        }
    }
    
    /**
     * Save JSON to SharedPreferences for persistence across app reinstalls
     */
    private fun saveToPreferences(jsonString: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_CUSTOM_JSON, jsonString)
                .putBoolean(KEY_USE_CUSTOM, true)
                .apply()
            Timber.d("✅ Saved JSON to SharedPreferences for persistence")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to save to SharedPreferences")
        }
    }
    
    /**
     * Add a new attraction to JSON
     */
    fun addAttraction(attraction: AttractionDto): Boolean {
        return try {
            val current = readAttractions() ?: return false
            
            // Check if ID already exists
            if (current.attractions.any { it.id == attraction.id }) {
                Timber.w("⚠️ Attraction with ID ${attraction.id} already exists")
                return false
            }
            
            val updated = current.copy(
                attractions = current.attractions + attraction,
                lastUpdated = java.time.LocalDate.now().toString()
            )
            
            writeAttractions(updated)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to add attraction")
            false
        }
    }
    
    /**
     * Update an existing attraction
     */
    fun updateAttraction(attraction: AttractionDto): Boolean {
        return try {
            val current = readAttractions() ?: return false
            
            val updated = current.copy(
                attractions = current.attractions.map {
                    if (it.id == attraction.id) attraction else it
                },
                lastUpdated = java.time.LocalDate.now().toString()
            )
            
            writeAttractions(updated)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to update attraction")
            false
        }
    }
    
    /**
     * Delete an attraction
     */
    fun deleteAttraction(attractionId: String): Boolean {
        return try {
            val current = readAttractions() ?: return false
            
            val updated = current.copy(
                attractions = current.attractions.filter { it.id != attractionId },
                lastUpdated = java.time.LocalDate.now().toString()
            )
            
            writeAttractions(updated)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to delete attraction")
            false
        }
    }
    
    /**
     * Generate next available ID
     */
    fun generateNextId(): String {
        val current = readAttractions() ?: return "1"
        val maxId = current.attractions
            .mapNotNull { it.id.toIntOrNull() }
            .maxOrNull() ?: 0
        return (maxId + 1).toString()
    }
    
    /**
     * Reset to original assets JSON
     */
    fun resetToOriginal(): Boolean {
        return try {
            if (internalJsonFile.exists()) {
                internalJsonFile.delete()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to reset JSON")
            false
        }
    }
    
    /**
     * Export JSON to Downloads folder for manual copying to project
     * Returns the file path if successful, null otherwise
     */
    fun exportToDownloads(): String? {
        return try {
            val attractions = readAttractions() ?: return null
            val jsonString = json.encodeToString(attractions)
            
            // Save to Downloads folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val exportFile = File(downloadsDir, "attractions_export_${System.currentTimeMillis()}.json")
            exportFile.writeText(jsonString)
            
            Timber.d("✅ Exported attractions to: ${exportFile.absolutePath}")
            exportFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to export JSON")
            null
        }
    }
    
    /**
     * Get the formatted JSON string for display or sharing
     */
    fun getJsonString(): String? {
        return try {
            val attractions = readAttractions() ?: return null
            json.encodeToString(attractions)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to get JSON string")
            null
        }
    }
    
    /**
     * Copy JSON to clipboard-friendly format
     */
    fun getClipboardJson(): String? {
        return try {
            val attractions = readAttractions() ?: return null
            // Create compact version for clipboard
            val compactJson = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
            compactJson.encodeToString(attractions)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to get clipboard JSON")
            null
        }
    }
}
