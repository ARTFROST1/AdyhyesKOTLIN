package com.adygyes.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.adygyes.app.R
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.presentation.theme.Dimensions

enum class ListViewMode {
    LIST,
    GRID
}

/**
 * Reusable attractions list component for displaying search results
 * Can be used in both MapScreen (list view) and SearchScreen
 * Now supports both List and Grid view modes
 */
@Composable
fun AttractionsList(
    attractions: List<Attraction>,
    onAttractionClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    searchQuery: String = "",
    selectedCategories: Set<String> = emptySet(),
    emptyStateMessage: String? = null,
    showResultCount: Boolean = true,
    viewMode: ListViewMode = ListViewMode.LIST,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = Dimensions.PaddingMedium,
        vertical = Dimensions.PaddingMedium
    )
) {
    val listState = rememberLazyListState()
    
    Box(modifier = modifier) {
        when {
            isLoading -> {
                // Loading state with shimmer effect
                LoadingShimmerList(
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            attractions.isEmpty() -> {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimensions.PaddingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val message = when {
                        emptyStateMessage != null -> emptyStateMessage
                        searchQuery.isNotEmpty() && selectedCategories.isNotEmpty() -> 
                            stringResource(R.string.no_results_with_filters, searchQuery)
                        searchQuery.isNotEmpty() -> 
                            stringResource(R.string.no_results_for_query, searchQuery)
                        selectedCategories.isNotEmpty() -> 
                            stringResource(R.string.no_results_for_categories)
                        else -> stringResource(R.string.no_attractions_available)
                    }
                    
                    NoResultsState(
                        message = message,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            else -> {
                // Success state with attractions list or grid
                Column(modifier = Modifier.fillMaxSize()) {
                    // Result count header (shown for both view modes)
                    if (showResultCount) {
                        AnimatedContent(
                            targetState = attractions.size,
                            transitionSpec = {
                                fadeIn() + slideInVertically() togetherWith 
                                fadeOut() + slideOutVertically()
                            }
                        ) { count ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = Dimensions.PaddingMedium,
                                        vertical = Dimensions.PaddingSmall
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when {
                                        searchQuery.isNotEmpty() -> 
                                            stringResource(R.string.search_results_count, count)
                                        selectedCategories.isNotEmpty() -> 
                                            stringResource(R.string.filtered_results_count, count)
                                        else -> 
                                            stringResource(R.string.attractions_count, count)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                // Applied filters indicator
                                if (searchQuery.isNotEmpty() || selectedCategories.isNotEmpty()) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = buildString {
                                                var filterCount = 0
                                                if (searchQuery.isNotEmpty()) filterCount++
                                                filterCount += selectedCategories.size
                                                append("$filterCount ")
                                                append(if (filterCount == 1) "filter" else "filters")
                                            },
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Content based on view mode
                    when (viewMode) {
                        ListViewMode.LIST -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = contentPadding,
                                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                            ) {
                                // Attractions list items
                                items(
                                    items = attractions,
                                    key = { it.id }
                                ) { attraction ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        AttractionListItem(
                                            attraction = attraction,
                                            onClick = { onAttractionClick(attraction.id) },
                                            onFavoriteClick = { onFavoriteClick(attraction.id) },
                                            highlightQuery = searchQuery.takeIf { it.isNotEmpty() }
                                        )
                                    }
                                }
                                
                                // Bottom spacing for FAB
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                        
                        else -> { // GRID mode - точно как на экране избранного
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = contentPadding,
                                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
                                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
                            ) {
                                items(
                                    items = attractions,
                                    key = { it.id }
                                ) { attraction ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn() + scaleIn(),
                                        exit = fadeOut() + scaleOut()
                                    ) {
                                        AttractionCard(
                                            attraction = attraction,
                                            onClick = { onAttractionClick(attraction.id) },
                                            onFavoriteClick = { onFavoriteClick(attraction.id) },
                                            compactForFavorites = true // Точно как на экране избранного!
                                        )
                                    }
                                }
                                
                                // Bottom spacing for FAB (spanning 2 columns)
                                item(span = { GridItemSpan(2) }) {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact list item for attraction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionListItem(
    attraction: Attraction,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlightQuery: String? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attraction image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(attraction.images.firstOrNull() ?: "")
                    .crossfade(true)
                    .build(),
                contentDescription = attraction.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            
            Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))
            
            // Attraction details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name with optional highlighting
                Text(
                    text = attraction.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                
                // Category chip
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = attraction.category.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                // Rating
                attraction.rating?.let { rating ->
                    if (rating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RatingBar(
                                rating = rating,
                                size = 12.dp,
                                maxRating = 5
                            )
                            Text(
                                text = String.format("%.1f", rating),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Favorite button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (attraction.isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = if (attraction.isFavorite) {
                        stringResource(R.string.remove_from_favorites)
                    } else {
                        stringResource(R.string.add_to_favorites)
                    },
                    tint = if (attraction.isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * No results state for empty list
 */
@Composable
fun NoResultsState(
    message: String,
    modifier: Modifier = Modifier,
    action: (() -> Unit)? = null,
    actionLabel: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
    ) {
        // Icon or illustration would go here
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (action != null && actionLabel != null) {
            Button(
                onClick = action,
                modifier = Modifier.padding(top = Dimensions.SpacingMedium)
            ) {
                Text(actionLabel)
            }
        }
    }
}
