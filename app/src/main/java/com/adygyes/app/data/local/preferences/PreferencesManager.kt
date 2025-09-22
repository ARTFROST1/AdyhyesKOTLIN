package com.adygyes.app.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling user preferences using DataStore
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    // Preference Keys
    companion object {
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_MAP_TYPE = stringPreferencesKey("map_type")
        private val KEY_SHOW_TRAFFIC = booleanPreferencesKey("show_traffic")
        private val KEY_AUTO_CENTER_LOCATION = booleanPreferencesKey("auto_center_location")
        private val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        private val KEY_FILTER_CATEGORIES = stringSetPreferencesKey("filter_categories")
        private val KEY_SORT_OPTION = stringPreferencesKey("sort_option")
        private val KEY_OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        private val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val KEY_DATA_VERSION = stringPreferencesKey("data_version")
        private val KEY_LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }
    
    /**
     * User preferences data class
     */
    data class UserPreferences(
        val isDarkTheme: Boolean = false,
        val language: String = "ru", // "ru" or "en"
        val mapType: String = "normal", // "normal", "satellite", "hybrid"
        val showTraffic: Boolean = false,
        val autoCenterLocation: Boolean = true,
        val isFirstLaunch: Boolean = true,
        val filterCategories: Set<String> = emptySet(),
        val sortOption: String = "name", // "name", "rating", "distance"
        val offlineMode: Boolean = false,
        val notificationEnabled: Boolean = true,
        val dataVersion: String? = null,
        val lastSyncTime: Long = 0L
    )
    
    /**
     * Flow of user preferences
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Error reading preferences")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapUserPreferences(preferences)
        }
    
    /**
     * Map preferences to UserPreferences data class
     */
    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        return UserPreferences(
            isDarkTheme = preferences[KEY_DARK_THEME] ?: false,
            language = preferences[KEY_LANGUAGE] ?: "ru",
            mapType = preferences[KEY_MAP_TYPE] ?: "normal",
            showTraffic = preferences[KEY_SHOW_TRAFFIC] ?: false,
            autoCenterLocation = preferences[KEY_AUTO_CENTER_LOCATION] ?: true,
            isFirstLaunch = preferences[KEY_FIRST_LAUNCH] ?: true,
            filterCategories = preferences[KEY_FILTER_CATEGORIES] ?: emptySet(),
            sortOption = preferences[KEY_SORT_OPTION] ?: "name",
            offlineMode = preferences[KEY_OFFLINE_MODE] ?: false,
            notificationEnabled = preferences[KEY_NOTIFICATION_ENABLED] ?: true,
            dataVersion = preferences[KEY_DATA_VERSION],
            lastSyncTime = preferences[KEY_LAST_SYNC_TIME] ?: 0L
        )
    }
    
    /**
     * Update dark theme preference
     */
    suspend fun updateDarkTheme(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DARK_THEME] = isDarkTheme
        }
    }
    
    /**
     * Update language preference
     */
    suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }
    
    /**
     * Update map type preference
     */
    suspend fun updateMapType(mapType: String) {
        dataStore.edit { preferences ->
            preferences[KEY_MAP_TYPE] = mapType
        }
    }
    
    /**
     * Update traffic display preference
     */
    suspend fun updateShowTraffic(showTraffic: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_TRAFFIC] = showTraffic
        }
    }
    
    /**
     * Update auto center location preference
     */
    suspend fun updateAutoCenterLocation(autoCenter: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_CENTER_LOCATION] = autoCenter
        }
    }
    
    /**
     * Mark first launch as completed
     */
    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[KEY_FIRST_LAUNCH] = false
        }
    }
    
    /**
     * Update filter categories
     */
    suspend fun updateFilterCategories(categories: Set<String>) {
        dataStore.edit { preferences ->
            preferences[KEY_FILTER_CATEGORIES] = categories
        }
    }
    
    /**
     * Update sort option
     */
    suspend fun updateSortOption(sortOption: String) {
        dataStore.edit { preferences ->
            preferences[KEY_SORT_OPTION] = sortOption
        }
    }
    
    /**
     * Update offline mode
     */
    suspend fun updateOfflineMode(offlineMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_OFFLINE_MODE] = offlineMode
        }
    }
    
    /**
     * Update notification enabled
     */
    suspend fun updateNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED] = enabled
        }
    }
    
    /**
     * Update data version
     */
    suspend fun updateDataVersion(version: String) {
        dataStore.edit { preferences ->
            preferences[KEY_DATA_VERSION] = version
        }
    }
    
    /**
     * Update last sync time
     */
    suspend fun updateLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIME] = timestamp
        }
    }
    
    /**
     * Clear all preferences
     */
    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
