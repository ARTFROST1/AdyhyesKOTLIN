package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adygyes.app.data.local.preferences.PreferencesManager
import com.adygyes.app.data.local.cache.CacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val cacheManager: CacheManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            // Load preferences using the actual PreferencesManager API
            preferencesManager.userPreferencesFlow.collect { preferences ->
                _uiState.value = SettingsUiState(
                    theme = if (preferences.isDarkTheme) Theme.DARK else Theme.LIGHT,
                    language = if (preferences.language == "ru") Language.RUSSIAN else Language.ENGLISH,
                    showUserLocation = preferences.autoCenterLocation,
                    clusterMarkers = true, // Default value since not in preferences
                    showTraffic = preferences.showTraffic,
                    offlineMode = preferences.offlineMode,
                    autoDownloadImages = true, // Default value
                    pushNotifications = preferences.notificationEnabled,
                    locationAlerts = preferences.notificationEnabled && preferences.autoCenterLocation,
                    appVersion = "1.0.0"
                )
            }
        }
    }
    
    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesManager.updateDarkTheme(theme == Theme.DARK)
        }
    }
    
    fun setLanguage(language: Language) {
        viewModelScope.launch {
            val langCode = when (language) {
                Language.RUSSIAN -> "ru"
                Language.ENGLISH -> "en"
            }
            preferencesManager.updateLanguage(langCode)
        }
    }
    
    fun setShowUserLocation(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateAutoCenterLocation(show)
        }
    }
    
    fun setClusterMarkers(cluster: Boolean) {
        // This setting is not in PreferencesManager, so just update UI state
        _uiState.update { it.copy(clusterMarkers = cluster) }
    }
    
    fun setShowTraffic(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateShowTraffic(show)
        }
    }
    
    fun setOfflineMode(offline: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateOfflineMode(offline)
        }
    }
    
    fun setAutoDownloadImages(autoDownload: Boolean) {
        // This setting is not in PreferencesManager, so just update UI state
        _uiState.update { it.copy(autoDownloadImages = autoDownload) }
    }
    
    fun setPushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateNotificationEnabled(enabled)
        }
    }
    
    fun setLocationAlerts(enabled: Boolean) {
        _uiState.update { it.copy(locationAlerts = enabled) }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            cacheManager.clearAllCache()
        }
    }
    
    enum class Theme(val displayName: String) {
        LIGHT("Light"),
        DARK("Dark"),
        SYSTEM("System Default")
    }
    
    enum class Language(val displayName: String) {
        RUSSIAN("Русский"),
        ENGLISH("English")
    }
    
    data class SettingsUiState(
        val theme: Theme = Theme.SYSTEM,
        val language: Language = Language.RUSSIAN,
        val showUserLocation: Boolean = true,
        val clusterMarkers: Boolean = true,
        val showTraffic: Boolean = false,
        val offlineMode: Boolean = false,
        val autoDownloadImages: Boolean = true,
        val pushNotifications: Boolean = true,
        val locationAlerts: Boolean = false,
        val appVersion: String = "1.0.0"
    )
}
