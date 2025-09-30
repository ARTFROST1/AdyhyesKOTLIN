package com.adygyes.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.adygyes.app.data.local.locale.LocaleManager
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Application class for Adygyes app.
 * Responsible for app-wide initialization.
 */
@HiltAndroidApp
class AdygyesApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var localeManager: LocaleManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        initializeTimber()
        initializeMapKit()
        initializeLocale()
    }
    
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Handle configuration changes if needed
    }

    /**
     * Initialize Yandex MapKit SDK
     */
    private fun initializeMapKit() {
        try {
            val apiKey = BuildConfig.YANDEX_MAPKIT_API_KEY
            if (apiKey.isNotEmpty() && apiKey != "YOUR_API_KEY_HERE") {
                MapKitFactory.setApiKey(apiKey)
                if (BuildConfig.DEBUG) {
                    Timber.d("MapKit initialized successfully with API key")
                } else {
                    android.util.Log.d("AdygyesApp", "MapKit initialized")
                }
            } else {
                val errorMsg = "Yandex MapKit API key not configured. Please add YANDEX_MAPKIT_API_KEY to local.properties"
                if (BuildConfig.DEBUG) {
                    Timber.w(errorMsg)
                } else {
                    android.util.Log.w("AdygyesApp", errorMsg)
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Failed to initialize MapKit: ${e.message}"
            if (BuildConfig.DEBUG) {
                Timber.e(e, errorMsg)
            } else {
                android.util.Log.e("AdygyesApp", errorMsg, e)
            }
            // Don't crash the app, continue without MapKit
        }
    }

    /**
     * Initialize Timber for logging in debug builds
     */
    private fun initializeTimber() {
        try {
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
                Timber.d("Adygyes Application Started")
            } else {
                // For release, plant a simple tree or no-op tree
                Timber.plant(object : Timber.Tree() {
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                        // No-op for release
                    }
                })
            }
        } catch (e: Exception) {
            // Silently fail if Timber initialization fails
            android.util.Log.e("AdygyesApp", "Failed to initialize Timber", e)
        }
    }
    
    /**
     * Initialize locale settings
     */
    private fun initializeLocale() {
        applicationScope.launch {
            try {
                val currentLanguage = localeManager.currentLanguage.first()
                if (BuildConfig.DEBUG) {
                    Timber.d("Application initialized with language: $currentLanguage")
                } else {
                    android.util.Log.d("AdygyesApp", "Language: $currentLanguage")
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to initialize locale: ${e.message}"
                if (BuildConfig.DEBUG) {
                    Timber.e(e, errorMsg)
                } else {
                    android.util.Log.e("AdygyesApp", errorMsg, e)
                }
                // Don't crash, use default locale
            }
        }
    }

    /**
     * Create Coil ImageLoader with custom configuration
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .components {
                add(SvgDecoder.Factory())
            }
            .respectCacheHeaders(false)
            .build()
    }
}
