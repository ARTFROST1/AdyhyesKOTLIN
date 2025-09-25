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
                Timber.d("MapKit initialized successfully with API key")
            } else {
                // Use a development/demo API key or handle gracefully
                Timber.w("Yandex MapKit API key not configured. Please add YANDEX_MAPKIT_API_KEY to local.properties")
                // For development, you can use a demo key or handle this case
                // MapKitFactory.setApiKey("your-demo-key-here")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize MapKit")
        }
    }

    /**
     * Initialize Timber for logging in debug builds
     */
    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Adygyes Application Started")
        }
    }
    
    /**
     * Initialize locale settings
     */
    private fun initializeLocale() {
        applicationScope.launch {
            try {
                val currentLanguage = localeManager.currentLanguage.first()
                Timber.d("Application initialized with language: $currentLanguage")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize locale")
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
