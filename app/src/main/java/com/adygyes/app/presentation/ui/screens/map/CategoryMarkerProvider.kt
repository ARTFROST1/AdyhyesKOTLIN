package com.adygyes.app.presentation.ui.screens.map

import android.content.Context
import android.graphics.*
import com.adygyes.app.domain.model.AttractionCategory
import com.yandex.runtime.image.ImageProvider

/**
 * Provides category-specific marker icons for map
 */
object CategoryMarkerProvider {
    
    /**
     * Get marker ImageProvider for a specific category
     */
    fun getMarkerForCategory(
        context: Context,
        category: AttractionCategory,
        isDarkTheme: Boolean
    ): ImageProvider {
        return CategoryMarkerImageProvider(category, isDarkTheme)
    }
    
    /**
     * Custom ImageProvider for category markers
     */
    private class CategoryMarkerImageProvider(
        private val category: AttractionCategory,
        private val isDarkTheme: Boolean
    ) : ImageProvider() {
        
        override fun getId(): String = "category_marker_${category.name}_$isDarkTheme"
        
        override fun getImage(): Bitmap {
            val width = 96
            val height = 120
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            val paint = Paint().apply {
                isAntiAlias = true
            }
            
            // Get color based on category
            val color = getCategoryColor(category, isDarkTheme)
            
            // Draw pin shape
            val path = Path().apply {
                // Top circle
                addCircle(width / 2f, width / 2f, width / 2f - 4, Path.Direction.CW)
                // Bottom triangle (pin point)
                moveTo(width / 2f - 20, width / 2f + 10)
                lineTo(width / 2f, height - 4f)
                lineTo(width / 2f + 20, width / 2f + 10)
                close()
            }
            
            // Fill pin
            paint.apply {
                this.color = color
                style = Paint.Style.FILL
            }
            canvas.drawPath(path, paint)
            
            // Draw border
            paint.apply {
                this.color = if (isDarkTheme) Color.parseColor("#212121") else Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawPath(path, paint)
            
            // Draw category icon or text in center
            drawCategoryIcon(canvas, category, width, isDarkTheme)
            
            return bitmap
        }
        
        private fun getCategoryColor(category: AttractionCategory, isDarkTheme: Boolean): Int {
            val colorHex = when (category) {
                AttractionCategory.NATURE -> if (isDarkTheme) "#66BB6A" else "#4CAF50"
                AttractionCategory.CULTURE -> if (isDarkTheme) "#AB47BC" else "#9C27B0"
                AttractionCategory.HISTORY -> if (isDarkTheme) "#8D6E63" else "#795548"
                AttractionCategory.ADVENTURE -> if (isDarkTheme) "#FF7043" else "#FF5722"
                AttractionCategory.RECREATION -> if (isDarkTheme) "#29B6F6" else "#03A9F4"
                AttractionCategory.GASTRONOMY -> if (isDarkTheme) "#FFB74D" else "#FF9800"
                AttractionCategory.RELIGIOUS -> if (isDarkTheme) "#90A4AE" else "#607D8B"
                AttractionCategory.ENTERTAINMENT -> if (isDarkTheme) "#EC407A" else "#E91E63"
            }
            return Color.parseColor(colorHex)
        }
        
        private fun drawCategoryIcon(canvas: Canvas, category: AttractionCategory, size: Int, isDarkTheme: Boolean) {
            val paint = Paint().apply {
                color = if (isDarkTheme) Color.parseColor("#212121") else Color.WHITE
                textSize = 28f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            
            // Get emoji/symbol for category
            val symbol = when (category) {
                AttractionCategory.NATURE -> "🌲"
                AttractionCategory.CULTURE -> "🎭"
                AttractionCategory.HISTORY -> "🏛️"
                AttractionCategory.ADVENTURE -> "⛰️"
                AttractionCategory.RECREATION -> "🏖️"
                AttractionCategory.GASTRONOMY -> "🍴"
                AttractionCategory.RELIGIOUS -> "⛪"
                AttractionCategory.ENTERTAINMENT -> "🎪"
            }
            
            // Draw symbol in center of circle
            val x = size / 2f
            val y = size / 2f + 10
            canvas.drawText(symbol, x, y, paint)
        }
    }
}
