package com.adygyes.app.presentation.ui.screens.favorites

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adygyes.app.R
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.ui.components.*
import com.adygyes.app.presentation.viewmodel.FavoritesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Favorites screen showing user's saved attractions
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    onAttractionClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_favorites)) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    // View mode toggle
                    IconButton(
                        onClick = { viewModel.toggleViewMode() }
                    ) {
                        Icon(
                            imageVector = if (viewMode == FavoritesViewModel.ViewMode.GRID) {
                                Icons.Default.ViewList
                            } else {
                                Icons.Default.GridView
                            },
                            contentDescription = "Toggle view"
                        )
                    }
                    
                    // Sort menu
                    var showSortMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            FavoritesViewModel.SortBy.values().forEach { sortOption ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(sortOption.displayName)
                                            if (sortBy == sortOption) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setSortBy(sortOption)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is FavoritesViewModel.UiState.Loading -> {
                    LoadingShimmerList(
                        itemCount = 8,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                is FavoritesViewModel.UiState.Success -> {
                    if (state.favorites.isEmpty()) {
                        NoFavoritesState(
                            onExplore = onExploreClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column {
                            // Stats bar
                            FavoritesStatsBar(
                                totalCount = state.favorites.size,
                                categoryCounts = state.favorites.groupBy { it.category }.mapValues { it.value.size },
                                modifier = Modifier.padding(
                                    horizontal = Dimensions.PaddingMedium,
                                    vertical = Dimensions.PaddingSmall
                                )
                            )
                            
                            // Content based on view mode
                            when (viewMode) {
                                FavoritesViewModel.ViewMode.LIST -> {
                                    FavoritesListView(
                                        favorites = state.favorites,
                                        onAttractionClick = onAttractionClick,
                                        onRemoveFavorite = { attractionId ->
                                            viewModel.removeFavorite(attractionId)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                
                                FavoritesViewModel.ViewMode.GRID -> {
                                    FavoritesGridView(
                                        favorites = state.favorites,
                                        onAttractionClick = onAttractionClick,
                                        onRemoveFavorite = { attractionId ->
                                            viewModel.removeFavorite(attractionId)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
                
                is FavoritesViewModel.UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadFavorites() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
        }
    }
}

@Composable
private fun FavoritesListView(
    favorites: List<Attraction>,
    onAttractionClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = Dimensions.PaddingMedium,
            vertical = Dimensions.PaddingMedium
        ),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
    ) {
        items(
            items = favorites,
            key = { it.id }
        ) { attraction ->
            SwipeToDeleteContainer(
                onDelete = { onRemoveFavorite(attraction.id) },
                modifier = Modifier
            ) {
                AttractionListItem(
                    attraction = attraction,
                    onClick = { onAttractionClick(attraction.id) },
                    onFavoriteClick = { onRemoveFavorite(attraction.id) }
                )
            }
        }
    }
}

@Composable
private fun FavoritesGridView(
    favorites: List<Attraction>,
    onAttractionClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(Dimensions.PaddingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingSmall)
    ) {
        items(
            items = favorites,
            key = { it.id }
        ) { attraction ->
            AttractionCard(
                attraction = attraction,
                onClick = { onAttractionClick(attraction.id) },
                onFavoriteClick = { onRemoveFavorite(attraction.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun FavoritesStatsBar(
    totalCount: Int,
    categoryCounts: Map<com.adygyes.app.domain.model.AttractionCategory, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingMedium)
        ) {
            Text(
                text = "You have $totalCount favorite${if (totalCount != 1) "s" else ""}",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (categoryCounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryCounts.entries
                        .sortedByDescending { it.value }
                        .take(3)
                        .forEach { (category, count) ->
                            CategoryChip(
                                category = category,
                                compact = true,
                                modifier = Modifier
                            )
                            Text(
                                text = "($count)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = Dimensions.PaddingLarge),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    ) {
        content()
    }
}