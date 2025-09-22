package com.adygyes.app.presentation.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.adygyes.app.R
import com.adygyes.app.presentation.theme.Dimensions

/**
 * Attraction detail screen placeholder
 * Will be implemented in Stage 4
 */
@Composable
fun AttractionDetailScreen(
    attractionId: String,
    onBackClick: () -> Unit,
    onPhotoClick: (Int) -> Unit,
    onBuildRoute: () -> Unit,
    onShareClick: () -> Unit
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
                imageVector = Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeExtraLarge),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Attraction Details",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Attraction ID: $attractionId",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Detail view will be implemented in Stage 4",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(top = Dimensions.SpacingLarge)
            ) {
                Text("Go Back")
            }
        }
    }
}