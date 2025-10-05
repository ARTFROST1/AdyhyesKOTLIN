package com.adygyes.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.adygyes.app.presentation.ui.screens.map.MapScreen
import com.adygyes.app.presentation.ui.screens.search.SearchScreen
import com.adygyes.app.presentation.ui.screens.favorites.FavoritesScreen
import com.adygyes.app.presentation.ui.screens.settings.SettingsScreen
import com.adygyes.app.presentation.ui.screens.detail.AttractionDetailScreen
import com.adygyes.app.presentation.ui.screens.about.AboutScreen
import com.adygyes.app.presentation.ui.screens.privacy.PrivacyPolicyScreen
import com.adygyes.app.presentation.ui.screens.terms.TermsOfUseScreen
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
        modifier = modifier.fillMaxSize()
    ) {
        // Splash Screen
        composable(NavDestination.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(NavDestination.Map.route) {
                        popUpTo(NavDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main Map Screen
        composable(NavDestination.Map.route) { backStackEntry ->
            MapScreen(
                onAttractionClick = { attractionId ->
                    navController.navigate(NavDestination.AttractionDetail.createRoute(attractionId))
                },
                onNavigateToFavorites = {
                    navController.navigate(NavDestination.Favorites.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavDestination.Settings.route)
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
        
        // Settings Screen - with smooth slide animation from right
        composable(
            route = NavDestination.Settings.route,
            enterTransition = {
                // Slide in from right with fade (like List mode)
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                // Stay in place with slight fade when opening sub-screens
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = LinearEasing
                    )
                )
            },
            popEnterTransition = {
                // Fade back in when returning from sub-screens
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = LinearEasing
                    )
                )
            },
            popExitTransition = {
                // Slide out to right with fade when going back
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = {
                    navController.navigate(NavDestination.AboutScreen.route)
                },
                onNavigateToPrivacy = {
                    navController.navigate(NavDestination.PrivacyPolicy.route)
                },
                onNavigateToTerms = {
                    navController.navigate(NavDestination.TermsOfUse.route)
                }
            )
        }
        
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
        
        // About Screen - with smooth slide animation from right
        composable(
            route = NavDestination.AboutScreen.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Privacy Policy Screen - with smooth slide animation from right
        composable(
            route = NavDestination.PrivacyPolicy.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Terms of Use Screen - with smooth slide animation from right
        composable(
            route = NavDestination.TermsOfUse.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            TermsOfUseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
