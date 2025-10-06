package com.adygyes.app.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * Horizontal carousel component for category filtering
 * Includes "All", "Favorites", and individual category chips
 */
@Composable
fun CategoryCarousel(
    selectedFilter: MapViewModel.CategoryFilter,
    onFilterSelected: (MapViewModel.CategoryFilter) -> Unit,
    modifier: Modifier = Modifier,
    withBackground: Boolean = false
) {
    val carouselModifier = if (withBackground) {
        modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp)
    } else {
        modifier.fillMaxWidth()
    }
    
    LazyRow(
        modifier = carouselModifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = Dimensions.PaddingMedium)
    ) {
        // "All" chip - always first
        item {
            CategoryFilterChip(
                label = stringResource(R.string.all_categories),
                isSelected = selectedFilter is MapViewModel.CategoryFilter.All,
                onClick = { onFilterSelected(MapViewModel.CategoryFilter.All) },
                leadingIcon = null
            )
        }
        
        // "Favorites" chip
        item {
            CategoryFilterChip(
                label = stringResource(R.string.nav_favorites),
                isSelected = selectedFilter is MapViewModel.CategoryFilter.Favorites,
                onClick = { onFilterSelected(MapViewModel.CategoryFilter.Favorites) },
                leadingIcon = {
                    AndroidView(
                        factory = { context ->
                            EmojiTextView(context).apply {
                                text = "❤️" // Apple-style heart emoji
                                textSize = 14f
                                setPadding(0, 0, 12, 0) // 4.dp end padding
                            }
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
        
        // Category chips
        items(AttractionCategory.values()) { category ->
            CategoryFilterChip(
                label = category.displayName,
                isSelected = selectedFilter is MapViewModel.CategoryFilter.Category && 
                           selectedFilter.category == category,
                onClick = { 
                    onFilterSelected(MapViewModel.CategoryFilter.Category(category))
                },
                leadingIcon = {
                    AndroidView(
                        factory = { context ->
                            EmojiTextView(context).apply {
                                text = category.emoji
                                textSize = 14f // bodyMedium size
                                setPadding(0, 0, 12, 0) // 4.dp end padding
                            }
                        },
                        modifier = Modifier.size(20.dp)
                    )
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
