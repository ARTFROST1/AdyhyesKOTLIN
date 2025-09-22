package com.adygyes.app.presentation.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adygyes.app.R
import com.adygyes.app.presentation.theme.Dimensions

/**
 * Main map screen placeholder
 * Will be implemented in Stage 2 with Yandex MapKit integration
 */
@Composable
fun MapScreen(
    onAttractionClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeExtraLarge),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.nav_map),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Map functionality will be implemented in Stage 2",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // Temporary button for testing navigation
            Button(
                onClick = onSearchClick,
                modifier = Modifier.padding(top = Dimensions.SpacingLarge)
            ) {
                Text(stringResource(R.string.nav_search))
            }
        }
    }
}
