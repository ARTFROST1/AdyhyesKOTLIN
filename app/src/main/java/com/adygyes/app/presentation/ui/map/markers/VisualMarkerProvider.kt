package com.adygyes.app.presentation.ui.map.markers

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.adygyes.app.domain.model.Attraction
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import coil.request.ImageRequest
import com.adygyes.app.data.local.cache.ImageCacheManager
import timber.log.Timber
import kotlinx.coroutines.*

/**
 * Provides native MapKit markers with beautiful visual representation
 * These markers are perfectly synchronized with map coordinates
 */
class VisualMarkerProvider(
    private val mapView: MapView,
    private val imageCacheManager: ImageCacheManager
) {
    private val mapObjectCollection = mapView.map.mapObjects
    private val markers = mutableMapOf<String, PlacemarkMapObject>()
    private val markerImages = mutableMapOf<String, ImageProvider>()
    private var coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * Add visual markers for attractions
     */
    fun addVisualMarkers(attractions: List<Attraction>) {
        clearMarkers()
        
        attractions.forEach { attraction ->
            addVisualMarker(attraction)
        }
        
        Timber.d("📍 Added ${attractions.size} native visual markers")
    }

    /**
     * Incrementally sync visual markers with provided attractions list without full clear-add.
     * Keeps existing markers, removes those that are no longer needed, and adds only new ones.
     */
    fun updateVisualMarkers(attractions: List<Attraction>) {
        val desiredIds = attractions.map { it.id }.toSet()

        // Remove markers that are no longer present
        val toRemove = markers.keys.toSet() - desiredIds
        toRemove.forEach { id ->
            markers.remove(id)?.let { placemark ->
                mapObjectCollection.remove(placemark)
            }
        }

        // Add new markers that don't exist yet
        val currentIds = markers.keys
        attractions.forEach { attraction ->
            if (!currentIds.contains(attraction.id)) {
                addVisualMarker(attraction)
            }
        }

        Timber.d("📍 Synced native visual markers: now ${markers.size} (added ${desiredIds.size - currentIds.size}, removed ${toRemove.size})")
    }
    
    /**
     * Add a single visual marker
     */
    private fun addVisualMarker(attraction: Attraction) {
        val point = Point(
            attraction.location.latitude,
            attraction.location.longitude
        )
        
        // Create or get cached image provider
        val imageProvider = getOrCreateImageProvider(attraction)
        
        // Create native placemark
        val placemark = mapObjectCollection.addPlacemark(point, imageProvider)
        
        // IMPORTANT: Make placemark non-interactive to not block Compose clicks
        placemark.isVisible = true
        placemark.zIndex = 0f // Ensure it's at the bottom
        
        // Store reference
        markers[attraction.id] = placemark
        
        // Set user data for debugging (not for click handling)
        placemark.userData = attraction
    }
    
    /**
     * Update selected marker appearance
     */
    fun updateSelectedMarker(selectedAttraction: Attraction?) {
        markers.forEach { (id, marker) ->
            val isSelected = id == selectedAttraction?.id
            val attraction = marker.userData as? Attraction
            
            if (attraction != null) {
                // Update marker image based on selection state
                val imageProvider = getOrCreateImageProvider(
                    attraction = attraction,
                    isSelected = isSelected
                )
                marker.setIcon(imageProvider)
            }
        }
    }
    
    /**
     * Create or retrieve cached image provider for marker
     */
    private fun getOrCreateImageProvider(
        attraction: Attraction,
        isSelected: Boolean = false
    ): ImageProvider {
        val cacheKey = "${attraction.id}_${if (isSelected) "selected" else "normal"}"
        
        return markerImages.getOrPut(cacheKey) {
            createCircularMarkerImage(
                attraction = attraction,
                isSelected = isSelected
            )
        }
    }
    
    /**
     * Create a circular marker image with attraction photo or category icon
     */
    private fun createCircularMarkerImage(
        attraction: Attraction,
        isSelected: Boolean = false
    ): ImageProvider {
        val size = if (isSelected) 60 else 52 // dp converted to pixels
        val sizePx = (size * mapView.context.resources.displayMetrics.density).toInt()
        val borderWidth = if (isSelected) 3 else 2
        val borderWidthPx = (borderWidth * mapView.context.resources.displayMetrics.density).toInt()
        
        // Create bitmap
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Paint for border
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = borderWidthPx.toFloat()
        }
        
        // Paint for shadow
        val shadowPaint = Paint().apply {
            isAntiAlias = true
            color = Color.argb(50, 0, 0, 0)
            maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        }
        
        val centerX = sizePx / 2f
        val centerY = sizePx / 2f
        val radius = (sizePx - borderWidthPx) / 2f - 4f // Account for shadow
        
        // Draw shadow
        canvas.drawCircle(centerX + 2, centerY + 2, radius, shadowPaint)
        // Do NOT draw background fill or emoji — keep fully transparent until image loads
        
        // Draw border
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
        
        // Load actual image asynchronously if available
        attraction.images.firstOrNull()?.let { imageUrl ->
            coroutineScope.launch {
                loadAndUpdateMarkerImage(attraction, imageUrl, isSelected)
            }
        }
        
        return ImageProvider.fromBitmap(bitmap)
    }
    
    // No category emoji/content drawing — markers remain transparent until an image is loaded
    
    /**
     * Load image from URL and update marker
     */
    private suspend fun loadAndUpdateMarkerImage(
        attraction: Attraction,
        imageUrl: String,
        isSelected: Boolean
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Load image using Coil with caching
                // IMPORTANT: Disable hardware bitmaps for Canvas operations
                val request = ImageRequest.Builder(mapView.context)
                    .data(imageUrl)
                    .crossfade(false)
                    .allowHardware(false) // Critical: Canvas cannot draw hardware bitmaps
                    .build()
                
                val result = imageCacheManager.imageLoader.execute(request)
                val drawable = result.drawable
                
                // Convert drawable to bitmap
                val bitmap = when {
                    drawable is BitmapDrawable -> {
                        val originalBitmap = drawable.bitmap
                        // Check if it's a hardware bitmap and convert if needed
                        if (originalBitmap.config == Bitmap.Config.HARDWARE) {
                            // Convert hardware bitmap to software bitmap
                            val softwareBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, false)
                            softwareBitmap ?: originalBitmap // Use original if copy fails
                        } else {
                            originalBitmap
                        }
                    }
                    drawable != null -> {
                        // Create bitmap from drawable
                        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 100
                        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 100
                        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bmp)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bmp
                    }
                    else -> {
                        // Create placeholder bitmap if drawable is null
                        Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    // Create circular image with loaded bitmap
                    val circularBitmap = createCircularBitmapWithImage(
                        bitmap = bitmap,
                        attraction = attraction,
                        isSelected = isSelected
                    )
                    
                    // Log cache status
                    val isCached = imageCacheManager.isImageCached(imageUrl)
                    if (isCached) {
                        Timber.d("✅ Loaded marker image from cache for ${attraction.name}")
                    } else {
                        Timber.d("📥 Downloaded marker image for ${attraction.name}")
                    }
                    
                    // Update marker
                    val imageProvider = ImageProvider.fromBitmap(circularBitmap)
                    markers[attraction.id]?.setIcon(imageProvider)
                    
                    // Update cache
                    val cacheKey = "${attraction.id}_${if (isSelected) "selected" else "normal"}"
                    markerImages[cacheKey] = imageProvider
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load marker image for ${attraction.name}")
            }
        }
    }
    
    /**
     * Create circular bitmap with loaded image
     */
    private fun createCircularBitmapWithImage(
        bitmap: Bitmap,
        attraction: Attraction,
        isSelected: Boolean
    ): Bitmap {
        val size = if (isSelected) 60 else 52
        val sizePx = (size * mapView.context.resources.displayMetrics.density).toInt()
        val borderWidth = if (isSelected) 3 else 2
        val borderWidthPx = (borderWidth * mapView.context.resources.displayMetrics.density).toInt()
        
        val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        
        val paint = Paint().apply {
            isAntiAlias = true
            alpha = 255 // Full opacity
        }
        
        val centerX = sizePx / 2f
        val centerY = sizePx / 2f
        val radius = (sizePx - borderWidthPx) / 2f - 4f
        
        // Draw shadow
        paint.color = Color.argb(50, 0, 0, 0)
        paint.maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
        canvas.drawCircle(centerX + 2, centerY + 2, radius, paint)
        paint.maskFilter = null
        
        // Create circular clip path
        val path = android.graphics.Path().apply {
            addCircle(centerX, centerY, radius, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(path)
        
        // Draw scaled image with full opacity
        try {
            // Ensure we're not working with a hardware bitmap
            val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } else {
                bitmap
            }
            val scaledBitmap = Bitmap.createScaledBitmap(safeBitmap, sizePx, sizePx, true)
            paint.alpha = 255 // Ensure full opacity
            canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
            scaledBitmap.recycle() // Clean up scaled bitmap
        } catch (e: Exception) {
            // If image drawing fails, do not draw any colored fallback — keep transparent
            Timber.e(e, "Failed to draw image for marker ${attraction.name}")
        }
        
        // Draw border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidthPx.toFloat()
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        return output
    }
    
    // Removed category color/emoji helpers — no visual fallback is used anymore
    
    /**
     * Clear all markers from the map
     */
    fun clearMarkers() {
        markers.clear()
        markerImages.clear()
        mapObjectCollection.clear()
        // Cancel existing coroutines and create new scope
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }
}
