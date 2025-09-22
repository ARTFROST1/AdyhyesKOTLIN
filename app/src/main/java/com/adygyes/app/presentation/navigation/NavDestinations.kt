package com.adygyes.app.presentation.navigation

/**
 * Navigation destinations for the Adygyes app
 */
sealed class NavDestination(val route: String) {
    
    // Main screens
    object Map : NavDestination("map")
    object Search : NavDestination("search")
    object Favorites : NavDestination("favorites")
    object Settings : NavDestination("settings")
    
    // Detail screens
    object AttractionDetail : NavDestination("attraction/{attractionId}") {
        fun createRoute(attractionId: String) = "attraction/$attractionId"
    }
    
    // Category filter
    object CategoryFilter : NavDestination("category_filter")
    
    // Photo gallery
    object PhotoGallery : NavDestination("photo_gallery/{attractionId}/{photoIndex}") {
        fun createRoute(attractionId: String, photoIndex: Int) = "photo_gallery/$attractionId/$photoIndex"
    }
    
    // Settings sub-screens
    object LanguageSettings : NavDestination("settings/language")
    object ThemeSettings : NavDestination("settings/theme")
    object AboutScreen : NavDestination("settings/about")
    object PrivacyPolicy : NavDestination("settings/privacy")
    object TermsOfUse : NavDestination("settings/terms")
}

/**
 * Bottom navigation items
 */
enum class BottomNavItem(
    val destination: NavDestination,
    val iconResId: Int = 0, // Will be replaced with actual icon resources
    val labelResId: Int = 0  // Will be replaced with actual string resources
) {
    MAP(NavDestination.Map),
    SEARCH(NavDestination.Search),
    FAVORITES(NavDestination.Favorites),
    SETTINGS(NavDestination.Settings)
}
