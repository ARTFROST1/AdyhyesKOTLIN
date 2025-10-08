package com.adygyes.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import com.adygyes.app.presentation.viewmodel.MapViewModel

/**
 * Unified category carousel component that works for both Map and List view modes.
 * Displays a count header and horizontal scrollable category filters.
 * 
 * This component maintains consistent appearance and behavior across view modes,
 * ensuring seamless transitions between Map and List views.
 */
@Composable
fun UnifiedCategoryCarousel(
    attractionsCount: Int,
    searchQuery: String,
    selectedCategories: Set<String>,
    selectedFilter: MapViewModel.CategoryFilter,
    onFilterSelected: (MapViewModel.CategoryFilter) -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        // Use the same Surface styling as MapCategoryCarousel for consistency
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Category filters carousel (убрали заголовок "Всего мест")
                CategoryFiltersCarousel(
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected
                )
            }
        }
    }
}

/**
 * Internal carousel component for category filters
 */
@Composable
private fun CategoryFiltersCarousel(
    selectedFilter: MapViewModel.CategoryFilter,
    onFilterSelected: (MapViewModel.CategoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    // Build list of all filter options
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
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(allFilters) { (filter, label) ->
            CategoryFilterChip(
                label = label,
                isSelected = when (filter) {
                    is MapViewModel.CategoryFilter.All -> 
                        selectedFilter is MapViewModel.CategoryFilter.All
                    is MapViewModel.CategoryFilter.Favorites -> 
                        selectedFilter is MapViewModel.CategoryFilter.Favorites
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
