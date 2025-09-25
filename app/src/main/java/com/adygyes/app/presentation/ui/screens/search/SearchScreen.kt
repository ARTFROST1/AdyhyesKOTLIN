package com.adygyes.app.presentation.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adygyes.app.R
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.presentation.theme.Dimensions
import com.adygyes.app.presentation.ui.components.*
import com.adygyes.app.presentation.viewmodel.SearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Search screen with real-time filtering and category selection
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onAttractionClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var showFilterSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_search)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Clear filters button
                    if (selectedCategories.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearFilters() }
                        ) {
                            Text("Clear")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    viewModel.updateSearchQuery(query)
                    // Debounce search
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        delay(300)
                        viewModel.search()
                    }
                },
                onSearch = {
                    keyboardController?.hide()
                    viewModel.search()
                },
                onFilterClick = { showFilterSheet = true },
                hasActiveFilters = selectedCategories.isNotEmpty(),
                placeholder = "Search attractions...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.PaddingMedium)
                    .padding(top = Dimensions.PaddingMedium),
                focusOnStart = true
            )
            
            // Quick category filters
            if (selectedCategories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.PaddingSmall),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = Dimensions.PaddingMedium)
                ) {
                    items(selectedCategories.toList()) { category ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.toggleCategory(category) },
                            enabled = true,
                            label = { Text(category.displayName) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
            
            // Search suggestions
            AnimatedVisibility(
                visible = suggestions.isNotEmpty() && searchQuery.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SearchSuggestions(
                    suggestions = suggestions,
                    onSuggestionClick = { suggestion ->
                        viewModel.updateSearchQuery(suggestion)
                        viewModel.search()
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.PaddingMedium)
                        .padding(top = Dimensions.PaddingSmall),
                    currentQuery = searchQuery
                )
            }
            
            // Results
            when (val state = uiState) {
                is SearchViewModel.UiState.Loading -> {
                    LoadingShimmerList(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = Dimensions.PaddingMedium)
                    )
                }
                
                is SearchViewModel.UiState.Success -> {
                    if (state.attractions.isEmpty()) {
                        NoSearchResultsState(
                            query = searchQuery,
                            onClearFilters = if (selectedCategories.isNotEmpty()) {
                                { viewModel.clearFilters() }
                            } else null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        SearchResultsList(
                            attractions = state.attractions,
                            onAttractionClick = onAttractionClick,
                            onFavoriteClick = { attractionId ->
                                viewModel.toggleFavorite(attractionId)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Results count
                        Text(
                            text = "${state.attractions.size} results found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = Dimensions.PaddingMedium)
                                .padding(top = Dimensions.PaddingSmall)
                        )
                    }
                }
                
                is SearchViewModel.UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.search() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                is SearchViewModel.UiState.Initial -> {
                    // Initial state - show recent searches or suggestions
                    InitialSearchState(
                        recentSearches = state.recentSearches,
                        popularAttractions = state.popularAttractions,
                        onSearchClick = { query ->
                            viewModel.updateSearchQuery(query)
                            viewModel.search()
                        },
                        onAttractionClick = onAttractionClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    
    // Filter bottom sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            selectedCategories = selectedCategories,
            onCategoryToggle = { category ->
                viewModel.toggleCategory(category)
            },
            onApply = {
                showFilterSheet = false
                viewModel.search()
            },
            onDismiss = { showFilterSheet = false },
            onClearAll = { viewModel.clearFilters() }
        )
    }
}

@Composable
private fun SearchResultsList(
    attractions: List<Attraction>,
    onAttractionClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
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
            items = attractions,
            key = { it.id }
        ) { attraction ->
            AttractionListItem(
                attraction = attraction,
                onClick = { onAttractionClick(attraction.id) },
                onFavoriteClick = { onFavoriteClick(attraction.id) }
            )
        }
    }
}

@Composable
private fun InitialSearchState(
    recentSearches: List<String>,
    popularAttractions: List<Attraction>,
    onSearchClick: (String) -> Unit,
    onAttractionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(Dimensions.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
    ) {
        // Recent searches
        if (recentSearches.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.search_recent),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Dimensions.PaddingSmall)
                )
            }
            
            items(recentSearches) { search ->
                Surface(
                    onClick = { onSearchClick(search) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.PaddingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = search,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
            }
        }
        
        // Popular attractions
        if (popularAttractions.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.search_popular),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Dimensions.PaddingSmall)
                )
            }
            
            items(
                items = popularAttractions,
                key = { it.id }
            ) { attraction ->
                AttractionCard(
                    attraction = attraction,
                    onClick = { onAttractionClick(attraction.id) },
                    onFavoriteClick = { },
                    modifier = Modifier.padding(vertical = Dimensions.PaddingExtraSmall)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedCategories: Set<AttractionCategory>,
    onCategoryToggle: (AttractionCategory) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingLarge)
                .padding(bottom = Dimensions.PaddingExtraLarge)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                TextButton(
                    onClick = onClearAll,
                    enabled = selectedCategories.isNotEmpty()
                ) {
                    Text(stringResource(R.string.clear_all))
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            
            // Category filters
            CategoryFilterChips(
                selectedCategories = selectedCategories,
                onCategoryToggle = onCategoryToggle,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
            
            // Apply button
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.apply_filters))
            }
        }
    }
}