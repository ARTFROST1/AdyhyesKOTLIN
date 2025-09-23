package com.adygyes.app.presentation.ui.screens.map

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.adygyes.app.R
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.viewmodel.MapViewModel
import com.adygyes.app.presentation.ui.components.*
import com.adygyes.app.presentation.ui.components.ViewMode
import com.adygyes.app.presentation.navigation.NavDestination
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.ClusterListener
import com.yandex.mapkit.map.Cluster
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.MapKitFactory
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Map screen with bottom navigation and integrated search
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreenWithBottomNav(
    navController: NavController,
    onAttractionClick: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val attractions by viewModel.attractions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAttraction by viewModel.selectedAttraction.collectAsStateWithLifecycle()
    
    // Debug logging for selectedAttraction changes
    LaunchedEffect(selectedAttraction) {
        if (selectedAttraction != null) {
            Timber.d("Selected attraction changed to: ${selectedAttraction?.name} (ID: ${selectedAttraction?.id})")
        } else {
            Timber.d("Selected attraction cleared")
        }
    }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val filteredAttractions by viewModel.filteredAttractions.collectAsStateWithLifecycle()
    val isDarkTheme = isSystemInDarkTheme()
    
    // View mode state (Map or List)
    var viewMode by remember { mutableStateOf(com.adygyes.app.presentation.ui.components.ViewMode.MAP) }
    
    // Filter sheet state
    var showFilterSheet by remember { mutableStateOf(false) }
    
    // Search debouncing
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    
    // Location permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Request permissions on first launch
    LaunchedEffect(locationPermissionsState) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        } else {
            viewModel.onLocationPermissionGranted()
        }
    }
    
    // Map lifecycle
    DisposableEffect(Unit) {
        onDispose {
            if (viewMode == com.adygyes.app.presentation.ui.components.ViewMode.MAP) {
                mapView?.onStop()
                MapKitFactory.getInstance().onStop()
            }
        }
    }
    
    // Initialize map when in map view mode
    LaunchedEffect(mapView, viewMode) {
        if (viewMode == com.adygyes.app.presentation.ui.components.ViewMode.MAP) {
            mapView?.let { map ->
                MapKitFactory.getInstance().onStart()
                map.onStart()
                
                // Center map on Adygea region
                val adygeaCenter = Point(44.6098, 40.1006)
                map.map.move(
                    CameraPosition(adygeaCenter, 10.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 2f),
                    null
                )
                
                // Apply map style
                MapStyleProvider.applyMapStyle(map, isDarkTheme)
                MapStyleProvider.configureMapInteraction(map)
                
                Timber.d("Map initialized and centered on Adygea")
            }
        }
    }
    
    // Remember previous attractions to avoid unnecessary marker recreation
    var previousAttractions by remember { mutableStateOf<List<Attraction>>(emptyList()) }
    
    // Add markers for filtered attractions
    LaunchedEffect(mapView, filteredAttractions, viewMode) {
        if (viewMode == com.adygyes.app.presentation.ui.components.ViewMode.MAP) {
            mapView?.let { map ->
                // Only update markers if attractions actually changed
                if (filteredAttractions != previousAttractions) {
                    try {
                        Timber.d("Updating markers for ${filteredAttractions.size} attractions")
                        // Clear existing markers
                        map.map.mapObjects.clear()
                        
                        if (filteredAttractions.isNotEmpty()) {
                            // Create clustered collection
                            val clusterizedCollection = map.map.mapObjects.addClusterizedPlacemarkCollection(
                                object : ClusterListener {
                                    override fun onClusterAdded(cluster: Cluster) {
                                        val text = cluster.size.toString()
                                        cluster.appearance.setIcon(
                                            TextImageProvider(text, context)
                                        )
                                        cluster.appearance.setIconStyle(
                                            IconStyle().apply {
                                                scale = 1f
                                            }
                                        )
                                        cluster.addClusterTapListener { _ ->
                                            map.map.move(
                                                CameraPosition(
                                                    cluster.appearance.geometry,
                                                    map.map.cameraPosition.zoom + 1,
                                                    0.0f, 0.0f
                                                ),
                                                Animation(Animation.Type.SMOOTH, 0.5f),
                                                null
                                            )
                                            true
                                        }
                                    }
                                }
                            )
                            
                            // Add placemarks
                            filteredAttractions.forEach { attraction ->
                                val point = Point(attraction.location.latitude, attraction.location.longitude)
                                val placemark = clusterizedCollection.addPlacemark(point)
                                
                                val markerIcon = CategoryMarkerProvider.getMarkerForCategory(
                                    context = context,
                                    category = attraction.category,
                                    isDarkTheme = isDarkTheme
                                )
                                placemark.setIcon(
                                    markerIcon,
                                    IconStyle().apply {
                                        scale = 0.8f
                                    }
                                )
                                
                                // Improved tap listener with better error handling
                                placemark.addTapListener(MapObjectTapListener { mapObject, point ->
                                    try {
                                        Timber.d("Marker tapped for attraction: ${attraction.name} (ID: ${attraction.id})")
                                        viewModel.selectAttraction(attraction)
                                        true
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error handling marker tap for ${attraction.name}")
                                        false
                                    }
                                })
                                
                                placemark.userData = attraction
                            }
                            
                            clusterizedCollection.clusterPlacemarks(60.0, 15)
                        }
                        
                        previousAttractions = filteredAttractions
                        Timber.d("Successfully added ${filteredAttractions.size} markers")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to add markers")
                    }
                }
            }
        }
    }
    
    Scaffold(
        bottomBar = {
            AdygyesBottomNavigation(
                currentViewMode = viewMode,
                onViewModeToggle = { 
                    viewMode = if (viewMode == com.adygyes.app.presentation.ui.components.ViewMode.MAP) com.adygyes.app.presentation.ui.components.ViewMode.LIST else com.adygyes.app.presentation.ui.components.ViewMode.MAP
                },
                onFavoritesClick = {
                    navController.navigate(NavDestination.Favorites.route)
                },
                onSettingsClick = {
                    navController.navigate(NavDestination.Settings.route)
                },
                showBadgeOnFavorites = filteredAttractions.any { it.isFavorite },
                favoritesCount = filteredAttractions.count { it.isFavorite }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Map or List content
            AnimatedContent(
                targetState = viewMode,
                transitionSpec = {
                    if (targetState == com.adygyes.app.presentation.ui.components.ViewMode.MAP) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { mode ->
                when (mode) {
                    com.adygyes.app.presentation.ui.components.ViewMode.MAP -> {
                        // Map View
                        Box(modifier = Modifier.fillMaxSize()) {
                            AndroidView(
                                factory = { ctx ->
                                    MapView(ctx).also { map ->
                                        mapView = map
                                        Timber.d("MapView created")
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Location FAB
                            FloatingActionButton(
                                onClick = { 
                                    if (locationPermissionsState.allPermissionsGranted) {
                                        uiState.userLocation?.let { location ->
                                            mapView?.map?.move(
                                                CameraPosition(
                                                    Point(location.first, location.second),
                                                    14.0f,
                                                    0.0f,
                                                    0.0f
                                                ),
                                                Animation(Animation.Type.SMOOTH, 0.5f),
                                                null
                                            )
                                        } ?: run {
                                            viewModel.getCurrentLocation()
                                        }
                                    } else {
                                        locationPermissionsState.launchMultiplePermissionRequest()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(Dimensions.SpacingMedium),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = stringResource(R.string.my_location),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            
                            // Selected attraction bottom sheet
                            selectedAttraction?.let { attraction ->
                                LaunchedEffect(attraction.id) {
                                    Timber.d("Showing bottom sheet for: ${attraction.name} (ID: ${attraction.id})")
                                }
                                
                                key(attraction.id) {
                                    AttractionBottomSheet(
                                        attraction = attraction,
                                        onDismiss = { 
                                            Timber.d("Bottom sheet dismissed for: ${attraction.name}")
                                            viewModel.clearSelection() 
                                        },
                                        onBuildRoute = { 
                                            Timber.d("Build route clicked for: ${attraction.name}")
                                            viewModel.navigateToAttractionById(attraction.id) 
                                        },
                                        onShare = { 
                                            Timber.d("Share clicked for: ${attraction.name}")
                                            viewModel.shareAttractionById(attraction.id) 
                                        },
                                        onToggleFavorite = { 
                                            Timber.d("Toggle favorite clicked for: ${attraction.name}")
                                            viewModel.toggleFavorite(attraction.id) 
                                        },
                                        onNavigateToDetail = { 
                                            Timber.d("Navigate to detail clicked for: ${attraction.name}")
                                            onAttractionClick(attraction.id) 
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    com.adygyes.app.presentation.ui.components.ViewMode.LIST -> {
                        // List View
                        AttractionsList(
                            attractions = filteredAttractions,
                            onAttractionClick = onAttractionClick,
                            onFavoriteClick = { attractionId ->
                                viewModel.toggleFavorite(attractionId)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                                .padding(top = 64.dp), // Space for search field
                            isLoading = uiState.isLoading,
                            searchQuery = searchQuery,
                            selectedCategories = selectedCategories.map { it.displayName }.toSet(),
                            showResultCount = true
                        )
                    }
                }
            }
            
            // Search field - floating above content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                    .padding(horizontal = Dimensions.PaddingMedium)
                    .padding(top = Dimensions.PaddingSmall)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    tonalElevation = 2.dp
                ) {
                    BottomNavSearchTextField(
                        value = searchQuery,
                        onValueChange = { query: String ->
                            viewModel.updateSearchQuery(query)
                            // Debounce search
                            searchJob?.cancel()
                            searchJob = scope.launch {
                                delay(300)
                                viewModel.search()
                            }
                        },
                        placeholder = stringResource(R.string.search_attractions),
                        onFilterClick = { showFilterSheet = true },
                        hasActiveFilters = selectedCategories.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Active filters indicator
            AnimatedVisibility(
                visible = searchQuery.isNotEmpty() || selectedCategories.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                    .padding(top = 72.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${filteredAttractions.size} ${stringResource(R.string.results_found)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (searchQuery.isNotEmpty() || selectedCategories.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.clearFilters()
                                    viewModel.updateSearchQuery("")
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.clear),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Filter bottom sheet
    if (showFilterSheet) {
        CategoryFilterBottomSheet(
            selectedCategories = selectedCategories,
            onCategoryToggle = { category ->
                viewModel.toggleCategory(category)
            },
            onApply = {
                showFilterSheet = false
                viewModel.search()
            },
            onDismiss = { showFilterSheet = false },
            onClearAll = { viewModel.clearFilters() }
        )
    }
}

/**
 * Simplified search text field without background for bottom nav screen
 */
@Composable
fun BottomNavSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { 
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Row {
                // Clear button
                AnimatedVisibility(visible = value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search)
                        )
                    }
                }
                
                // Filter button with badge
                BadgedBox(
                    badge = {
                        if (hasActiveFilters) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filters)
                        )
                    }
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
