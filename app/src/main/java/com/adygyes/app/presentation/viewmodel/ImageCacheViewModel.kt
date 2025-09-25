package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.adygyes.app.data.local.cache.ImageCacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for managing image cache operations
 */
@HiltViewModel
class ImageCacheViewModel @Inject constructor(
    private val imageCacheManager: ImageCacheManager
) : ViewModel() {
    
    private val _cacheInfo = MutableStateFlow(ImageCacheManager.CacheInfo())
    val cacheInfo: StateFlow<ImageCacheManager.CacheInfo> = _cacheInfo.asStateFlow()
    
    private val _isPreloading = MutableStateFlow(false)
    val isPreloading: StateFlow<Boolean> = _isPreloading.asStateFlow()
    
    init {
        loadCacheInfo()
    }
    
    /**
     * Get the Coil ImageLoader instance
     */
    fun getImageLoader(): ImageLoader = imageCacheManager.imageLoader
    
    /**
     * Preload first images from attractions
     */
    fun preloadAttractionImages(imageUrls: List<String>) {
        if (imageUrls.isEmpty()) return
        
        viewModelScope.launch {
            try {
                _isPreloading.value = true
                Timber.d("🚀 Starting preload of ${imageUrls.size} images")
                
                // Preload images in parallel
                imageCacheManager.preloadImages(imageUrls)
                
                Timber.d("✅ Completed preloading images")
                loadCacheInfo() // Update cache info after preloading
            } catch (e: Exception) {
                Timber.e(e, "Error preloading images")
            } finally {
                _isPreloading.value = false
            }
        }
    }
    
    /**
     * Check if image is cached
     */
    suspend fun isImageCached(url: String): Boolean {
        return imageCacheManager.isImageCached(url)
    }
    
    /**
     * Clear all image cache
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                Timber.d("🗑️ Clearing all image cache")
                imageCacheManager.clearAllCache()
                loadCacheInfo()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing cache")
            }
        }
    }
    
    /**
     * Clear cache if JSON version changed
     */
    fun checkAndClearCacheIfNeeded(jsonVersion: String) {
        viewModelScope.launch {
            if (imageCacheManager.shouldClearCache(jsonVersion)) {
                Timber.d("🔄 JSON version changed, clearing cache")
                imageCacheManager.clearAllCache()
                imageCacheManager.updateCacheVersion(jsonVersion)
                loadCacheInfo()
            } else {
                Timber.d("✅ Cache version matches, no clearing needed")
            }
        }
    }
    
    /**
     * Load cache information
     */
    private fun loadCacheInfo() {
        viewModelScope.launch {
            try {
                val info = imageCacheManager.getCacheInfo()
                _cacheInfo.value = info
                
                Timber.d("""
                    📊 Cache Info:
                    Memory: ${info.memorySizeMB}MB / ${info.memoryMaxSizeBytes / (1024 * 1024)}MB (${info.memoryUsagePercent}%)
                    Disk: ${info.diskSizeMB}MB / ${info.diskMaxSizeBytes / (1024 * 1024)}MB (${info.diskUsagePercent}%)
                """.trimIndent())
            } catch (e: Exception) {
                Timber.e(e, "Error loading cache info")
            }
        }
    }
    
    /**
     * Get formatted cache size string
     */
    fun getFormattedCacheSize(): String {
        val info = _cacheInfo.value
        val totalSizeMB = info.memorySizeMB + info.diskSizeMB
        return String.format("%.1f MB", totalSizeMB)
    }
}
