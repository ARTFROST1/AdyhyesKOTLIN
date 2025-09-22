package com.adygyes.app.presentation.ui.screens.map

import android.content.Context
import android.graphics.*
import com.yandex.runtime.image.ImageProvider

/**
 * Custom ImageProvider for creating cluster icons with text
 */
class TextImageProvider(
    private val text: String,
    private val context: Context
) : ImageProvider() {
    
    override fun getId(): String = "cluster_$text"
    
    override fun getImage(): Bitmap {
        val size = 100
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw circle background
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#2E7D32") // Primary green
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)
        
        // Draw border
        paint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)
        
        // Draw text
        paint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = 36f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        
        // Calculate text position
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val x = size / 2f
        val y = size / 2f - (paint.descent() + paint.ascent()) / 2
        
        canvas.drawText(text, x, y, paint)
        
        return bitmap
    }
}
