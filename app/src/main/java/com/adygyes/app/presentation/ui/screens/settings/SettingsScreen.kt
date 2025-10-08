package com.adygyes.app.presentation.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adygyes.app.R
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.viewmodel.SettingsViewModel
import com.adygyes.app.presentation.ui.components.RatingComingSoonDialog
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import com.adygyes.app.presentation.ui.util.EasterEggManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Settings screen for app configuration and preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    // Easter egg: 7 taps on title
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    // Protection against double-click on back button - prevents multiple popBackStack calls
    var isNavigating by remember { mutableStateOf(false) }
    
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_settings),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable {
                            val now = System.currentTimeMillis()
                            // Reset sequence if too slow between taps
                            if (now - lastTapTime > 1500) tapCount = 0
                            lastTapTime = now
                            tapCount += 1
                            if (tapCount >= 7) {
                                tapCount = 0
                                EasterEggManager.activate()
                                Toast.makeText(
                                    context,
                                    "Пасхалка активирована: фон карты заменён на фото до перезапуска",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { 
                            if (!isNavigating) {
                                isNavigating = true
                                onNavigateBack()
                                // Reset flag after navigation completes (longer than animation)
                                coroutineScope.launch {
                                    delay(500)
                                    isNavigating = false
                                }
                            }
                        },
                        enabled = !isNavigating
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.search_back),
                            tint = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (!isNavigating) 1f else 0.5f
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = Dimensions.PaddingSmall)
        ) {
            // Appearance Section
            item {
                SettingsSectionHeader(title = stringResource(R.string.settings_appearance))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.settings_theme),
                    subtitle = when (uiState.theme) {
                        SettingsViewModel.Theme.LIGHT -> stringResource(R.string.settings_theme_light)
                        SettingsViewModel.Theme.DARK -> stringResource(R.string.settings_theme_dark)
                        SettingsViewModel.Theme.SYSTEM -> stringResource(R.string.settings_theme_system)
                    },
                    onClick = { showThemeDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = when (uiState.language) {
                        SettingsViewModel.Language.RUSSIAN -> stringResource(R.string.settings_language_ru)
                        SettingsViewModel.Language.ENGLISH -> stringResource(R.string.settings_language_en)
                    },
                    onClick = { showLanguageDialog = true }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }
            
            // Map Settings Section
            item {
                SettingsSectionHeader(title = stringResource(R.string.settings_map))
            }
            
            item {
                SettingsItemSwitch(
                    icon = Icons.Default.MyLocation,
                    title = stringResource(R.string.settings_show_location),
                    subtitle = stringResource(R.string.settings_show_location_desc),
                    checked = uiState.showUserLocation,
                    onCheckedChange = { enabled -> viewModel.setShowUserLocation(enabled) }
                )
            }
            
            
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }
            
            // Notifications Section
            item {
                SettingsSectionHeader(title = stringResource(R.string.settings_notifications))
            }
            
            item {
                SettingsItemSwitch(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.settings_push_notifications),
                    subtitle = stringResource(R.string.settings_push_notifications_desc),
                    checked = uiState.pushNotifications,
                    onCheckedChange = { enabled -> viewModel.setPushNotifications(enabled) }
                )
            }
            
            
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }
            
            // About Section
            item {
                SettingsSectionHeader(title = stringResource(R.string.settings_about))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_about_app),
                    subtitle = "${stringResource(R.string.settings_version)} ${uiState.appVersion}",
                    onClick = {
                        viewModel.onVersionClick()
                        onNavigateToAbout()
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = stringResource(R.string.settings_privacy),
                    subtitle = stringResource(R.string.settings_privacy_desc),
                    onClick = onNavigateToPrivacy
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Article,
                    title = stringResource(R.string.settings_terms),
                    subtitle = stringResource(R.string.settings_terms_desc),
                    onClick = onNavigateToTerms
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.RateReview,
                    title = stringResource(R.string.settings_rate_us),
                    subtitle = stringResource(R.string.settings_rate_us_desc),
                    onClick = { showRatingDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = stringResource(R.string.settings_share_app),
                    subtitle = stringResource(R.string.settings_share_app_desc),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/MaykopTech"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handle error - fallback to sharing text
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Попробуйте AdygGis - приложение для изучения достопримечательностей Адыгеи!")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Поделиться приложением"))
                        }
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
            }
        }
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = uiState.language,
            onLanguageSelected = { language ->
                viewModel.setLanguage(language)
                showLanguageDialog = false
                // Delay to allow data to save, then recreate activity
                coroutineScope.launch {
                    delay(200)
                    (context as? android.app.Activity)?.recreate()
                }
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.theme,
            onThemeSelected = { theme ->
                viewModel.setTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // Rating Coming Soon Dialog
    RatingComingSoonDialog(
        isVisible = showRatingDialog,
        onDismiss = { showRatingDialog = false }
    )
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: SettingsViewModel.Language,
    onLanguageSelected: (SettingsViewModel.Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_select_language)) },
        text = {
            Column {
                SettingsViewModel.Language.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == currentLanguage,
                            onClick = { onLanguageSelected(language) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (language) {
                                SettingsViewModel.Language.RUSSIAN -> stringResource(R.string.settings_language_ru)
                                SettingsViewModel.Language.ENGLISH -> stringResource(R.string.settings_language_en)
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: SettingsViewModel.Theme,
    onThemeSelected: (SettingsViewModel.Theme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_select_theme)) },
        text = {
            Column {
                SettingsViewModel.Theme.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (theme) {
                                    SettingsViewModel.Theme.LIGHT -> stringResource(R.string.settings_theme_light)
                                    SettingsViewModel.Theme.DARK -> stringResource(R.string.settings_theme_dark)
                                    SettingsViewModel.Theme.SYSTEM -> stringResource(R.string.settings_theme_system)
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (theme) {
                                    SettingsViewModel.Theme.LIGHT -> stringResource(R.string.settings_theme_light_desc)
                                    SettingsViewModel.Theme.DARK -> stringResource(R.string.settings_theme_dark_desc)
                                    SettingsViewModel.Theme.SYSTEM -> stringResource(R.string.settings_theme_system_desc)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
