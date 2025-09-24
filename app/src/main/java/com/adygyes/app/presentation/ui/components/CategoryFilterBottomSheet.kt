package com.adygyes.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adygyes.app.R
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.presentation.theme.Dimensions

/**
 * Bottom sheet for category filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterBottomSheet(
    selectedCategories: Set<AttractionCategory>,
    onCategoryToggle: (AttractionCategory) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingLarge)
                .padding(bottom = Dimensions.PaddingExtraLarge)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                TextButton(
                    onClick = onClearAll,
                    enabled = selectedCategories.isNotEmpty()
                ) {
                    Text(stringResource(R.string.clear_all))
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            
            // Categories
            LazyColumn {
                items(AttractionCategory.values()) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedCategories.contains(category),
                            onCheckedChange = { onCategoryToggle(category) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
            
            // Apply button
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.apply_filters))
            }
        }
    }
}
