package com.adygyes.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.adygyes.app.R
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.presentation.theme.Dimensions

/**
 * Reusable attraction card component
 */
@Composable
fun AttractionCard(
    attraction: Attraction,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDistance: Boolean = false,
    distance: Float? = null,
    compactForFavorites: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // Image with gradient overlay
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(attraction.images.firstOrNull() ?: "")
                    .crossfade(true)
                    .build(),
                contentDescription = attraction.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.PaddingMedium)
            ) {
                // Top row with category and favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    CategoryChip(
                        category = attraction.category,
                        modifier = Modifier.padding(top = Dimensions.PaddingExtraSmall)
                    )

                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(if (compactForFavorites) 44.dp else 40.dp)
                            .then(
                                if (compactForFavorites) Modifier
                                    .border(1.dp, Color.White, RoundedCornerShape(50))
                                else Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.9f),
                                        RoundedCornerShape(50)
                                    )
                            )
                    ) {
                        Icon(
                            imageVector = if (attraction.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (attraction.isFavorite) stringResource(R.string.cd_remove_from_favorites) else stringResource(
                                R.string.cd_add_to_favorites
                            ),
                            tint = if (compactForFavorites) MaterialTheme.colorScheme.primary else if (attraction.isFavorite) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(if (compactForFavorites) 28.dp else 24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom content
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = attraction.name,
                        style = if (compactForFavorites) MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 20.sp
                        ) else MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )

                    if (!compactForFavorites) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = attraction.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }


                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Location info
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = (attraction.location.address?.let {
                                    if (compactForFavorites) extractSettlement(it) else it
                                }
                                    ?: stringResource(R.string.detail_location)),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }

                    // Distance if available
                    if (showDistance && distance != null) {
                        Text(
                            text = formatDistanceForCard(distance),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extract settlement from address
 */
private fun extractSettlement(address: String): String {
    val parts = address.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    return if (parts.isNotEmpty()) parts.last() else address
}

/**
 * Format distance for display in attraction cards
 */
private fun formatDistanceForCard(distance: Float): String {
    return when {
        distance < 1000 -> "${distance.toInt()} м"
        distance < 10000 -> "${"%.1f".format(distance / 1000)} км"
        else -> "${(distance / 1000).toInt()} км"
    }
}