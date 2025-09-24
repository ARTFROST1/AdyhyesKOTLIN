package com.adygyes.app.presentation.ui.screens.developer

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.viewmodel.DeveloperViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Developer Mode screen for managing attractions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String?) -> Unit,
    viewModel: DeveloperViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val attractions by viewModel.attractions.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var attractionToDelete by remember { mutableStateOf<Attraction?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.operationResult.collectLatest { result ->
            when (result) {
                is DeveloperViewModel.OperationResult.Success -> {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is DeveloperViewModel.OperationResult.Error -> {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Long,
                        actionLabel = "OK"
                    )
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Mode") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Copy to Clipboard
                    IconButton(
                        onClick = { viewModel.getJsonForClipboard(clipboardManager) }
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy to Clipboard")
                    }
                    
                    // Export to Downloads
                    IconButton(
                        onClick = { showExportDialog = true }
                    ) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = "Export Options")
                    }
                    
                    // Reload from JSON
                    IconButton(
                        onClick = { viewModel.reloadFromJson() },
                        enabled = !uiState.isReloading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload from JSON")
                    }
                    
                    // Reset to original
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset to Original")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Attraction")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isReloading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (attractions.isEmpty()) {
                EmptyState(
                    onAddClick = { onNavigateToEditor(null) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = Dimensions.PaddingSmall)
                ) {
                    // Stats Card
                    item {
                        StatsCard(
                            attractionsCount = attractions.size,
                            autoSaveStatus = uiState.autoSaveStatus,
                            projectPath = uiState.projectPath
                        )
                    }
                    
                    // Attractions List
                    items(attractions) { attraction ->
                        AttractionDeveloperItem(
                            attraction = attraction,
                            onEditClick = { onNavigateToEditor(attraction.id) },
                            onDeleteClick = {
                                attractionToDelete = attraction
                                showDeleteDialog = true
                            }
                        )
                    }
                    
                    // Bottom padding for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && attractionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                attractionToDelete = null
            },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null)
            },
            title = { Text("Delete Attraction?") },
            text = {
                Text("Are you sure you want to delete \"${attractionToDelete?.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        attractionToDelete?.let { viewModel.deleteAttraction(it.id) }
                        showDeleteDialog = false
                        attractionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        attractionToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Reset to Original Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
            },
            title = { Text("Reset to Original?") },
            text = {
                Text("This will reset all attractions to the original JSON data from assets. All changes will be lost.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToOriginal()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export JSON Data") },
            text = {
                Column {
                    Text("Choose how to export your custom attractions data:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "• Copy to Clipboard - Paste into attractions.json",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Save to Downloads - Find file and copy to project",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Important: Replace app/src/main/assets/attractions.json with the exported data to make changes permanent in your project.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.exportToDownloads()
                        showExportDialog = false
                    }
                ) {
                    Text("Save to Downloads")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatsCard(
    attractionsCount: Int,
    autoSaveStatus: com.adygyes.app.presentation.viewmodel.DeveloperViewModel.AutoSaveStatus,
    projectPath: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingMedium)
        ) {
            // Основная статистика
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = attractionsCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total Attractions",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Статус автосохранения
            AutoSaveStatusIndicator(
                status = autoSaveStatus,
                projectPath = projectPath
            )
        }
    }
}

@Composable
private fun AttractionDeveloperItem(
    attraction: Attraction,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingSmall)
            .clickable { onEditClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on category
            Icon(
                imageVector = getCategoryIcon(attraction.category.name),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))
            
            // Attraction Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ID: ${attraction.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = attraction.category.displayName,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
                Text(
                    text = attraction.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = attraction.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.4f, %.4f".format(
                            attraction.location.latitude,
                            attraction.location.longitude
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (attraction.images.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${attraction.images.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Action Buttons
            Column {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Explore,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        Text(
            text = "No Attractions",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Add your first attraction to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Attraction")
        }
    }
}

private fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "NATURE" -> Icons.Default.Park
        "CULTURE" -> Icons.Default.AccountBalance
        "HISTORY" -> Icons.Default.AccountBalance
        "ADVENTURE" -> Icons.Default.DirectionsBike
        "RECREATION" -> Icons.Default.Pool
        "GASTRONOMY" -> Icons.Default.Restaurant
        "RELIGIOUS" -> Icons.Default.AccountBalance
        "ENTERTAINMENT" -> Icons.Default.Celebration
        else -> Icons.Default.Place
    }
}

@Composable
private fun AutoSaveStatusIndicator(
    status: com.adygyes.app.presentation.viewmodel.DeveloperViewModel.AutoSaveStatus,
    projectPath: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, text, color) = when (status) {
            com.adygyes.app.presentation.viewmodel.DeveloperViewModel.AutoSaveStatus.IDLE -> {
                Triple(
                    Icons.Default.CheckCircle,
                    "Auto-save: Ready",
                    MaterialTheme.colorScheme.primary
                )
            }
            com.adygyes.app.presentation.viewmodel.DeveloperViewModel.AutoSaveStatus.SAVING -> {
                Triple(
                    Icons.Default.Sync,
                    "Auto-save: Saving...",
                    MaterialTheme.colorScheme.primary
                )
            }
            com.adygyes.app.presentation.viewmodel.DeveloperViewModel.AutoSaveStatus.SUCCESS -> {
                Triple(
                    Icons.Default.CheckCircle,
                    "Auto-save: Saved to project",
                    Color(0xFF4CAF50)
                )
            }
            com.adygyes.app.presentation.viewmodel.DeveloperViewModel.AutoSaveStatus.FAILED -> {
                Triple(
                    Icons.Default.Error,
                    "Auto-save: Failed",
                    MaterialTheme.colorScheme.error
                )
            }
            com.adygyes.app.presentation.viewmodel.DeveloperViewModel.AutoSaveStatus.PROJECT_NOT_FOUND -> {
                Triple(
                    Icons.Default.Warning,
                    "Auto-save: Project not found",
                    MaterialTheme.colorScheme.error
                )
            }
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
        
        if (projectPath != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "• ${projectPath.substringAfterLast("/")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
