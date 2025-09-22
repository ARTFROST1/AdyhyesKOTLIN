package com.adygyes.app.presentation.ui.screens.map

import androidx.compose.runtime.Composable

/**
 * Main map screen with Yandex MapKit integration
 */
@Composable
fun MapScreen(
    onAttractionClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    // Use the Yandex MapKit implementation
    MapScreenWithYandex(
        onAttractionClick = onAttractionClick,
        onSearchClick = onSearchClick
    )
}
