package com.adygyes.app.presentation.ui.screens.map

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.adygyes.app.presentation.ui.screens.settings.SettingsScreen
import com.adygyes.app.presentation.ui.screens.about.AboutScreen
import com.adygyes.app.presentation.ui.screens.privacy.PrivacyPolicyScreen
import com.adygyes.app.presentation.ui.screens.terms.TermsOfUseScreen

/**
 * Container that holds MapScreen and Settings as overlays
 * Settings slides over Map exactly like List mode slides over Map mode
 */
enum class ScreenMode {
    MAP,
    SETTINGS,
    ABOUT,
    PRIVACY,
    TERMS
}

@Composable
fun MapScreenContainer(
    onAttractionClick: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    var screenMode by remember { mutableStateOf(ScreenMode.MAP) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Animated content - exactly like Map/List toggle!
        AnimatedContent(
            targetState = screenMode,
            transitionSpec = {
                if (targetState == ScreenMode.MAP) {
                    // Going back to Map - slide in from left
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> width } + fadeOut()
                } else {
                    // Opening Settings/sub-screens - slide in from right
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { mode ->
            when (mode) {
                ScreenMode.MAP -> {
                    MapScreen(
                        onAttractionClick = onAttractionClick,
                        onNavigateToFavorites = onNavigateToFavorites,
                        onNavigateToSettings = {
                            screenMode = ScreenMode.SETTINGS // Switch to Settings overlay
                        }
                    )
                }
                ScreenMode.SETTINGS -> {
                    SettingsScreen(
                        onNavigateBack = {
                            screenMode = ScreenMode.MAP // Go back to Map
                        },
                        onNavigateToAbout = {
                            screenMode = ScreenMode.ABOUT
                        },
                        onNavigateToPrivacy = {
                            screenMode = ScreenMode.PRIVACY
                        },
                        onNavigateToTerms = {
                            screenMode = ScreenMode.TERMS
                        }
                    )
                }
                ScreenMode.ABOUT -> {
                    AboutScreen(
                        onNavigateBack = {
                            screenMode = ScreenMode.SETTINGS // Go back to Settings
                        }
                    )
                }
                ScreenMode.PRIVACY -> {
                    PrivacyPolicyScreen(
                        onNavigateBack = {
                            screenMode = ScreenMode.SETTINGS // Go back to Settings
                        }
                    )
                }
                ScreenMode.TERMS -> {
                    TermsOfUseScreen(
                        onNavigateBack = {
                            screenMode = ScreenMode.SETTINGS // Go back to Settings
                        }
                    )
                }
            }
        }
    }
}
