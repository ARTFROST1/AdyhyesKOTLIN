package com.adygyes.app.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                .padding(horizontal = Dimensions.PaddingLarge)
                .padding(bottom = Dimensions.PaddingLarge)
        ) {
            // Image gallery
            PhotoGallery(
                images = attraction.images,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onImageClick = { onNavigateToDetail() }
            )
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            
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
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    CategoryChip(
                        category = attraction.category,
                        compact = true
                    )
                }
                
                // Favorite button
                IconButton(
                    onClick = onToggleFavorite
                ) {
                    Icon(
                        imageVector = if (attraction.isFavorite) 
                            Icons.Default.Favorite 
                        else 
                            Icons.Default.FavoriteBorder,
                        contentDescription = if (attraction.isFavorite) 
                            "Remove from favorites" 
                        else 
                            "Add to favorites",
                        tint = if (attraction.isFavorite) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Rating and distance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.SpacingSmall),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                attraction.rating?.let { rating ->
                    RatingBar(
                        rating = rating,
                        size = 16.dp
                    )
                }
                
                distance?.let { dist ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDistance(dist),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            attraction.workingHours?.let { hours ->
                InfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Working Hours",
                    value = hours
                )
            }
            
            attraction.priceInfo?.let { price ->
                InfoRow(
                    icon = Icons.Default.AttachMoney,
                    label = "Price",
                    value = price
                )
            }
            
            attraction.location.address?.let { address ->
                InfoRow(
                    icon = Icons.Default.Place,
                    label = "Address",
                    value = address
                )
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
                    Text("Route")
                }
                
                // Share button
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
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
                    Text("Details")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDistance(distanceInMeters: Float): String {
    return when {
        distanceInMeters < 1000 -> "${distanceInMeters.toInt()} m"
        else -> "%.1f km".format(distanceInMeters / 1000)
    }
}
