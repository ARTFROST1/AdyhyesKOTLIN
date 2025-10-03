package com.adygyes.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.domain.model.Location
import com.adygyes.app.domain.model.ContactInfo

/**
 * Compact attraction card for search results panel.
 * More minimalistic than the full AttractionCard.
 */
@Composable
fun CompactAttractionCard(
    attraction: Attraction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular image
            AsyncImage(
                model = attraction.images.firstOrNull(),
                contentDescription = attraction.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        getCategoryColor(attraction.category).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Name
                Text(
                    text = attraction.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Category and rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category
                    Text(
                        text = getCategoryName(attraction.category),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Rating if available
                    attraction.rating?.let { rating ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFFFFC107)
                            )
                            Text(
                                text = String.format("%.1f", rating),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryColor(category: AttractionCategory): Color = when (category) {
    AttractionCategory.NATURE -> Color(0xFF4CAF50)
    AttractionCategory.HISTORY -> Color(0xFF795548)
    AttractionCategory.CULTURE -> Color(0xFF9C27B0)
    AttractionCategory.ENTERTAINMENT -> Color(0xFFFF9800)
    AttractionCategory.RECREATION -> Color(0xFF2196F3)
    AttractionCategory.GASTRONOMY -> Color(0xFFFFC107)
    AttractionCategory.RELIGIOUS -> Color(0xFF607D8B)
    AttractionCategory.ADVENTURE -> Color(0xFFFF5722)
}

private fun getCategoryName(category: AttractionCategory): String = when (category) {
    AttractionCategory.NATURE -> "Природа"
    AttractionCategory.HISTORY -> "История"
    AttractionCategory.CULTURE -> "Культура"
    AttractionCategory.ENTERTAINMENT -> "Развлечения"
    AttractionCategory.RECREATION -> "Отдых"
    AttractionCategory.GASTRONOMY -> "Гастрономия"
    AttractionCategory.RELIGIOUS -> "Религия"
    AttractionCategory.ADVENTURE -> "Приключения"
}

@Preview
@Composable
fun CompactAttractionCardPreview() {
    MaterialTheme {
        CompactAttractionCard(
            attraction = Attraction(
                id = "1",
                name = "Хаджохская теснина",
                description = "Живописное ущелье реки Белой",
                category = AttractionCategory.NATURE,
                location = Location(
                    latitude = 44.2853,
                    longitude = 40.1742,
                    address = "п. Каменномостский, Майкопский район"
                ),
                images = listOf("https://example.com/image.jpg"),
                rating = 4.8f,
                workingHours = null,
                contactInfo = ContactInfo(
                    phone = "+7 (877) 777-77-77",
                    website = null
                ),
                priceInfo = "300 ₽",
                amenities = emptyList(),
                tags = emptyList()
            ),
            onClick = {}
        )
    }
}
