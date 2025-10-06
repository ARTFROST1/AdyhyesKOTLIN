package com.adygyes.app.presentation.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adygyes.app.R
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.ui.components.*
import com.adygyes.app.presentation.viewmodel.AttractionDetailViewModel

/**
 * Detailed attraction information screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionDetailScreen(
    attractionId: String,
    onBackClick: () -> Unit,
    onBuildRoute: () -> Unit,
    onShareClick: () -> Unit,
    onShowOnMap: (() -> Unit)? = null,
    viewModel: AttractionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPhotoViewer by remember { mutableStateOf(false) }
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(attractionId) {
        viewModel.loadAttraction(attractionId)
    }
    
    // Handle back gesture - close photo viewer if open, otherwise navigate back
    BackHandler(enabled = true) {
        if (showPhotoViewer) {
            showPhotoViewer = false
        } else {
            onBackClick()
        }
    }
    
    when (val state = uiState) {
        is AttractionDetailViewModel.UiState.Loading -> {
            LoadingIndicator()
        }
        
        is AttractionDetailViewModel.UiState.Error -> {
            ErrorState(
                message = state.message,
                onRetry = { viewModel.loadAttraction(attractionId) }
            )
        }
        
        is AttractionDetailViewModel.UiState.Success -> {
            val attraction = state.attraction
            
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onShareClick) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = stringResource(R.string.cd_share)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleFavorite() }
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
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                bottomBar = {
                    // Bottom action bar
                    Surface(
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.PaddingLarge),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Show on Map button (only in list mode)
                            if (onShowOnMap != null) {
                                OutlinedButton(
                                    onClick = onShowOnMap,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.detail_show_on_map))
                                }
                            }
                            
                            // Get Directions button
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
                                Text(stringResource(R.string.detail_get_directions))
                            }
                        }
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    // Photo Gallery
                    item {
                        PhotoGallery(
                            images = attraction.images,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(bottom = 4.dp),
                            onImageClick = { index ->
                                selectedPhotoIndex = index
                                showPhotoViewer = true
                            },
                            onFullscreenClick = {
                                selectedPhotoIndex = 0
                                showPhotoViewer = true
                            }
                        )
                    }
                    
                    // Main content
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.PaddingLarge)
                        ) {
                            // Title and category
                            Text(
                                text = attraction.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CategoryChip(category = attraction.category)
                                attraction.rating?.let { rating ->
                                    RatingBar(
                                        rating = rating,
                                        totalReviews = state.reviewCount
                                    )
                                }
                            }
                            
                            // Description
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.detail_about),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = attraction.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Information cards
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = Dimensions.PaddingLarge),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Location card
                            attraction.location.address?.let { address ->
                                InfoCard(
                                    icon = Icons.Default.LocationOn,
                                    title = stringResource(R.string.detail_location),
                                    content = {
                                        Text(
                                            text = address,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        attraction.location.directions?.let { directions ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = directions,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                )
                            }
                            
                            // Working hours card
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
                            
                            // Price card
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
                            
                            // Contact info card
                            attraction.contactInfo?.let { contact ->
                                if (contact.phone != null || contact.email != null || contact.website != null) {
                                    InfoCard(
                                        icon = Icons.Default.ContactPhone,
                                        title = stringResource(R.string.detail_contact_info),
                                        content = {
                                            ClickableContactInfo(
                                                contactInfo = contact,
                                                compact = false
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Amenities
                    if (attraction.amenities.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = Dimensions.PaddingLarge,
                                    vertical = Dimensions.PaddingMedium
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.detail_amenities),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                AmenitiesGrid(amenities = attraction.amenities)
                            }
                        }
                    }
                    
                    // Tags
                    if (attraction.tags.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = Dimensions.PaddingLarge,
                                    vertical = Dimensions.PaddingMedium
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.detail_tags),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TagsFlow(tags = attraction.tags)
                            }
                        }
                    }
                }
            }
            
            // Photo viewer dialog
            if (showPhotoViewer) {
                PhotoViewer(
                    images = attraction.images,
                    initialPage = selectedPhotoIndex,
                    onDismiss = { showPhotoViewer = false },
                    onShare = { imageUrl ->
                        // Handle image sharing
                        onShareClick()
                    }
                )
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
                .padding(Dimensions.PaddingMedium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmenitiesGrid(
    amenities: List<String>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        amenities.forEach { amenity ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(red = 1.0f, green = 0.843f, blue = 0.0f, alpha = 1.0f)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = getAmenityIcon(amenity),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                    Text(
                        text = amenity.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsFlow(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        tags.forEach { tag ->
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

private fun getAmenityIcon(amenity: String): ImageVector {
    return when (amenity.lowercase()) {
        "parking", "парковка" -> Icons.Default.LocalParking
        "wifi", "wi-fi" -> Icons.Default.Wifi
        "restaurant", "ресторан", "кафе", "cafe" -> Icons.Default.Restaurant
        "toilet", "туалет", "restroom" -> Icons.Default.Wc
        "shop", "магазин", "store" -> Icons.Default.ShoppingCart
        "guide", "гид", "экскурсия" -> Icons.Default.Person
        "photo", "фото", "photography" -> Icons.Default.PhotoCamera
        "disabled", "инвалиды", "accessibility" -> Icons.Default.Accessible
        else -> Icons.Default.CheckCircle
    }
}