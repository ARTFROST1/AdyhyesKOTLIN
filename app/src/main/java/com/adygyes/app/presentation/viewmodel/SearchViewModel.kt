package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.domain.repository.AttractionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the search screen with real-time filtering
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AttractionRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategories = MutableStateFlow<Set<AttractionCategory>>(emptySet())
    val selectedCategories: StateFlow<Set<AttractionCategory>> = _selectedCategories.asStateFlow()
    
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()
    
    private val _uiState = MutableStateFlow<UiState>(
        UiState.Initial(
            recentSearches = getRecentSearches(),
            popularAttractions = emptyList()
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private var allAttractions: List<Attraction> = emptyList()
    
    init {
        loadInitialData()
        observeSearchQuery()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                repository.getAllAttractions().collect { attractions ->
                    allAttractions = attractions
                    val popular = attractions
                        .filter { it.rating != null }
                        .sortedByDescending { it.rating ?: 0f }
                        .take(5)
                    
                    _uiState.value = UiState.Initial(
                        recentSearches = getRecentSearches(),
                        popularAttractions = popular
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = UiState.Error(
                    message = exception.message ?: "Failed to load attractions"
                )
            }
        }
    }
    
    private fun observeSearchQuery() {
        searchQuery
            .debounce(200)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotEmpty()) {
                    updateSuggestions(query)
                } else {
                    _suggestions.value = emptyList()
                }
            }
            .launchIn(viewModelScope)
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun search() {
        val query = _searchQuery.value
        val categories = _selectedCategories.value
        
        if (query.isEmpty() && categories.isEmpty()) {
            // Reset to initial state
            _uiState.value = UiState.Initial(
                recentSearches = getRecentSearches(),
                popularAttractions = allAttractions
                    .filter { it.rating != null }
                    .sortedByDescending { it.rating }
                    .take(5)
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            // Simulate search delay
            kotlinx.coroutines.delay(300)
            
            val results = filterAttractions(query, categories)
            
            if (query.isNotEmpty()) {
                saveRecentSearch(query)
            }
            
            _uiState.value = UiState.Success(attractions = results)
        }
    }
    
    private fun filterAttractions(
        query: String,
        categories: Set<AttractionCategory>
    ): List<Attraction> {
        return allAttractions.filter { attraction ->
            val matchesQuery = if (query.isNotEmpty()) {
                attraction.name.contains(query, ignoreCase = true) ||
                attraction.description.contains(query, ignoreCase = true) ||
                attraction.tags.any { it.contains(query, ignoreCase = true) } ||
                attraction.category.displayName.contains(query, ignoreCase = true)
            } else {
                true
            }
            
            val matchesCategory = if (categories.isNotEmpty()) {
                categories.contains(attraction.category)
            } else {
                true
            }
            
            matchesQuery && matchesCategory
        }
    }
    
    fun toggleCategory(category: AttractionCategory) {
        _selectedCategories.update { current ->
            if (current.contains(category)) {
                current - category
            } else {
                current + category
            }
        }
    }
    
    fun clearFilters() {
        _selectedCategories.value = emptySet()
        if (_searchQuery.value.isNotEmpty()) {
            search()
        }
    }
    
    fun toggleFavorite(attractionId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is UiState.Success) {
                val updatedAttractions = currentState.attractions.map { attraction ->
                    if (attraction.id == attractionId) {
                        attraction.copy(isFavorite = !attraction.isFavorite)
                    } else {
                        attraction
                    }
                }
                
                // Update in repository
                val attraction = updatedAttractions.find { it.id == attractionId }
                attraction?.let {
                    repository.updateFavoriteStatus(attractionId, it.isFavorite)
                }
                
                // Update UI state
                _uiState.value = currentState.copy(attractions = updatedAttractions)
                
                // Update all attractions cache
                allAttractions = allAttractions.map { attr ->
                    if (attr.id == attractionId) {
                        attr.copy(isFavorite = attraction?.isFavorite ?: attr.isFavorite)
                    } else {
                        attr
                    }
                }
            }
        }
    }
    
    private fun updateSuggestions(query: String) {
        val suggestions = mutableListOf<String>()
        
        // Add attraction names
        suggestions.addAll(
            allAttractions
                .map { it.name }
                .filter { it.contains(query, ignoreCase = true) }
                .take(3)
        )
        
        // Add categories
        suggestions.addAll(
            AttractionCategory.values()
                .map { it.displayName }
                .filter { it.contains(query, ignoreCase = true) }
                .take(2)
        )
        
        // Add tags
        suggestions.addAll(
            allAttractions
                .flatMap { it.tags }
                .distinct()
                .filter { it.contains(query, ignoreCase = true) }
                .take(2)
        )
        
        _suggestions.value = suggestions.distinct().take(5)
    }
    
    private fun getRecentSearches(): List<String> {
        // TODO: Implement persistent storage for recent searches
        return listOf(
            "Waterfalls",
            "Хаджохская теснина",
            "Mountains",
            "Camping"
        )
    }
    
    private fun saveRecentSearch(query: String) {
        // TODO: Implement persistent storage for recent searches
    }
    
    sealed interface UiState {
        data class Initial(
            val recentSearches: List<String>,
            val popularAttractions: List<Attraction>
        ) : UiState
        
        data object Loading : UiState
        
        data class Success(
            val attractions: List<Attraction>
        ) : UiState
        
        data class Error(
            val message: String
        ) : UiState
    }
}
