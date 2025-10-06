package com.adygyes.app.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawWithContent
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
import com.adygyes.app.presentation.theme.getOverlayTextColor
import com.adygyes.app.presentation.theme.getOverlayTextColorWithAlpha
import com.adygyes.app.presentation.theme.getOverlayIconTint
import com.adygyes.app.presentation.theme.getContentIconTint
import com.adygyes.app.presentation.theme.getSecondaryTextColor
import com.adygyes.app.presentation.theme.getContentTextColor

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
    val hasImage = attraction.images.isNotEmpty()
    val imageUrl = attraction.images.firstOrNull()
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            if (hasImage && imageUrl != null) {
                // Image with gradient overlay
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = attraction.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay for image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 100f
                            )
                        )
                )
            } else {
                // Solid background when no image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

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
                    if (!compactForFavorites) {
                        CategoryChip(
                            category = attraction.category,
                            modifier = Modifier.padding(top = Dimensions.PaddingExtraSmall)
                        )
                    } else {
                        // Пустое место в компактном режиме, чтобы кнопка была справа
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(if (compactForFavorites) 44.dp else 40.dp)
                            .offset(x = 8.dp, y = (-8).dp)
                    ) {
                        AnimatedContent(
                            targetState = attraction.isFavorite,
                            transitionSpec = {
                                scaleIn(
                                    animationSpec = androidx.compose.animation.core.spring(
                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                    )
                                ) togetherWith scaleOut(
                                    animationSpec = androidx.compose.animation.core.tween(150)
                                )
                            }
                        ) { isFavorite ->
                            Box(
                                modifier = Modifier
                                    .size(if (compactForFavorites) 36.dp else 32.dp)
                                    .then(
                                        if (!isFavorite) {
                                            Modifier.background(
                                                Color.Black.copy(alpha = 0.3f),
                                                RoundedCornerShape(50)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFavorite) stringResource(R.string.cd_remove_from_favorites) else stringResource(
                                        R.string.cd_add_to_favorites
                                    ),
                                    tint = if (isFavorite) Color(0xFF0C5329) else if (hasImage) getOverlayIconTint() else getContentIconTint(), // Зеленый цвет для избранного
                                    modifier = Modifier.size(if (compactForFavorites) 26.dp else 24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom content
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Тег над заголовком в компактном режиме
                    if (compactForFavorites) {
                        CategoryChip(
                            category = attraction.category,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    
                    Text(
                        text = attraction.name,
                        style = if (compactForFavorites) MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 22.sp // Уменьшенный line-height
                        ) else MaterialTheme.typography.headlineSmall,
                        color = if (hasImage) getOverlayTextColor() else getContentTextColor(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )

                    if (!compactForFavorites) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = attraction.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hasImage) getOverlayTextColorWithAlpha(0.9f) else getSecondaryTextColor(),
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
                                tint = if (hasImage) getOverlayIconTint() else getContentIconTint(),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = (attraction.location.address?.let {
                                    if (compactForFavorites) extractSettlement(it) else it
                                }
                                    ?: stringResource(R.string.detail_location)),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasImage) getOverlayTextColorWithAlpha(0.9f) else getSecondaryTextColor(),
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
                            color = if (hasImage) getOverlayTextColorWithAlpha(0.9f) else getSecondaryTextColor()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extract first sentence from description
 */
private fun getFirstSentence(description: String): String {
    val sentences = description.split(". ", "! ", "? ")
    return if (sentences.isNotEmpty()) {
        val firstSentence = sentences[0].trim()
        if (firstSentence.endsWith(".") || firstSentence.endsWith("!") || firstSentence.endsWith("?")) {
            firstSentence
        } else {
            "$firstSentence."
        }
    } else {
        description
    }
}

/**
 * Rating component for attraction cards
 */
@Composable
private fun AttractionRating(
    rating: Float?,
    showDistance: Boolean = false,
    distance: Float? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Rating with star (only show if rating exists)
        if (rating != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭐",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", rating),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Empty space if no rating
            Spacer(modifier = Modifier.width(1.dp))
        }

        // Distance if available
        if (showDistance && distance != null) {
            Text(
                text = formatDistanceForCard(distance),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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