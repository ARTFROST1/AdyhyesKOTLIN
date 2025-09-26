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
// import removed: AndroidView no longer needed in overlay mode
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.adygyes.app.R
import com.adygyes.app.presentation.ui.components.*
import com.adygyes.app.presentation.ui.components.CategoryFilterBottomSheet
import com.adygyes.app.presentation.ui.components.AdygyesBottomNavigation
import com.adygyes.app.presentation.ui.components.ViewMode
import com.adygyes.app.presentation.ui.map.markers.DualLayerMarkerSystem
import com.adygyes.app.presentation.ui.map.markers.MarkerOverlay
import com.adygyes.app.presentation.ui.map.markers.VisualMarkerRegistry
import com.adygyes.app.presentation.viewmodel.ImageCacheViewModel
import com.adygyes.app.presentation.viewmodel.MapViewModel
import com.adygyes.app.presentation.viewmodel.MapUiState
import com.adygyes.app.presentation.viewmodel.ThemeViewModel
import com.adygyes.app.presentation.navigation.NavDestination
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.domain.model.Attraction
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
// import removed: MapKitFactory lifecycle handled by MapHost
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import com.adygyes.app.presentation.ui.util.EasterEggManager
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInteropFilter

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
    // Get persistent MapView and preload manager from MapHost
    val mapHostController = LocalMapHostController.current
    val mapView: MapView? = mapHostController?.mapView
    val preloadManager = mapHostController?.preloadManager
    
    // State collection
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAttraction by viewModel.selectedAttraction.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val filteredAttractions by viewModel.filteredAttractions.collectAsStateWithLifecycle()
    // Observe app theme (light/dark/system) and compute dark flag
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val isDarkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    
    // UI State
    var viewMode by remember { mutableStateOf(ViewMode.MAP) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var isMapReady by remember { mutableStateOf(false) }
    val easterEggActive by EasterEggManager.isActive.collectAsState()
    
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
    
    // Check if data is preloaded and skip loading if already done
    val preloadState = preloadManager?.preloadState?.collectAsStateWithLifecycle()
    
    LaunchedEffect(preloadState?.value?.dataLoaded) {
        if (preloadState?.value?.dataLoaded == true && filteredAttractions.isEmpty()) {
            // Trigger data loading in ViewModel if not already loaded
            // The ViewModel will automatically load data when needed
            Timber.d("🚀 Preloaded data available, ViewModel will use it")
        }
    }
    
    // Log when MapScreen becomes active with preloaded markers
    LaunchedEffect(mapView, preloadState?.value?.allMarkersReady) {
        if (mapView != null && preloadState?.value?.allMarkersReady == true) {
            val existingIds = preloadManager?.let { VisualMarkerRegistry.getLastIds(mapView) }
            Timber.d("🎯 MapScreen active with MapView: ${mapView.hashCode()}, preloaded markers: ${existingIds?.size ?: 0}")
        }
    }
    
    // Force marker update when attractions are loaded
    LaunchedEffect(filteredAttractions.size, isMapReady) {
        if (isMapReady && filteredAttractions.isNotEmpty()) {
            // Only update if not already created by preloader
            if (preloadState?.value?.markersCreated != true) {
                viewModel.updateMarkerPositions()
                Timber.d("🔄 Attractions loaded: ${filteredAttractions.size}, updating markers")
            } else {
                Timber.d("✅ Markers already created by preloader")
            }
        }
    }
    
    // MapKit lifecycle handled by MapHost now
    
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
                    // Overlay mode: Map is rendered by MapHost behind; here we render overlay click layer and ensure native markers rendered via DualLayerMarkerSystem
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Ensure readiness based on persistent mapView availability
                        isMapReady = mapView != null

                        if (easterEggActive) {
                            // Opaque container to ensure the map is fully covered, even if image fails to load
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                                    .pointerInteropFilter { true } // block gestures to the map
                            ) {
                                var loadError by remember { mutableStateOf(false) }
                                var isLoading by remember { mutableStateOf(true) }

                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1589182337358-2cb63099350c?w=1200",
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onSuccess = {
                                        isLoading = false
                                        loadError = false
                                        Timber.d("🖼️ Easter egg image loaded successfully")
                                    },
                                    onError = {
                                        isLoading = false
                                        loadError = true
                                        Timber.w("🖼️ Easter egg image failed to load")
                                    }
                                )

                                if (isLoading) {
                                    // Simple overlay while loading
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.White)
                                    }
                                }

                                if (loadError) {
                                    // Show subtle text so it's clear the background is intentional
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Easter Egg",
                                            color = Color.White.copy(alpha = 0.6f),
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                    }
                                }
                            }
                        }

                        if (mapView != null && isMapReady) {
                            // Check if markers are already rendered in background
                            val backgroundMarkersReady = preloadState?.value?.allMarkersReady == true
                            
                            if (backgroundMarkersReady) {
                                // Markers already rendered in background, only add interaction layer
                                Timber.d("🎯 Using background markers, adding interaction layer only")
                                MarkerOverlay(
                                    mapView = mapView,
                                    attractions = filteredAttractions,
                                    selectedAttraction = selectedAttraction,
                                    onMarkerClick = { attraction ->
                                        Timber.d("🎯 MARKER CLICKED: ${attraction.name}")
                                        viewModel.onMarkerClick(attraction)
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    enableClustering = false,
                                    animationDuration = 0,
                                    transparentMode = true // Only interactions, no visuals
                                )
                            } else {
                                // Fallback: render full DualLayerMarkerSystem if background not ready
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
                                    modifier = Modifier.fillMaxSize(),
                                    composeVisualMode = easterEggActive
                                )
                            }
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
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.my_location),
                    tint = MaterialTheme.colorScheme.onPrimary
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
