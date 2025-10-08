package com.adygyes.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import com.vanniktech.emoji.EmojiTextView
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adygyes.app.R
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.viewmodel.MapViewModel

/**
 * Combined component that displays search results header and category carousel
 * in a single container with transparent background
 */
@Composable
fun SearchResultsWithCategories(
    attractionsCount: Int,
    searchQuery: String,
    selectedCategories: Set<String>,
    selectedFilter: MapViewModel.CategoryFilter,
    onFilterSelected: (MapViewModel.CategoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true, // Always show the container so users can change filters
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.PaddingMedium),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.PaddingMedium)
            ) {
                // Search results header (first)
                AnimatedContent(
                    targetState = attractionsCount,
                    transitionSpec = {
                        fadeIn() + slideInVertically() togetherWith 
                        fadeOut() + slideOutVertically()
                    }
                ) { count ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category carousel (second) - with no-clip system
                NoClipCategoryCarousel(
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected
                )
            }
        }
    }
}

/**
 * Category carousel with scrolling support
 */
@Composable
private fun NoClipCategoryCarousel(
    selectedFilter: MapViewModel.CategoryFilter,
    onFilterSelected: (MapViewModel.CategoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    // Create list of all filter options
    val allFilters = buildList {
        add(MapViewModel.CategoryFilter.All to stringResource(R.string.all_categories))
        add(MapViewModel.CategoryFilter.Favorites to stringResource(R.string.nav_favorites))
        AttractionCategory.values().forEach { category ->
            add(MapViewModel.CategoryFilter.Category(category) to category.displayName)
        }
    }
    
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp) // No extra padding since we're inside a container
    ) {
        items(allFilters) { (filter, label) ->
            CategoryFilterChip(
                label = label,
                isSelected = when (filter) {
                    is MapViewModel.CategoryFilter.All -> selectedFilter is MapViewModel.CategoryFilter.All
                    is MapViewModel.CategoryFilter.Favorites -> selectedFilter is MapViewModel.CategoryFilter.Favorites
                    is MapViewModel.CategoryFilter.Category -> 
                        selectedFilter is MapViewModel.CategoryFilter.Category && 
                        selectedFilter.category == filter.category
                },
                onClick = { onFilterSelected(filter) },
                leadingIcon = when (filter) {
                    is MapViewModel.CategoryFilter.Favorites -> {
                        {
                            AndroidView(
                                factory = { context ->
                                    EmojiTextView(context).apply {
                                        text = "❤️" // Apple-style heart emoji
                                        textSize = 14f
                                        gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.START
                                        includeFontPadding = false
                                        setPadding(0, 0, 8, 0) // Reduced padding for better alignment
                                    }
                                },
                                modifier = Modifier
                                    .size(20.dp)
                                    .wrapContentHeight(Alignment.CenterVertically)
                            )
                        }
                    }
                    is MapViewModel.CategoryFilter.Category -> {
                        {
                            AndroidView(
                                factory = { context ->
                                    EmojiTextView(context).apply {
                                        text = filter.category.emoji
                                        textSize = 14f // bodyMedium size
                                        gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.START
                                        includeFontPadding = false
                                        setPadding(0, 0, 8, 0) // Reduced padding for better alignment
                                    }
                                },
                                modifier = Modifier
                                    .size(20.dp)
                                    .wrapContentHeight(Alignment.CenterVertically)
                            )
                        }
                    }
                    else -> null
                }
            )
        }
    }
}

/**
 * Individual filter chip component with consistent styling
 */
@Composable
private fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.invoke()
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = contentColor
            )
        }
    }
}
