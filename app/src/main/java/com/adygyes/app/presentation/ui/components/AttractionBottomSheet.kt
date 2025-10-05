package com.adygyes.app.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adygyes.app.R
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.presentation.theme.Dimensions
import kotlinx.coroutines.launch

/**
 * Bottom sheet for displaying attraction details on the map
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionBottomSheet(
    attraction: Attraction,
    onDismiss: () -> Unit,
    onNavigateToDetail: () -> Unit,
    onBuildRoute: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    distance: Float? = null
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.PaddingLarge)
                .padding(bottom = Dimensions.PaddingLarge)
        ) {
            // Image gallery
            PhotoGallery(
                images = attraction.images,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onImageClick = { 
                    // Close bottom sheet immediately and navigate
                    scope.launch {
                        sheetState.hide()
                        onNavigateToDetail()
                    }
                },
                onFullscreenClick = {
                    // Close bottom sheet immediately and navigate
                    scope.launch {
                        sheetState.hide()
                        onNavigateToDetail()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title and category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attraction.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                }
                
                // Favorite button (top right)
                IconButton(
                    onClick = onToggleFavorite
                ) {
                    Icon(
                        imageVector = if (attraction.isFavorite) 
                            Icons.Default.Favorite 
                        else 
                            Icons.Default.FavoriteBorder,
                        contentDescription = if (attraction.isFavorite) 
                            stringResource(R.string.cd_remove_from_favorites) 
                        else 
                            stringResource(R.string.cd_add_to_favorites),
                        tint = if (attraction.isFavorite) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category, rating and share button row (full width)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category chip on the left
                CategoryChip(
                    category = attraction.category,
                    compact = true
                )
                
                // Rating in the center
                attraction.rating?.let { rating ->
                    RatingBar(
                        rating = rating,
                        size = 16.dp
                    )
                }
                
                // Share button on the right (same size as favorite)
                IconButton(
                    onClick = onShare
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.detail_share),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Distance info
            distance?.let { dist ->
                Row(
                    modifier = Modifier.padding(vertical = Dimensions.SpacingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDistance(dist),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = Dimensions.SpacingMedium))
            
            // Description
            Text(
                text = attraction.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = Dimensions.SpacingMedium)
            )
            
            // Info items
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                attraction.workingHours?.let { hours ->
                    InfoCard(
                        icon = Icons.Default.Schedule,
                        title = stringResource(R.string.detail_working_hours),
                        content = {
                            Text(
                                text = hours,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
                
                attraction.priceInfo?.let { price ->
                    InfoCard(
                        icon = Icons.Default.AttachMoney,
                        title = stringResource(R.string.detail_price),
                        content = {
                            Text(
                                text = price,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
                
                attraction.location.address?.let { address ->
                    InfoCard(
                        icon = Icons.Default.Place,
                        title = stringResource(R.string.detail_address),
                        content = {
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
                
                // Контактная информация
                attraction.contactInfo?.let { contactInfo ->
                    InfoCard(
                        icon = Icons.Default.ContactPhone,
                        title = stringResource(R.string.detail_contact_info),
                        content = {
                            ClickableContactInfo(
                                contactInfo = contactInfo,
                                compact = true
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
            ) {
                // Build route button
                Button(
                    onClick = onBuildRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_route))
                }
                
                // Details button
                OutlinedButton(
                    onClick = onNavigateToDetail,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_details))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingMedium),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                content()
            }
        }
    }
}

fun formatDistance(distanceInMeters: Float): String {
    return when {
        distanceInMeters < 1000 -> "${distanceInMeters.toInt()} m"
        else -> "%.1f km".format(distanceInMeters / 1000)
    }
}
