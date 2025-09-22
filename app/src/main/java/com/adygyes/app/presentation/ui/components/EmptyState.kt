package com.adygyes.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Empty state component for lists and screens
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Icon
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // Action button
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

/**
 * Specific empty states for different scenarios
 */
@Composable
fun NoFavoritesState(
    onExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "No Favorites Yet",
        message = "Start exploring and add your favorite attractions to see them here",
        icon = Icons.Default.FavoriteBorder,
        actionLabel = "Explore Attractions",
        onAction = onExplore,
        modifier = modifier
    )
}

@Composable
fun NoSearchResultsState(
    query: String,
    onClearFilters: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "No Results Found",
        message = "We couldn't find any attractions matching \"$query\".\nTry adjusting your search or filters.",
        icon = Icons.Default.SearchOff,
        actionLabel = if (onClearFilters != null) "Clear Filters" else null,
        onAction = onClearFilters,
        modifier = modifier
    )
}

@Composable
fun NoConnectionState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "No Internet Connection",
        message = "Please check your connection and try again",
        icon = Icons.Default.WifiOff,
        actionLabel = "Retry",
        onAction = onRetry,
        modifier = modifier
    )
}

@Composable
fun ErrorState(
    message: String = "Something went wrong",
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "Oops!",
        message = message,
        icon = Icons.Default.ErrorOutline,
        actionLabel = "Try Again",
        onAction = onRetry,
        modifier = modifier
    )
}

@Composable
fun LocationDisabledState(
    onEnableLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "Location Services Disabled",
        message = "Enable location services to see nearby attractions and get directions",
        icon = Icons.Default.LocationOff,
        actionLabel = "Enable Location",
        onAction = onEnableLocation,
        modifier = modifier
    )
}
