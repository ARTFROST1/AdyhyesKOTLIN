package com.adygyes.app.data.local.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.adygyes.app.data.local.preferences.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling app localization and language changes
 */
@Singleton
class LocaleManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        const val LANGUAGE_RUSSIAN = "ru"
        const val LANGUAGE_ENGLISH = "en"
        const val DEFAULT_LANGUAGE = LANGUAGE_RUSSIAN
    }
    
    /**
     * Get current language from preferences
     */
    val currentLanguage: Flow<String> = preferencesManager.userPreferencesFlow
        .map { preferences -> preferences.language }
    
    /**
     * Set application language
     */
    suspend fun setLanguage(languageCode: String) {
        preferencesManager.updateLanguage(languageCode)
    }
    
    /**
     * Apply locale to context
     */
    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * Get locale for language code
     */
    fun getLocale(languageCode: String): Locale {
        return when (languageCode) {
            LANGUAGE_RUSSIAN -> Locale("ru", "RU")
            LANGUAGE_ENGLISH -> Locale("en", "US")
            else -> Locale(DEFAULT_LANGUAGE, "RU")
        }
    }
    
    /**
     * Check if language is supported
     */
    fun isSupportedLanguage(languageCode: String): Boolean {
        return languageCode in listOf(LANGUAGE_RUSSIAN, LANGUAGE_ENGLISH)
    }
    
    /**
     * Get system default language if supported, otherwise return default
     */
    fun getSystemLanguageOrDefault(): String {
        val systemLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0].language
        } else {
            @Suppress("DEPRECATION")
            Locale.getDefault().language
        }
        
        return if (isSupportedLanguage(systemLanguage)) {
            systemLanguage
        } else {
            DEFAULT_LANGUAGE
        }
    }
    
    /**
     * Get display name for language
     */
    fun getLanguageDisplayName(languageCode: String, context: Context): String {
        return when (languageCode) {
            LANGUAGE_RUSSIAN -> "Русский"
            LANGUAGE_ENGLISH -> "English"
            else -> "Русский"
        }
    }
}
