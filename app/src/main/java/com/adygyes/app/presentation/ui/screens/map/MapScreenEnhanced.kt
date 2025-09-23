package com.adygyes.app.presentation.ui.screens.map

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.adygyes.app.R
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.viewmodel.MapViewModel
import com.adygyes.app.presentation.ui.components.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.ClusterListener
import com.yandex.mapkit.map.Cluster
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectVisitor
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.MapKitFactory
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Enhanced Map screen with integrated search and list view
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreenEnhanced(
    onAttractionClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val attractions by viewModel.attractions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAttraction by viewModel.selectedAttraction.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val filteredAttractions by viewModel.filteredAttractions.collectAsStateWithLifecycle()
    val isDarkTheme = isSystemInDarkTheme()
    
    // View mode state (Map or List)
    var viewMode by remember { mutableStateOf(ViewMode.MAP) }
    
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
    
    // Handle permission result
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.onLocationPermissionGranted()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }
    
    // Map lifecycle
    DisposableEffect(Unit) {
        onDispose {
            if (viewMode == ViewMode.MAP) {
                mapView?.onStop()
                MapKitFactory.getInstance().onStop()
            }
        }
    }
    
    // Initialize map when in map view mode
    LaunchedEffect(mapView, viewMode) {
        if (viewMode == ViewMode.MAP) {
            mapView?.let { map ->
                MapKitFactory.getInstance().onStart()
                map.onStart()
                
                // Center map on Adygea region (Maykop - capital city)
                val adygeaCenter = Point(44.6098, 40.1006)
                map.map.move(
                    CameraPosition(adygeaCenter, 10.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 2f),
                    null
                )
                
                // Apply map style and configure interactions
                MapStyleProvider.applyMapStyle(map, isDarkTheme)
                MapStyleProvider.configureMapInteraction(map)
                
                Timber.d("Map initialized and centered on Adygea")
            }
        }
    }
    
    // Add markers for filtered attractions
    LaunchedEffect(mapView, filteredAttractions, viewMode) {
        if (viewMode == ViewMode.MAP) {
            mapView?.let { map ->
                try {
                    // Clear existing markers
                    map.map.mapObjects.clear()
                    
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
                    
                    // Add placemarks to the clustered collection
                    filteredAttractions.forEach { attraction ->
                        val point = Point(attraction.location.latitude, attraction.location.longitude)
                        val placemark = clusterizedCollection.addPlacemark(point)
                        
                        // Set marker style based on category
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
                        
                        // Add tap listener
                        placemark.addTapListener(MapObjectTapListener { _, _ ->
                            viewModel.selectAttraction(attraction)
                            true
                        })
                        
                        // Store attraction data
                        placemark.userData = attraction
                    }
                    
                    // Cluster the placemarks
                    clusterizedCollection.clusterPlacemarks(60.0, 15)
                    
                    Timber.d("Added ${filteredAttractions.size} markers with clustering")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to add markers")
                }
            }
        }
    }
    
    // Add user location marker
    LaunchedEffect(mapView, uiState.userLocation, viewMode) {
        if (viewMode == ViewMode.MAP) {
            mapView?.let { map ->
                uiState.userLocation?.let { location ->
                    try {
                        // Add user location marker logic here
                        val userPoint = Point(location.first, location.second)
                        val userMarker = map.map.mapObjects.addPlacemark(userPoint)
                        userMarker.setIcon(
                            ImageProvider.fromResource(context, R.drawable.ic_user_location),
                            IconStyle().apply {
                                scale = 1.0f
                            }
                        )
                        Timber.d("User location marker added: ${location.first}, ${location.second}")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to add user location marker")
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    // Integrated search field
                    SearchTextField(
                        value = searchQuery,
                        onValueChange = { query ->
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
                },
                navigationIcon = {
                    // View mode toggle
                    IconButton(
                        onClick = { 
                            viewMode = if (viewMode == ViewMode.MAP) ViewMode.LIST else ViewMode.MAP
                        }
                    ) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.MAP) {
                                Icons.Default.List
                            } else {
                                Icons.Default.Map
                            },
                            contentDescription = if (viewMode == ViewMode.MAP) {
                                stringResource(R.string.switch_to_list_view)
                            } else {
                                stringResource(R.string.switch_to_map_view)
                            }
                        )
                    }
                },
                actions = {
                    // Favorites button
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.nav_favorites)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Animated transition between Map and List views
            AnimatedContent(
                targetState = viewMode,
                transitionSpec = {
                    if (targetState == ViewMode.MAP) {
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
                    ViewMode.MAP -> {
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
                                    .padding(Dimensions.SpacingMedium)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = stringResource(R.string.my_location)
                                )
                            }
                            
                            // Selected attraction bottom sheet
                            selectedAttraction?.let { attraction ->
                                AttractionBottomSheet(
                                    attraction = attraction,
                                    onDismiss = { viewModel.clearSelection() },
                                    onNavigateToDetail = { onAttractionClick(attraction.id) },
                                    onBuildRoute = { viewModel.navigateToAttractionById(attraction.id) },
                                    onToggleFavorite = { viewModel.toggleFavorite(attraction.id) },
                                    onShare = { viewModel.shareAttractionById(attraction.id) }
                                )
                            }
                        }
                    }
                    
                    ViewMode.LIST -> {
                        // List View
                        AttractionsList(
                            attractions = filteredAttractions,
                            onAttractionClick = onAttractionClick,
                            onFavoriteClick = { attractionId ->
                                viewModel.toggleFavorite(attractionId)
                            },
                            modifier = Modifier.fillMaxSize(),
                            isLoading = uiState.isLoading,
                            searchQuery = searchQuery,
                            selectedCategories = selectedCategories.map { it.displayName }.toSet(),
                            showResultCount = true
                        )
                    }
                }
            }
            
            // Active filters indicator
            AnimatedVisibility(
                visible = searchQuery.isNotEmpty() || selectedCategories.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(top = Dimensions.SpacingSmall)
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
                            text = "${filteredAttractions.size} results",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (searchQuery.isNotEmpty() || selectedCategories.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.clearFilters()
                                    viewModel.updateSearchQuery("")
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelMedium
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
 * View mode enum
 */
enum class ViewMode {
    MAP, LIST
}

/**
 * Search text field component
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
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
                            Badge()
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
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * Category filter bottom sheet
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
            
            // Category filters
            CategoryFilterChips(
                selectedCategories = selectedCategories,
                onCategoryToggle = onCategoryToggle,
                modifier = Modifier.fillMaxWidth()
            )
            
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
