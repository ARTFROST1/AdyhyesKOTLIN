package com.adygyes.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adygyes.app.R
import com.adygyes.app.presentation.theme.Dimensions

/**
 * Header component that displays search results count and active filters
 * Used in MapScreen to show information above category carousel
 */
@Composable
fun SearchResultsHeader(
    attractionsCount: Int,
    searchQuery: String,
    selectedCategories: Set<String>,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = attractionsCount > 0 || searchQuery.isNotEmpty() || selectedCategories.isNotEmpty(),
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
            AnimatedContent(
                targetState = attractionsCount,
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
    }
}
