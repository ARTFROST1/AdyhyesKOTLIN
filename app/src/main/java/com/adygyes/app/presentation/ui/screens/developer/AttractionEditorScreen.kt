package com.adygyes.app.presentation.ui.screens.developer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.viewmodel.DeveloperViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Screen for adding or editing an attraction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionEditorScreen(
    attractionId: String?,
    onNavigateBack: () -> Unit,
    viewModel: DeveloperViewModel = hiltViewModel()
) {
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(attractionId) {
        if (attractionId != null) {
            viewModel.loadAttractionForEdit(attractionId)
        } else {
            viewModel.initializeNewAttraction()
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.operationResult.collectLatest { result ->
            when (result) {
                is DeveloperViewModel.OperationResult.Success -> {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Short
                    )
                    onNavigateBack()
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
                title = {
                    Text(
                        if (editorState.isEditMode) "Edit Attraction" else "Add Attraction"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showDiscardDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    // Preview images
                    if (editorState.images.isNotBlank()) {
                        IconButton(onClick = { showPreviewDialog = true }) {
                            Icon(Icons.Default.Preview, contentDescription = "Preview Images")
                        }
                    }
                    
                    // Save button
                    TextButton(
                        onClick = { viewModel.saveAttraction() },
                        enabled = !editorState.isLoading
                    ) {
                        Text("SAVE")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (editorState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(Dimensions.PaddingMedium)
            ) {
                // Basic Information Section
                item {
                    SectionHeader(title = "Basic Information")
                }
                
                // ID (read-only)
                item {
                    OutlinedTextField(
                        value = editorState.id,
                        onValueChange = { },
                        label = { Text("ID") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Tag, contentDescription = null)
                        }
                    )
                }
                
                // Name
                item {
                    OutlinedTextField(
                        value = editorState.name,
                        onValueChange = { 
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.NAME, it)
                        },
                        label = { Text("Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Label, contentDescription = null)
                        },
                        isError = editorState.name.isBlank()
                    )
                }
                
                // Description
                item {
                    OutlinedTextField(
                        value = editorState.description,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.DESCRIPTION, it)
                        },
                        label = { Text("Description *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        minLines = 3,
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null)
                        },
                        isError = editorState.description.isBlank()
                    )
                }
                
                // Category
                item {
                    OutlinedTextField(
                        value = editorState.category.displayName,
                        onValueChange = { },
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { showCategoryDialog = true },
                        readOnly = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(Icons.Default.Category, contentDescription = null)
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                }
                
                // Location Section
                item {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                    SectionHeader(title = "Location")
                }
                
                // Coordinates
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editorState.latitude,
                            onValueChange = {
                                viewModel.updateEditorField(DeveloperViewModel.EditorField.LATITUDE, it)
                            },
                            label = { Text("Latitude *") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = {
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                            },
                            isError = editorState.latitude.isBlank()
                        )
                        
                        OutlinedTextField(
                            value = editorState.longitude,
                            onValueChange = {
                                viewModel.updateEditorField(DeveloperViewModel.EditorField.LONGITUDE, it)
                            },
                            label = { Text("Longitude *") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = {
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                            },
                            isError = editorState.longitude.isBlank()
                        )
                    }
                }
                
                // Address
                item {
                    OutlinedTextField(
                        value = editorState.address,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.ADDRESS, it)
                        },
                        label = { Text("Address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Home, contentDescription = null)
                        }
                    )
                }
                
                // Directions
                item {
                    OutlinedTextField(
                        value = editorState.directions,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.DIRECTIONS, it)
                        },
                        label = { Text("Directions") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Directions, contentDescription = null)
                        }
                    )
                }
                
                // Media Section
                item {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                    SectionHeader(title = "Media")
                }
                
                // Images
                item {
                    OutlinedTextField(
                        value = editorState.images,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.IMAGES, it)
                        },
                        label = { Text("Image URLs (one per line)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        minLines = 3,
                        leadingIcon = {
                            Icon(Icons.Default.Image, contentDescription = null)
                        },
                        supportingText = {
                            Text("Enter image URLs, one per line")
                        }
                    )
                }
                
                // Additional Info Section
                item {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                    SectionHeader(title = "Additional Information")
                }
                
                // Rating
                item {
                    OutlinedTextField(
                        value = editorState.rating,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.RATING, it)
                        },
                        label = { Text("Rating (0-5)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Icon(Icons.Default.Star, contentDescription = null)
                        }
                    )
                }
                
                // Working Hours
                item {
                    OutlinedTextField(
                        value = editorState.workingHours,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.WORKING_HOURS, it)
                        },
                        label = { Text("Working Hours") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                        }
                    )
                }
                
                // Price Info
                item {
                    OutlinedTextField(
                        value = editorState.priceInfo,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.PRICE_INFO, it)
                        },
                        label = { Text("Price Info") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null)
                        }
                    )
                }
                
                // Contact Section
                item {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                    SectionHeader(title = "Contact Information")
                }
                
                // Phone
                item {
                    OutlinedTextField(
                        value = editorState.phoneNumber,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.PHONE, it)
                        },
                        label = { Text("Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        }
                    )
                }
                
                // Email
                item {
                    OutlinedTextField(
                        value = editorState.email,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.EMAIL, it)
                        },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        }
                    )
                }
                
                // Website
                item {
                    OutlinedTextField(
                        value = editorState.website,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.WEBSITE, it)
                        },
                        label = { Text("Website") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        leadingIcon = {
                            Icon(Icons.Default.Language, contentDescription = null)
                        }
                    )
                }
                
                // Tags & Amenities Section
                item {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                    SectionHeader(title = "Tags & Amenities")
                }
                
                // Tags
                item {
                    OutlinedTextField(
                        value = editorState.tags,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.TAGS, it)
                        },
                        label = { Text("Tags (comma-separated)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        leadingIcon = {
                            Icon(Icons.Default.LocalOffer, contentDescription = null)
                        },
                        supportingText = {
                            Text("Example: природа, водопад, треккинг")
                        }
                    )
                }
                
                // Amenities
                item {
                    OutlinedTextField(
                        value = editorState.amenities,
                        onValueChange = {
                            viewModel.updateEditorField(DeveloperViewModel.EditorField.AMENITIES, it)
                        },
                        label = { Text("Amenities (comma-separated)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        },
                        supportingText = {
                            Text("Example: парковка, кафе, туалеты")
                        }
                    )
                }
                
                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
                }
            }
        }
    }
    
    // Category Selection Dialog
    if (showCategoryDialog) {
        CategorySelectionDialog(
            currentCategory = editorState.category,
            onCategorySelected = {
                viewModel.updateCategory(it)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
    
    // Preview Images Dialog
    if (showPreviewDialog) {
        ImagePreviewDialog(
            imageUrls = editorState.images.split("\n").map { it.trim() }.filter { it.isNotBlank() },
            onDismiss = { showPreviewDialog = false }
        )
    }
    
    // Discard Changes Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null)
            },
            title = { Text("Discard Changes?") },
            text = {
                Text("Are you sure you want to discard your changes?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun CategorySelectionDialog(
    currentCategory: AttractionCategory,
    onCategorySelected: (AttractionCategory) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Category") },
        text = {
            Column {
                AttractionCategory.values().forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = category == currentCategory,
                            onClick = { onCategorySelected(category) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = category.displayName,
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
private fun ImagePreviewDialog(
    imageUrls: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Image Preview") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                imageUrls.forEachIndexed { index, url ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column {
                            Text(
                                text = "Image ${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(8.dp)
                            )
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Image preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = url,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
