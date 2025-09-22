package com.adygyes.app.presentation.ui.screens.map

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.yandex.mapkit.map.MapType
import com.yandex.mapkit.mapview.MapView

/**
 * Provides map styling based on the current theme
 */
object MapStyleProvider {
    
    /**
     * Apply theme-based styling to the map
     */
    fun applyMapStyle(mapView: MapView, isDarkTheme: Boolean) {
        try {
            // Set map type based on theme
            mapView.map.mapType = if (isDarkTheme) {
                MapType.VECTOR_MAP
            } else {
                MapType.VECTOR_MAP
            }
            
            // Apply night mode for dark theme
            mapView.map.isNightModeEnabled = isDarkTheme
            
            // Set map background color
            val backgroundColor = if (isDarkTheme) {
                android.graphics.Color.parseColor("#121212")
            } else {
                android.graphics.Color.parseColor("#FAFAFA")
            }
            
            // Additional styling can be added here
            // For example, custom map styles, layer visibility, etc.
            
        } catch (e: Exception) {
            // Handle styling errors gracefully
        }
    }
    
    /**
     * Get marker color based on category and theme
     */
    fun getMarkerColor(category: String, isDarkTheme: Boolean): Int {
        return when (category) {
            "NATURE" -> if (isDarkTheme) {
                android.graphics.Color.parseColor("#66BB6A")
            } else {
                android.graphics.Color.parseColor("#4CAF50")
            }
            "CULTURE" -> if (isDarkTheme) {
                android.graphics.Color.parseColor("#AB47BC")
            } else {
                android.graphics.Color.parseColor("#9C27B0")
            }
            "HISTORY" -> if (isDarkTheme) {
                android.graphics.Color.parseColor("#8D6E63")
            } else {
                android.graphics.Color.parseColor("#795548")
            }
            "ADVENTURE" -> if (isDarkTheme) {
                android.graphics.Color.parseColor("#FF7043")
            } else {
                android.graphics.Color.parseColor("#FF5722")
            }
            "RECREATION" -> if (isDarkTheme) {
                android.graphics.Color.parseColor("#29B6F6")
            } else {
                android.graphics.Color.parseColor("#03A9F4")
            }
            "GASTRONOMY" -> if (isDarkTheme) {
                android.graphics.Color.parseColor("#FFB74D")
            } else {
                android.graphics.Color.parseColor("#FF9800")
            }
            else -> if (isDarkTheme) {
                android.graphics.Color.parseColor("#90A4AE")
            } else {
                android.graphics.Color.parseColor("#607D8B")
            }
        }
    }
    
    /**
     * Configure map interaction settings
     */
    fun configureMapInteraction(mapView: MapView) {
        mapView.map.apply {
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
            
            // Enable fast tap response
            isFastTapEnabled = true
        }
    }
}
