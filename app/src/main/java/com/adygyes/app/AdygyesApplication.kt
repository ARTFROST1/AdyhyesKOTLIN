package com.adygyes.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Main Application class for Adygyes app.
 * Responsible for app-wide initialization.
 */
@HiltAndroidApp
class AdygyesApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        initializeTimber()
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
