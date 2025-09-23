package com.adygyes.app.presentation.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.viewmodel.MapViewModel
import com.adygyes.app.presentation.ui.components.AttractionCard
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
import timber.log.Timber

/**
 * Tablet-optimized map screen with side panel for attraction details
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreenTablet(
    onAttractionClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val attractions by viewModel.attractions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAttraction by viewModel.selectedAttraction.collectAsStateWithLifecycle()
    val isDarkTheme = isSystemInDarkTheme()
    
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
    
    // Remember map lifecycle
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }
    
    LaunchedEffect(mapView) {
        mapView?.let { map ->
            MapKitFactory.getInstance().onStart()
            map.onStart()
            
            // Center map on Adygea region (Maykop - capital city)
            val adygeaCenter = Point(44.6098, 40.1006) // Coordinates of Maykop
            map.map.move(
                CameraPosition(adygeaCenter, 10.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 2f),
                null
            )
            
            // Apply map style and configure interactions
            MapStyleProvider.applyMapStyle(map, isDarkTheme)
            MapStyleProvider.configureMapInteraction(map)
            
            Timber.d("Tablet map initialized and centered on Adygea")
        }
    }
    
    // Add user location marker
    LaunchedEffect(mapView, uiState.userLocation) {
        mapView?.let { map ->
            uiState.userLocation?.let { location ->
                try {
                    // Remove old user location marker if exists
                    val userMarkerTag = "user_location_marker"
                    map.map.mapObjects.traverse(object : MapObjectVisitor {
                        override fun onPlacemarkVisited(placemark: PlacemarkMapObject) {
                            if (placemark.userData == userMarkerTag) {
                                map.map.mapObjects.remove(placemark)
                            }
                        }
                        override fun onPolylineVisited(polyline: com.yandex.mapkit.map.PolylineMapObject) {}
                        override fun onPolygonVisited(polygon: com.yandex.mapkit.map.PolygonMapObject) {}
                        override fun onCircleVisited(circle: com.yandex.mapkit.map.CircleMapObject) {}
                        override fun onCollectionVisitStart(collection: com.yandex.mapkit.map.MapObjectCollection): Boolean = true
                        override fun onCollectionVisitEnd(collection: com.yandex.mapkit.map.MapObjectCollection) {}
                        override fun onClusterizedCollectionVisitStart(collection: com.yandex.mapkit.map.ClusterizedPlacemarkCollection): Boolean = true
                        override fun onClusterizedCollectionVisitEnd(collection: com.yandex.mapkit.map.ClusterizedPlacemarkCollection) {}
                    })
                    
                    // Add new user location marker
                    val userPoint = Point(location.first, location.second)
                    val userMarker = map.map.mapObjects.addPlacemark(userPoint)
                    userMarker.setIcon(
                        ImageProvider.fromResource(context, R.drawable.ic_user_location),
                        IconStyle().apply {
                            scale = 1.0f
                        }
                    )
                    userMarker.userData = userMarkerTag
                    
                    Timber.d("User location marker added: ${location.first}, ${location.second}")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to add user location marker")
                }
            }
        }
    }
    
    // Add markers for attractions with clustering
    LaunchedEffect(mapView, attractions) {
        mapView?.let { map ->
            try {
                // Clear existing markers
                map.map.mapObjects.clear()
                
                // Create clustered collection
                val clusterizedCollection = map.map.mapObjects.addClusterizedPlacemarkCollection(
                    object : ClusterListener {
                        override fun onClusterAdded(cluster: Cluster) {
                            // Update cluster appearance to show number of items
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
                                // Zoom in when cluster is tapped
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
                attractions.forEach { attraction ->
                    val point = Point(attraction.location.latitude, attraction.location.longitude)
                    val placemark = clusterizedCollection.addPlacemark(point)
                    
                    // Set marker style based on category with themed colors
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
                
                Timber.d("Added ${attractions.size} markers with clustering to tablet map")
            } catch (e: Exception) {
                Timber.e(e, "Failed to add markers to tablet map")
            }
        }
    }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Map View (takes 2/3 of screen)
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).also { map ->
                        mapView = map
                        Timber.d("Tablet MapView created")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Search Bar at the top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.SpacingMedium)
                    .align(Alignment.TopCenter),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.SpacingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.nav_search),
                        modifier = Modifier.padding(end = Dimensions.SpacingSmall)
                    )
                    TextButton(
                        onClick = onSearchClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Location FAB
            FloatingActionButton(
                onClick = { 
                    if (locationPermissionsState.allPermissionsGranted) {
                        // Center on user location
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
                    Timber.d("Location button clicked on tablet")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimensions.SpacingMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "My Location"
                )
            }
        }
        
        // Side Panel (takes 1/3 of screen)
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(Dimensions.SpacingSmall),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.PaddingMedium)
            ) {
                Text(
                    text = "Информация",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = Dimensions.PaddingMedium)
                )
                
                selectedAttraction?.let { attraction ->
                    AttractionCard(
                        attraction = attraction,
                        onClick = { /* Handle attraction click */ },
                        onFavoriteClick = { viewModel.toggleFavorite(attraction.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } ?: run {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Выберите достопримечательность на карте",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
