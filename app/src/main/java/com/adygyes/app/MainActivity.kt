package com.adygyes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.adygyes.app.presentation.navigation.AdygyesNavHost
import com.adygyes.app.presentation.theme.AdygyesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity that hosts the Compose UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AdygyesTheme {
                AdygyesApp()
            }
        }
    }
}

@Composable
fun AdygyesApp() {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AdygyesNavHost(
            navController = navController,
            paddingValues = innerPadding
        )
    }
}
