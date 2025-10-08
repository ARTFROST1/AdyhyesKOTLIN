package com.adygyes.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Utility functions for adaptive colors based on theme
 */

/**
 * Returns appropriate text color for content over images with dark overlay
 * In light theme: White (for dark overlay)
 * In dark theme: White (for dark overlay)
 */
@Composable
fun getOverlayTextColor(): Color {
    // Always white for dark overlay regardless of theme
    return Color.White
}

/**
 * Returns appropriate text color for content over images with dark overlay (with alpha)
 * In light theme: White with alpha (for dark overlay)
 * In dark theme: White with alpha (for dark overlay)
 */
@Composable
fun getOverlayTextColorWithAlpha(alpha: Float = 0.9f): Color {
    // Always white with alpha for dark overlay regardless of theme
    return Color.White.copy(alpha = alpha)
}

/**
 * Returns appropriate text color for regular content
 * In light theme: Dark text
 * In dark theme: Light text
 */
@Composable
fun getContentTextColor(): Color {
    return MaterialTheme.colorScheme.onSurface
}

/**
 * Returns appropriate secondary text color for regular content
 * In light theme: Dark text with reduced opacity
 * In dark theme: Light text with reduced opacity
 */
@Composable
fun getSecondaryTextColor(): Color {
    return MaterialTheme.colorScheme.onSurfaceVariant
}

/**
 * Returns appropriate icon tint for regular content
 * In light theme: Dark tint
 * In dark theme: Light tint
 */
@Composable
fun getContentIconTint(): Color {
    return MaterialTheme.colorScheme.onSurface
}

/**
 * Returns appropriate icon tint for content over images with dark overlay
 * Always white regardless of theme
 */
@Composable
fun getOverlayIconTint(): Color {
    return Color.White
}

/**
 * Determines if a color is dark based on luminance
 */
fun isColorDark(color: Color): Boolean {
    val darkness = 1 - (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return darkness >= 0.5
}

/**
 * Returns appropriate text color based on background color
 * If background is dark: White text
 * If background is light: Black text
 */
fun getContrastingTextColor(backgroundColor: Color): Color {
    return if (isColorDark(backgroundColor)) Color.White else Color.Black
}
