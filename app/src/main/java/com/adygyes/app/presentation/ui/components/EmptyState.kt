package com.adygyes.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adygyes.app.R

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
        title = stringResource(R.string.no_favorites_yet),
        message = stringResource(R.string.no_favorites_message),
        icon = Icons.Default.FavoriteBorder,
        actionLabel = stringResource(R.string.explore_attractions),
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
        title = stringResource(R.string.no_results_found),
        message = stringResource(R.string.no_results_message, query),
        icon = Icons.Default.SearchOff,
        actionLabel = if (onClearFilters != null) stringResource(R.string.clear_filters) else null,
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
        title = stringResource(R.string.no_internet_connection),
        message = stringResource(R.string.no_internet_message),
        icon = Icons.Default.WifiOff,
        actionLabel = stringResource(R.string.retry),
        onAction = onRetry,
        modifier = modifier
    )
}

@Composable
fun ErrorState(
    message: String? = null,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = stringResource(R.string.oops),
        message = message ?: stringResource(R.string.something_went_wrong),
        icon = Icons.Default.ErrorOutline,
        actionLabel = stringResource(R.string.try_again),
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
        title = stringResource(R.string.location_services_disabled),
        message = stringResource(R.string.location_services_message),
        icon = Icons.Default.LocationOff,
        actionLabel = stringResource(R.string.enable_location),
        onAction = onEnableLocation,
        modifier = modifier
    )
}
