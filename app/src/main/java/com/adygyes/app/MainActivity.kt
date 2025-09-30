package com.adygyes.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.adygyes.app.data.local.locale.LocaleManager
import com.adygyes.app.presentation.navigation.AdygyesNavHost
import com.adygyes.app.presentation.ui.screens.map.MapHost
import com.adygyes.app.presentation.theme.AdygyesTheme
import com.adygyes.app.presentation.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import android.os.Build
import android.graphics.Color as AndroidColor

/**
 * Main activity that hosts the Compose UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private fun setupEdgeToEdge() {
        enableEdgeToEdge()
        // Prevent Android from adding a contrast-enforcing translucent scrim over the navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        // Remove navigation bar divider line on Android 9+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = AndroidColor.TRANSPARENT
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
        
        setContent {
            // Observe theme mode from preferences
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            AdygyesTheme(darkTheme = darkTheme) {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    // Make system bars fully transparent with proper icon colors
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !darkTheme
                    )
                }
                AdygyesApp()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Re-apply edge-to-edge to ensure it's not lost after config changes
        setupEdgeToEdge()
    }
    
    override fun attachBaseContext(newBase: Context?) {
        if (newBase == null) {
            super.attachBaseContext(newBase)
            return
        }
        
        // Get saved language preference synchronously from SharedPreferences
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("language", LocaleManager.DEFAULT_LANGUAGE) ?: LocaleManager.DEFAULT_LANGUAGE
        
        android.util.Log.d("MainActivity", "attachBaseContext: applying language=$savedLanguage")
        
        // Apply saved language preference
        val contextWithLocale = applyStaticLocale(newBase, savedLanguage)
        
        super.attachBaseContext(contextWithLocale)
    }
    
    /**
     * Static method to apply locale without LocaleManager dependency
     */
    private fun applyStaticLocale(context: Context, languageCode: String): Context {
        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)
        
        val configuration = android.content.res.Configuration(context.resources.configuration)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            configuration.setLocales(android.os.LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
}

@Composable
fun AdygyesApp() {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        // MapHost now accepts content and provides LocalMapHostController to it
        MapHost(modifier = Modifier.fillMaxSize()) {
            AdygyesNavHost(
                navController = navController,
                paddingValues = PaddingValues(0.dp)
            )
        }
    }
}
