package com.adygyes.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.adygyes.app.data.local.locale.LocaleManager
import com.adygyes.app.presentation.navigation.AdygyesNavHost
import com.adygyes.app.presentation.theme.AdygyesTheme
import com.adygyes.app.presentation.viewmodel.LocaleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main activity that hosts the Compose UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var localeManager: LocaleManager
    
    private var currentLanguage: String = LocaleManager.DEFAULT_LANGUAGE
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Get current language from preferences
        lifecycleScope.launch {
            currentLanguage = localeManager.currentLanguage.first()
        }
        
        setContent {
            AdygyesTheme {
                AdygyesApp()
            }
        }
    }
    
    override fun attachBaseContext(newBase: Context?) {
        if (newBase == null) {
            super.attachBaseContext(newBase)
            return
        }
        
        // Apply saved language preference
        val contextWithLocale = if (::localeManager.isInitialized) {
            localeManager.applyLocale(newBase, currentLanguage)
        } else {
            // Use static method for initial setup
            applyStaticLocale(newBase, LocaleManager.DEFAULT_LANGUAGE)
        }
        
        super.attachBaseContext(contextWithLocale)
    }
    
    /**
     * Restart activity to apply language changes
     */
    fun restartForLanguageChange() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
    val localeViewModel: LocaleViewModel = hiltViewModel()
    val currentLanguage by localeViewModel.currentLanguage.collectAsState()
    val context = LocalContext.current
    
    // Track language changes and restart activity when needed
    var initialLanguage by remember { mutableStateOf(currentLanguage) }
    
    LaunchedEffect(currentLanguage) {
        if (initialLanguage != currentLanguage && initialLanguage != LocaleManager.DEFAULT_LANGUAGE) {
            // Language has changed, restart activity
            (context as? MainActivity)?.restartForLanguageChange()
        } else {
            initialLanguage = currentLanguage
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        AdygyesNavHost(
            navController = navController,
            paddingValues = PaddingValues(0.dp)
        )
    }
}
