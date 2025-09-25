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
import com.adygyes.app.presentation.ui.components.*
import com.adygyes.app.presentation.ui.components.CategoryFilterBottomSheet
import com.adygyes.app.presentation.ui.components.AdygyesBottomNavigation
import com.adygyes.app.presentation.ui.components.ViewMode
import com.adygyes.app.presentation.ui.map.markers.DualLayerMarkerSystem
import com.adygyes.app.presentation.viewmodel.ImageCacheViewModel
import com.adygyes.app.presentation.viewmodel.MapViewModel
import com.adygyes.app.presentation.viewmodel.MapUiState
import com.adygyes.app.presentation.navigation.NavDestination
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.domain.model.Attraction
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Unified Map Screen - combines all best features from previous versions:
 * - Reliable marker tap handling (from MapScreenReliable)
 * - Bottom navigation integration (from MapScreenWithBottomNav) 
 * - Enhanced search and filtering (from MapScreenEnhanced)
 * - Edge-to-edge display support
 * - Location permissions handling
 * - Map/List view toggle
 * - Real-time search with debouncing
 * - Proper lifecycle management
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    onAttractionClick: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
    imageCacheViewModel: ImageCacheViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    
    // State collection
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAttraction by viewModel.selectedAttraction.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val filteredAttractions by viewModel.filteredAttractions.collectAsStateWithLifecycle()
    val isDarkTheme = isSystemInDarkTheme()
    
    // UI State
    var viewMode by remember { mutableStateOf(ViewMode.MAP) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var isMapReady by remember { mutableStateOf(false) }
    
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
    
    // Debug logging for selectedAttraction changes
    LaunchedEffect(selectedAttraction) {
        if (selectedAttraction != null) {
            Timber.d("🎯 Selected attraction: ${selectedAttraction?.name} (ID: ${selectedAttraction?.id})")
        } else {
            Timber.d("🎯 Selected attraction cleared")
        }
    }
    
    // Force marker update when attractions are loaded
    LaunchedEffect(filteredAttractions.size, isMapReady) {
        if (isMapReady && filteredAttractions.isNotEmpty()) {
            viewModel.updateMarkerPositions()
            Timber.d("🔄 Attractions loaded: ${filteredAttractions.size}, updating markers")
        }
    }
    
    // MapKit lifecycle management
    DisposableEffect(Unit) {
        Timber.d("🗺️ Initializing MapKit")
        MapKitFactory.getInstance().onStart()
        
        onDispose {
            Timber.d("🗺️ Disposing MapKit")
            mapView?.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }
    
    // Location permissions handling
    LaunchedEffect(locationPermissionsState) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        } else {
            viewModel.onLocationPermissionGranted()
        }
    }
    
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.onLocationPermissionGranted()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }
    
    // Main content with edge-to-edge support
    Box(modifier = Modifier.fillMaxSize()) {
        // Map or List content
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
                    // CRITICAL: Box ensures proper layering
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Layer 1: Map View (bottom) - DISABLE TOUCH for markers to work
                        AndroidView(
                            factory = { ctx ->
                                Timber.d("🗺️ Creating MapView")
                                MapView(ctx).apply {
                                    this.onStart()
                                    mapView = this
                                    
                                    // Apply styles immediately
                                    MapStyleProvider.applyMapStyle(this, isDarkTheme)
                                    MapStyleProvider.configureMapInteraction(this)
                                    
                                    // Keep map interactive for pan/zoom
                                    
                                    // Initialize map position
                                    this.map.move(
                                        CameraPosition(Point(44.6098, 40.1006), 10.0f, 0.0f, 0.0f),
                                        Animation(Animation.Type.SMOOTH, 2f),
                                        null
                                    )
                                    
                                    isMapReady = true
                                    // Force marker update after map initialization
                                    viewModel.updateMarkerPositions()
                                    Timber.d("✅ Map initialized and ready, markers updated")
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { view ->
                                // Store latest mapView reference for instant marker updates
                                mapView = view
                                // Force immediate recomposition on any map change
                                viewModel.updateMarkerPositions()
                            }
                        )
                        
                        // Layer 2: DUAL-LAYER MARKER SYSTEM (top)
                        // Native markers for visual (perfect map binding)
                        // Transparent overlays for clicks (100% reliability)
                        if (mapView != null && isMapReady) {
                            DualLayerMarkerSystem(
                                mapView = mapView,
                                attractions = filteredAttractions,
                                selectedAttraction = selectedAttraction,
                                imageCacheManager = remember { 
                                    com.adygyes.app.data.local.cache.ImageCacheManager(context) 
                                },
                                onMarkerClick = { attraction ->
                                    Timber.d("🎯 DUAL-LAYER SYSTEM: Clicked ${attraction.name}")
                                    viewModel.onMarkerClick(attraction)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                
                ViewMode.LIST -> {
                    // List View
                    AttractionsList(
                        attractions = filteredAttractions,
                        onAttractionClick = onAttractionClick,
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                            .padding(top = 64.dp), // Space for search
                        isLoading = uiState.isLoading,
                        searchQuery = searchQuery,
                        selectedCategories = selectedCategories.map { it.displayName }.toSet(),
                        showResultCount = true
                    )
                }
            }
        }
        
        // Floating search field at top
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
                shadowElevation = 8.dp
            ) {
                UnifiedSearchTextField(
                    value = searchQuery,
                    onValueChange = { query: String ->
                        viewModel.updateSearchQuery(query)
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
        
        // Location FAB (only in MAP mode)
        if (viewMode == ViewMode.MAP) {
            FloatingActionButton(
                onClick = { 
                    if (locationPermissionsState.allPermissionsGranted) {
                        uiState.userLocation?.let { location ->
                            mapView?.map?.move(
                                CameraPosition(
                                    Point(location.first, location.second),
                                    14.0f, 0.0f, 0.0f
                                ),
                                Animation(Animation.Type.SMOOTH, 0.5f),
                                null
                            )
                        } ?: viewModel.getCurrentLocation()
                    } else {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimensions.SpacingMedium)
                    .padding(bottom = 80.dp), // Space for bottom nav
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.my_location),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        // Bottom navigation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            AdygyesBottomNavigation(
                currentViewMode = viewMode,
                onViewModeToggle = { 
                    viewMode = if (viewMode == ViewMode.MAP) ViewMode.LIST else ViewMode.MAP
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
        }
        
        // CRITICAL: Bottom sheet must be last to render on top
        selectedAttraction?.let { attraction ->
            Timber.d("🎯 Showing BottomSheet for: ${attraction.name}")
            
            key(attraction.id) {
                AttractionBottomSheet(
                    attraction = attraction,
                    onDismiss = { 
                        Timber.d("🎯 BottomSheet dismissed")
                        viewModel.clearSelection() 
                    },
                    onBuildRoute = { 
                        viewModel.navigateToAttractionById(attraction.id) 
                    },
                    onShare = { 
                        viewModel.shareAttractionById(attraction.id) 
                    },
                    onToggleFavorite = { 
                        viewModel.toggleFavorite(attraction.id) 
                    },
                    onNavigateToDetail = { 
                        onAttractionClick(attraction.id) 
                    }
                )
            }
        }
    }
    
    // Filter bottom sheet
    if (showFilterSheet) {
        CategoryFilterBottomSheet(
            selectedCategories = selectedCategories,
            onCategoryToggle = { viewModel.toggleCategory(it) },
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
 * Unified search text field for the map screen
 */
@Composable
private fun UnifiedSearchTextField(
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
