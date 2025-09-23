package com.adygyes.app.presentation.ui.screens.map

import android.graphics.*
import com.adygyes.app.domain.model.WaypointType
import com.yandex.runtime.image.ImageProvider

/**
 * Provides waypoint markers for tourist trails
 */
object WaypointMarkerProvider {
    
    /**
     * Get marker ImageProvider for a specific waypoint type
     */
    fun getWaypointMarker(
        waypointType: WaypointType,
        isDarkTheme: Boolean
    ): ImageProvider {
        return WaypointMarkerImageProvider(waypointType, isDarkTheme)
    }
    
    /**
     * Custom ImageProvider for waypoint markers
     */
    private class WaypointMarkerImageProvider(
        private val waypointType: WaypointType,
        private val isDarkTheme: Boolean
    ) : ImageProvider() {
        
        override fun getId(): String = "waypoint_marker_${waypointType.name}_$isDarkTheme"
        
        override fun getImage(): Bitmap {
            val size = 64
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            val paint = Paint().apply {
                isAntiAlias = true
            }
            
            // Get color based on waypoint type
            val backgroundColor = getWaypointColor(waypointType, isDarkTheme)
            val borderColor = if (isDarkTheme) Color.parseColor("#212121") else Color.WHITE
            
            // Draw circle background
            paint.apply {
                color = backgroundColor
                style = Paint.Style.FILL
            }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)
            
            // Draw border
            paint.apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)
            
            // Draw waypoint icon/emoji
            drawWaypointIcon(canvas, waypointType, size, isDarkTheme)
            
            return bitmap
        }
        
        private fun getWaypointColor(waypointType: WaypointType, isDarkTheme: Boolean): Int {
            val colorHex = when (waypointType) {
                WaypointType.START -> if (isDarkTheme) "#66BB6A" else "#4CAF50"
                WaypointType.FINISH -> if (isDarkTheme) "#EF5350" else "#F44336"
                WaypointType.VIEWPOINT -> if (isDarkTheme) "#29B6F6" else "#03A9F4"
                WaypointType.REST_AREA -> if (isDarkTheme) "#FFB74D" else "#FF9800"
                WaypointType.WATER_SOURCE -> if (isDarkTheme) "#4FC3F7" else "#00BCD4"
                WaypointType.DANGER -> if (isDarkTheme) "#FF7043" else "#FF5722"
                WaypointType.PHOTO_SPOT -> if (isDarkTheme) "#AB47BC" else "#9C27B0"
                WaypointType.LANDMARK -> if (isDarkTheme) "#8D6E63" else "#795548"
            }
            return Color.parseColor(colorHex)
        }
        
        private fun drawWaypointIcon(
            canvas: Canvas,
            waypointType: WaypointType,
            size: Int,
            isDarkTheme: Boolean
        ) {
            val paint = Paint().apply {
                color = if (isDarkTheme) Color.parseColor("#212121") else Color.WHITE
                textSize = 24f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            
            // Draw emoji/symbol for waypoint type
            val symbol = waypointType.icon
            
            // Draw symbol in center of circle
            val x = size / 2f
            val y = size / 2f + 8
            canvas.drawText(symbol, x, y, paint)
        }
    }
}
