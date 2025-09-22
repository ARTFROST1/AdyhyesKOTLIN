package com.adygyes.app.presentation.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adygyes.app.R
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.viewmodel.MapViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.MapKitFactory
import com.yandex.runtime.image.ImageProvider
import timber.log.Timber

/**
 * Map screen with Yandex MapKit integration
 */
@Composable
fun MapScreenWithYandex(
    onAttractionClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val attractions by viewModel.attractions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAttraction by viewModel.selectedAttraction.collectAsStateWithLifecycle()
    
    // Remember map lifecycle
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }
    
    LaunchedEffect(mapView) {
        mapView?.let { map ->
            try {
                MapKitFactory.getInstance().onStart()
                map.onStart()
                
                // Center map on Adygea region (Maykop - capital city)
                val adygeaCenter = Point(44.6098, 40.1006) // Coordinates of Maykop
                map.map.move(
                    CameraPosition(adygeaCenter, 10.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 2f),
                    null
                )
                
                // Enable zoom controls
                map.map.isZoomGesturesEnabled = true
                map.map.isScrollGesturesEnabled = true
                map.map.isRotateGesturesEnabled = true
                map.map.isTiltGesturesEnabled = true
                
                Timber.d("Map initialized and centered on Adygea")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize map")
            }
        }
    }
    
    // Add markers for attractions
    LaunchedEffect(mapView, attractions) {
        mapView?.let { map ->
            try {
                // Clear existing markers
                map.map.mapObjects.clear()
                
                // Add markers for each attraction
                attractions.forEach { attraction ->
                    val point = Point(attraction.location.latitude, attraction.location.longitude)
                    val placemark = map.map.mapObjects.addPlacemark(point)
                    
                    // Set marker style based on category
                    placemark.setIcon(
                        ImageProvider.fromResource(context, R.drawable.ic_map_marker)
                    )
                    
                    // Add tap listener
                    placemark.addTapListener(MapObjectTapListener { _, _ ->
                        viewModel.selectAttraction(attraction)
                        true
                    })
                    
                    // Store attraction ID as user data
                    placemark.userData = attraction.id
                }
                
                Timber.d("Added ${attractions.size} markers to map")
            } catch (e: Exception) {
                Timber.e(e, "Failed to add markers")
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Map View
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { map ->
                    mapView = map
                    Timber.d("MapView created")
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
                // TODO: Implement location centering
                Timber.d("Location button clicked")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimensions.SpacingMedium)
                .padding(bottom = 80.dp) // Account for bottom navigation
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "My Location"
            )
        }
    }
}
