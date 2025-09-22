package com.adygyes.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adygyes.app.domain.model.AttractionCategory

/**
 * Colored chip component for displaying attraction categories
 */
@Composable
fun CategoryChip(
    category: AttractionCategory,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = Color(android.graphics.Color.parseColor(category.colorHex))
    val contentColor = if (isColorDark(backgroundColor)) Color.White else Color.Black
    
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
        onClick = onClick ?: {}
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 6.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.displayName,
                style = if (compact) {
                    MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                } else {
                    MaterialTheme.typography.labelMedium
                },
                color = contentColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Multi-select category filter chips
 */
@Composable
fun CategoryFilterChips(
    selectedCategories: Set<AttractionCategory>,
    onCategoryToggle: (AttractionCategory) -> Unit,
    modifier: Modifier = Modifier,
    showAll: Boolean = true
) {
    Column(modifier = modifier) {
        if (showAll) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AttractionCategory.values().forEach { category ->
                FilterChip(
                    selected = selectedCategories.contains(category),
                    onClick = { onCategoryToggle(category) },
                    enabled = true,
                    label = {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(android.graphics.Color.parseColor(category.colorHex)).copy(alpha = 0.2f),
                        selectedLabelColor = Color(android.graphics.Color.parseColor(category.colorHex))
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategories.contains(category),
                        borderColor = Color(android.graphics.Color.parseColor(category.colorHex)),
                        selectedBorderColor = Color(android.graphics.Color.parseColor(category.colorHex))
                    )
                )
            }
        }
    }
}

/**
 * Helper function to determine if a color is dark
 */
private fun isColorDark(color: Color): Boolean {
    val darkness = 1 - (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return darkness >= 0.5
}
