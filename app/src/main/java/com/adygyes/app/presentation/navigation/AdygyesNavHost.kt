package com.adygyes.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.adygyes.app.presentation.ui.screens.splash.SplashScreen
import com.adygyes.app.presentation.ui.screens.map.MapScreenContainer
import com.adygyes.app.presentation.ui.screens.search.SearchScreen
import com.adygyes.app.presentation.ui.screens.favorites.FavoritesScreen
import com.adygyes.app.presentation.ui.screens.detail.AttractionDetailScreen
import com.adygyes.app.presentation.viewmodel.MapViewModel

/**
 * Main navigation host for Adygyes app
 */
@Composable
fun AdygyesNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavDestination.Splash.route,
        modifier = modifier.fillMaxSize(),
        // Disable default transitions - use per-screen transitions instead
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // Splash Screen
        composable(
            route = NavDestination.Splash.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(NavDestination.Map.route) {
                        popUpTo(NavDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main Map Screen Container - handles Map/Settings overlay like Map/List toggle
        composable(
            route = NavDestination.Map.route,
            enterTransition = {
                fadeIn()
            },
            exitTransition = {
                fadeOut()
            },
            popEnterTransition = {
                fadeIn()
            },
            popExitTransition = {
                fadeOut()
            }
        ) { backStackEntry ->
            // MapScreenContainer handles Map ↔ Settings animation internally
            // (exactly like Map ↔ List toggle)
            MapScreenContainer(
                onAttractionClick = { attractionId ->
                    navController.navigate(NavDestination.AttractionDetail.createRoute(attractionId))
                },
                onNavigateToFavorites = {
                    navController.navigate(NavDestination.Favorites.route)
                }
            )
        }
        
        // Search Screen
        composable(NavDestination.Search.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onAttractionClick = { attractionId ->
                    navController.navigate(NavDestination.AttractionDetail.createRoute(attractionId))
                }
            )
        }
        
        // Favorites Screen
        composable(NavDestination.Favorites.route) {
            FavoritesScreen(
                onAttractionClick = { attractionId ->
                    navController.navigate(NavDestination.AttractionDetail.createRoute(attractionId))
                },
                onExploreClick = {
                    navController.navigate(NavDestination.Map.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Note: Settings, About, Privacy, Terms are now handled inside MapScreenContainer
        // They animate as overlays like List mode, not as navigation routes
        
        // Attraction Detail Screen
        composable(
            route = NavDestination.AttractionDetail.route,
            arguments = listOf(
                navArgument("attractionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val attractionId = backStackEntry.arguments?.getString("attractionId") ?: ""
            
            // Get MapViewModel from MapScreen's navigation entry
            val mapBackStackEntry = remember(navController) {
                navController.getBackStackEntry(NavDestination.Map.route)
            }
            val mapViewModel: MapViewModel = hiltViewModel(mapBackStackEntry)
            
            AttractionDetailScreen(
                attractionId = attractionId,
                onBackClick = { navController.popBackStack() },
                onBuildRoute = {
                    // Will be implemented when Yandex Maps is integrated
                },
                onShareClick = {
                    // Will be implemented with share functionality
                },
                // Always show "Show on Map" button
                onShowOnMap = {
                    // Set attraction ID in MapViewModel
                    mapViewModel.setAttractionToShowOnMap(attractionId)
                    
                    // Navigate to map and show this attraction
                    navController.navigate(NavDestination.Map.route) {
                        // Pop back to map (remove DetailScreen from backstack)
                        popUpTo(NavDestination.Map.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // Photo Gallery Screen
        composable(
            route = NavDestination.PhotoGallery.route,
            arguments = listOf(
                navArgument("attractionId") { type = NavType.StringType },
                navArgument("photoIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val attractionId = backStackEntry.arguments?.getString("attractionId") ?: ""
            val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0
            // PhotoGalleryScreen will be implemented
        }
        
        // Settings sub-screens
        composable(NavDestination.LanguageSettings.route) {
            // LanguageSettingsScreen will be implemented
        }
        
        composable(NavDestination.ThemeSettings.route) {
            // ThemeSettingsScreen will be implemented
        }
        
        // Note: About, Privacy, Terms screens removed from navigation
        // They are now part of MapScreenContainer and animate like List mode overlay
    }
}
