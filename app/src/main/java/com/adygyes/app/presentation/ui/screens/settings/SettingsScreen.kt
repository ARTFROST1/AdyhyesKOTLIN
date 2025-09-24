package com.adygyes.app.presentation.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adygyes.app.R
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.viewmodel.SettingsViewModel

/**
 * Settings screen for app configuration and preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: (() -> Unit)? = null,
    onAboutClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onDeveloperModeClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val developerModeEnabled by viewModel.developerModeEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCacheDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
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
                SettingsSectionHeader(title = "Appearance")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = uiState.theme.displayName,
                    onClick = { showThemeDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = uiState.language.displayName,
                    onClick = { showLanguageDialog = true }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }
            
            // Map Settings Section
            item {
                SettingsSectionHeader(title = "Map Settings")
            }
            
            item {
                SettingsItemSwitch(
                    icon = Icons.Default.MyLocation,
                    title = "Show My Location",
                    subtitle = "Display your current location on the map",
                    checked = uiState.showUserLocation,
                    onCheckedChange = { viewModel.setShowUserLocation(it) }
                )
            }
            
            item {
                SettingsItemSwitch(
                    icon = Icons.Default.GroupWork,
                    title = "Cluster Markers",
                    subtitle = "Group nearby markers when zoomed out",
                    checked = uiState.clusterMarkers,
                    onCheckedChange = { viewModel.setClusterMarkers(it) }
                )
            }
            
            item {
                SettingsItemSwitch(
                    icon = Icons.Default.Traffic,
                    title = "Show Traffic",
                    subtitle = "Display traffic information on the map",
                    checked = uiState.showTraffic,
                    onCheckedChange = { viewModel.setShowTraffic(it) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }
            
            // Data & Storage Section
            item {
                SettingsSectionHeader(title = "Data & Storage")
            }
            
            item {
                SettingsItemSwitch(
                    icon = Icons.Default.CloudOff,
                    title = "Offline Mode",
                    subtitle = "Use cached data when possible",
                    checked = uiState.offlineMode,
                    onCheckedChange = { viewModel.setOfflineMode(it) }
                )
            }
            
            item {
                SettingsItemSwitch(
                    icon = Icons.Default.PhotoLibrary,
                    title = "Auto-download Images",
                    subtitle = "Download images for offline viewing",
                    checked = uiState.autoDownloadImages,
                    onCheckedChange = { viewModel.setAutoDownloadImages(it) },
                    enabled = !uiState.offlineMode
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Clear Cache",
                    subtitle = "Free up storage space",
                    onClick = { showCacheDialog = true }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }
            
            // Notifications Section
            item {
                SettingsSectionHeader(title = "Notifications")
            }
            
            item {
                SettingsItemSwitch(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive updates about new attractions",
                    checked = uiState.pushNotifications,
                    onCheckedChange = { viewModel.setPushNotifications(it) }
                )
            }
            
            item {
                SettingsItemSwitch(
                    icon = Icons.Default.LocationOn,
                    title = "Location-based Alerts",
                    subtitle = "Notify when near attractions",
                    checked = uiState.locationAlerts,
                    onCheckedChange = { viewModel.setLocationAlerts(it) },
                    enabled = uiState.pushNotifications
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }
            
            // About Section
            item {
                SettingsSectionHeader(title = "About")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About Adygyes",
                    subtitle = "Version ${uiState.appVersion}",
                    onClick = {
                        viewModel.onVersionClick()
                        onAboutClick()
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Learn how we protect your data",
                    onClick = onPrivacyClick
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Article,
                    title = "Terms of Service",
                    subtitle = "Read our terms and conditions",
                    onClick = onTermsClick
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.RateReview,
                    title = "Rate Us",
                    subtitle = "Share your feedback on Google Play",
                    onClick = { /* Open Play Store */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    subtitle = "Recommend Adygyes to friends",
                    onClick = { /* Share app link */ }
                )
            }
            
            // Developer Mode Section (only visible when enabled)
            if (developerModeEnabled) {
                item {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                }
                
                item {
                    SettingsSectionHeader(title = "Developer Mode")
                }
                
                item {
                    SettingsItem(
                        icon = Icons.Default.Code,
                        title = "Developer Tools",
                        subtitle = "Manage attractions and data",
                        onClick = onDeveloperModeClick
                    )
                }
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
    
    // Clear Cache Confirmation Dialog
    if (showCacheDialog) {
        AlertDialog(
            onDismissRequest = { showCacheDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null
                )
            },
            title = { Text("Clear Cache?") },
            text = {
                Text("This will delete all cached data including offline maps and images. You'll need an internet connection to reload this data.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCache()
                        showCacheDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimensions.PaddingLarge,
                vertical = Dimensions.PaddingSmall
            )
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            
            Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsItemSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        
        Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: SettingsViewModel.Language,
    onLanguageSelected: (SettingsViewModel.Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
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
                            text = language.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
        title = { Text("Select Theme") },
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
                                text = theme.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (theme) {
                                    SettingsViewModel.Theme.LIGHT -> "Always use light theme"
                                    SettingsViewModel.Theme.DARK -> "Always use dark theme"
                                    SettingsViewModel.Theme.SYSTEM -> "Follow system settings"
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
                Text("Cancel")
            }
        }
    )
}