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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalFocusManager
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
import com.adygyes.app.presentation.ui.components.ViewMode
import com.adygyes.app.presentation.ui.components.SearchResultsHeader
import com.adygyes.app.presentation.ui.components.UnifiedCategoryCarousel
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
import com.adygyes.app.domain.model.AttractionCategory
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
    onAttractionClick: (String) -> Unit,
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // Get persistent MapView and preload manager from MapHost
    val mapHostController = LocalMapHostController.current
    val mapView: MapView? = mapHostController?.mapView
    val preloadManager = mapHostController?.preloadManager
    
    // State collection
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val attractions by viewModel.attractions.collectAsStateWithLifecycle()
    val filteredAttractions by viewModel.filteredAttractions.collectAsStateWithLifecycle()
    val selectedAttraction by viewModel.selectedAttraction.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val listViewMode by viewModel.listViewMode.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    
    // UI State
    var showFilterSheet by remember { mutableStateOf(false) }
    
    // Unified state for category carousel - always visible in LIST mode, toggleable in MAP mode
    var showCategoryCarousel by remember { mutableStateOf(false) }
    
    // Search field focus state for expandable search
    var isSearchFieldFocused by remember { mutableStateOf(false) }
    
    // Auto-show carousel in LIST mode
    LaunchedEffect(viewMode) {
        if (viewMode == ViewMode.LIST) {
            showCategoryCarousel = true
        }
        // Keep previous state when switching to MAP mode
    }
    
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    var isMapReady by remember { mutableStateOf(false) }
    
    // Easter egg state
    val easterEggActive by EasterEggManager.isActive.collectAsState()
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
                            // Always use DualLayerMarkerSystem for proper filtering
                            // This ensures visual markers update when filters change
                            DualLayerMarkerSystem(
                                mapView = mapView,
                                attractions = filteredAttractions, // This updates with filters!
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
                
                ViewMode.LIST -> {
                    // List View - now uses the same unified carousel
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                            .padding(top = if (showCategoryCarousel) 140.dp else 90.dp) // Dynamic padding based on carousel visibility
                    ) {
                        // Attractions list/grid (unified carousel is displayed above as part of search area)
                        AttractionsList(
                            attractions = filteredAttractions,
                            onAttractionClick = onAttractionClick,
                            onFavoriteClick = { viewModel.toggleFavorite(it) },
                            modifier = Modifier.fillMaxSize(),
                            isLoading = uiState.isLoading,
                            searchQuery = searchQuery,
                            selectedCategories = selectedCategories.map { it.displayName }.toSet(),
                            showResultCount = false, // Count is shown in unified carousel
                            viewMode = if (listViewMode == MapViewModel.ListViewMode.LIST) {
                                com.adygyes.app.presentation.ui.components.ListViewMode.LIST
                            } else {
                                com.adygyes.app.presentation.ui.components.ListViewMode.GRID
                            }
                        )
                    }
                }
            }
        }
        
        // Top navigation bar with buttons and search field
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(horizontal = Dimensions.PaddingMedium)
                .padding(
                    top = Dimensions.PaddingSmall,
                    bottom = if (viewMode == ViewMode.LIST) Dimensions.PaddingMedium else 0.dp
                )
        ) {
            // Top bar with animated layout for expandable search
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                // Animated layout based on search field focus
                AnimatedContent(
                    targetState = isSearchFieldFocused,
                    transitionSpec = {
                        slideInHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) { if (targetState) -it else it } + fadeIn(
                            animationSpec = tween(300)
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) { if (targetState) it else -it } + fadeOut(
                            animationSpec = tween(300)
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { focused ->
                    if (focused) {
                        // Expanded search field layout
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shadowElevation = 8.dp
                        ) {
                            if (viewMode == ViewMode.LIST) {
                                // Enhanced search field for list mode with sort and view toggle
                                EnhancedSearchTextField(
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
                                    sortBy = sortBy,
                                    onSortChange = { viewModel.setSortBy(it) },
                                    viewMode = listViewMode,
                                    onViewModeToggle = { viewModel.toggleListViewMode() },
                                    isExpanded = true,
                                    onFocusChange = { focused -> isSearchFieldFocused = focused },
                                    onCloseClick = { isSearchFieldFocused = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                )
                            } else {
                                // Expanded search field for map mode
                                ExpandedSearchTextField(
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
                                    onFilterClick = { 
                                        showCategoryCarousel = !showCategoryCarousel
                                    },
                                    onCloseClick = { isSearchFieldFocused = false },
                                    hasActiveFilters = selectedCategories.isNotEmpty() || selectedCategoryFilter !is MapViewModel.CategoryFilter.All,
                                    isCarouselVisible = showCategoryCarousel,
                                    onFocusChange = { focused -> isSearchFieldFocused = focused },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                )
                            }
                        }
                    } else {
                        // Normal three-button layout
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Left button - View Mode Toggle (Map/List)
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .fillMaxHeight(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shadowElevation = 8.dp
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleViewMode() },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    AnimatedContent(
                                        targetState = viewMode,
                                        transitionSpec = {
                                            (scaleIn(animationSpec = tween(220)) + fadeIn(animationSpec = tween(220)))
                                                .togetherWith(scaleOut(animationSpec = tween(90)) + fadeOut(animationSpec = tween(90)))
                                        }
                                    ) { mode ->
                                        Icon(
                                            imageVector = if (mode == ViewMode.MAP) {
                                                Icons.Filled.List
                                            } else {
                                                Icons.Filled.Map
                                            },
                                            contentDescription = if (mode == ViewMode.MAP) {
                                                stringResource(R.string.switch_to_list_view)
                                            } else {
                                                stringResource(R.string.switch_to_map_view)
                                            },
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
                            // Center - Search field container
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shadowElevation = 8.dp
                            ) {
                                if (viewMode == ViewMode.LIST) {
                                    // Enhanced search field for list mode with sort and view toggle
                                    EnhancedSearchTextField(
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
                                        sortBy = sortBy,
                                        onSortChange = { viewModel.setSortBy(it) },
                                        viewMode = listViewMode,
                                        onViewModeToggle = { viewModel.toggleListViewMode() },
                                        isExpanded = false,
                                        onFocusChange = { focused -> isSearchFieldFocused = focused },
                                        onCloseClick = { },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                    )
                                } else {
                                    // Simple search field for map mode
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
                                        onFilterClick = { 
                                            showCategoryCarousel = !showCategoryCarousel
                                        },
                                        hasActiveFilters = selectedCategories.isNotEmpty() || selectedCategoryFilter !is MapViewModel.CategoryFilter.All,
                                        isCarouselVisible = showCategoryCarousel,
                                        onFocusChange = { focused -> isSearchFieldFocused = focused },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                    )
                                }
                            }
                            
                            // Right button - Settings
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .fillMaxHeight(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shadowElevation = 8.dp
                            ) {
                                IconButton(
                                    onClick = onNavigateToSettings,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = stringResource(R.string.nav_settings),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Unified category carousel for both Map and List modes
            // Shows when filter button is clicked or always in list mode
            Spacer(modifier = Modifier.height(8.dp))
            
            UnifiedCategoryCarousel(
                attractionsCount = filteredAttractions.size,
                searchQuery = searchQuery,
                selectedCategories = selectedCategories.map { it.displayName }.toSet(),
                selectedFilter = selectedCategoryFilter,
                onFilterSelected = { filter ->
                    viewModel.selectCategoryFilter(filter)
                },
                visible = showCategoryCarousel,
                modifier = Modifier.fillMaxWidth()
            )
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
        
        // Bottom navigation removed - buttons moved to top bar
        
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
 * Simple search text field for map mode
 */
@Composable
private fun UnifiedSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    isCarouselVisible: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Start
        ),
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
                        imageVector = if (isCarouselVisible) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.FilterList
                        },
                        contentDescription = stringResource(R.string.filters),
                        tint = if (isCarouselVisible) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
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

/**
 * Expanded search text field for map mode when focused
 */
@Composable
private fun ExpandedSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onFilterClick: () -> Unit,
    onCloseClick: () -> Unit,
    hasActiveFilters: Boolean,
    isCarouselVisible: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Auto-focus when this component appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Start
        ),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Filter button with badge (if filters are available)
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
                            imageVector = if (isCarouselVisible) {
                                Icons.Default.ExpandLess
                            } else {
                                Icons.Default.FilterList
                            },
                            contentDescription = stringResource(R.string.filters),
                            tint = if (isCarouselVisible) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                // Close button with dual function: clear text and remove focus
                IconButton(
                    onClick = {
                        onValueChange("") // Clear text
                        focusManager.clearFocus() // Remove focus from input field
                        onCloseClick() // Close expanded mode
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.common_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

// MapCategoryCarousel and CategoryFilterChip components have been replaced by UnifiedCategoryCarousel
// The unified component ensures consistent behavior across Map and List view modes

/**
 * Enhanced search text field with sort and view toggle for list mode
 */
@Composable
private fun EnhancedSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    sortBy: MapViewModel.SortBy,
    onSortChange: (MapViewModel.SortBy) -> Unit,
    viewMode: MapViewModel.ListViewMode,
    onViewModeToggle: () -> Unit,
    isExpanded: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {},
    onCloseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Auto-focus when expanded
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            focusRequester.requestFocus()
        }
    }
    
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Start
        ),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sort dropdown (only show when not expanded)
                if (!isExpanded) {
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = stringResource(R.string.sort),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            MapViewModel.SortBy.values().forEach { sortOption ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(sortOption.displayName)
                                            if (sortBy == sortOption) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSortChange(sortOption)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // View mode toggle (only show when not expanded)
                if (!isExpanded) {
                    IconButton(
                        onClick = onViewModeToggle
                    ) {
                        Icon(
                            imageVector = if (viewMode == MapViewModel.ListViewMode.GRID) {
                                Icons.Default.ViewList
                            } else {
                                Icons.Default.GridView
                            },
                            contentDescription = stringResource(R.string.toggle_view),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Close button with dual function (only show when expanded)
                if (isExpanded) {
                    IconButton(
                        onClick = {
                            onValueChange("") // Clear text
                            focusManager.clearFocus() // Remove focus from input field
                            onCloseClick() // Close expanded mode
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.common_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
