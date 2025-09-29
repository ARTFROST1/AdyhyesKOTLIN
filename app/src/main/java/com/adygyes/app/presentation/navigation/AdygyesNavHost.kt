package com.adygyes.app.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.adygyes.app.presentation.ui.screens.splash.SplashScreen
import com.adygyes.app.presentation.ui.screens.map.MapScreen
import com.adygyes.app.presentation.ui.screens.search.SearchScreen
import com.adygyes.app.presentation.ui.screens.favorites.FavoritesScreen
import com.adygyes.app.presentation.ui.screens.settings.SettingsScreen
import com.adygyes.app.presentation.ui.screens.detail.AttractionDetailScreen
import com.adygyes.app.presentation.ui.screens.about.AboutScreen
import com.adygyes.app.presentation.ui.screens.privacy.PrivacyPolicyScreen
import com.adygyes.app.presentation.ui.screens.terms.TermsOfUseScreen

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
        composable(NavDestination.Map.route) {
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
        
        // Settings Screen
        composable(NavDestination.Settings.route) {
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
            AttractionDetailScreen(
                attractionId = attractionId,
                onBackClick = { navController.popBackStack() },
                onBuildRoute = {
                    // Will be implemented when Yandex Maps is integrated
                },
                onShareClick = {
                    // Will be implemented with share functionality
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
        
        composable(NavDestination.AboutScreen.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NavDestination.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NavDestination.TermsOfUse.route) {
            TermsOfUseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
