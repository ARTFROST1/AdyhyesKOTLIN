package com.adygyes.app.presentation.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.adygyes.app.R
import com.adygyes.app.presentation.theme.Dimensions

/**
 * Favorites screen placeholder
 * Will be implemented in Stage 4
 */
@Composable
fun FavoritesScreen(
    onAttractionClick: (String) -> Unit,
    onExploreClick: () -> Unit
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
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.IconSizeExtraLarge),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.nav_favorites),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.favorites_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimensions.PaddingLarge)
            )
            
            Button(
                onClick = onExploreClick,
                modifier = Modifier.padding(top = Dimensions.SpacingLarge)
            ) {
                Text(stringResource(R.string.favorites_explore))
            }
        }
    }
}