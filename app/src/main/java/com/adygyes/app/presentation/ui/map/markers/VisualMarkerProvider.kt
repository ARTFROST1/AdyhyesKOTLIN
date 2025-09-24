package com.adygyes.app.presentation.ui.map.markers

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.toArgb
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import timber.log.Timber
import java.net.URL
import kotlinx.coroutines.*

/**
 * Provides native MapKit markers with beautiful visual representation
 * These markers are perfectly synchronized with map coordinates
 */
class VisualMarkerProvider(
    private val mapView: MapView
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
        
        // Paint for background circle
        val backgroundPaint = Paint().apply {
            isAntiAlias = true
            color = getCategoryColor(attraction.category)
            style = Paint.Style.FILL
        }
        
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
        
        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)
        
        // Draw category emoji or image placeholder
        drawCategoryContent(canvas, attraction, centerX, centerY, radius)
        
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
    
    /**
     * Draw category emoji or icon in the center of marker
     */
    private fun drawCategoryContent(
        canvas: Canvas,
        attraction: Attraction,
        centerX: Float,
        centerY: Float,
        radius: Float
    ) {
        val emoji = getCategoryEmoji(attraction.category)
        
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = radius * 0.8f
            textAlign = Paint.Align.CENTER
        }
        
        // Center emoji vertically
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(emoji, 0, emoji.length, textBounds)
        val textHeight = textBounds.height()
        
        canvas.drawText(
            emoji,
            centerX,
            centerY + textHeight / 2f,
            textPaint
        )
    }
    
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
                // Load image from URL
                val url = URL(imageUrl)
                val bitmap = BitmapFactory.decodeStream(url.openStream())
                
                withContext(Dispatchers.Main) {
                    // Create circular image with loaded bitmap
                    val circularBitmap = createCircularBitmapWithImage(
                        bitmap = bitmap,
                        attraction = attraction,
                        isSelected = isSelected
                    )
                    
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
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, sizePx, sizePx, true)
        paint.alpha = 255 // Ensure full opacity
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
        
        // Draw border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidthPx.toFloat()
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        return output
    }
    
    /**
     * Get category color
     */
    private fun getCategoryColor(category: AttractionCategory): Int {
        return when (category) {
            AttractionCategory.NATURE -> Color.parseColor("#4CAF50")
            AttractionCategory.CULTURE -> Color.parseColor("#9C27B0")
            AttractionCategory.HISTORY -> Color.parseColor("#795548")
            AttractionCategory.ADVENTURE -> Color.parseColor("#FF5722")
            AttractionCategory.RECREATION -> Color.parseColor("#03A9F4")
            AttractionCategory.GASTRONOMY -> Color.parseColor("#FF9800")
            AttractionCategory.RELIGIOUS -> Color.parseColor("#607D8B")
            AttractionCategory.ENTERTAINMENT -> Color.parseColor("#E91E63")
        }
    }
    
    /**
     * Get category emoji
     */
    private fun getCategoryEmoji(category: AttractionCategory): String {
        return when (category) {
            AttractionCategory.NATURE -> "🌲"
            AttractionCategory.CULTURE -> "🎭"
            AttractionCategory.HISTORY -> "🏛️"
            AttractionCategory.ADVENTURE -> "⛰️"
            AttractionCategory.RECREATION -> "🏖️"
            AttractionCategory.GASTRONOMY -> "🍴"
            AttractionCategory.RELIGIOUS -> "⛪"
            AttractionCategory.ENTERTAINMENT -> "🎪"
        }
    }
    
    /**
     * Clear all markers from the map
     */
    fun clearMarkers() {
        markers.clear()
        mapObjectCollection.clear()
        // Cancel existing coroutines and create new scope
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }
}
