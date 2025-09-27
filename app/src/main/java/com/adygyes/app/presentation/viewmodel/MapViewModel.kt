package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.domain.repository.AttractionRepository
import com.adygyes.app.domain.usecase.GetLocationUseCase
import com.adygyes.app.domain.usecase.NavigationUseCase
import com.adygyes.app.domain.usecase.ShareUseCase
import com.adygyes.app.domain.usecase.NetworkUseCase
import com.adygyes.app.domain.usecase.AttractionDisplayUseCase
import com.adygyes.app.domain.usecase.SortCriteria
import com.adygyes.app.domain.usecase.NetworkStatus
import com.adygyes.app.presentation.ui.map.markers.MarkerOverlayState
import com.adygyes.app.presentation.ui.map.markers.MarkerState
import com.adygyes.app.presentation.ui.components.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Map screen
 * Enhanced with new marker overlay system for 100% reliable clicks
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val attractionRepository: AttractionRepository,
    private val getLocationUseCase: GetLocationUseCase,
    private val navigationUseCase: NavigationUseCase,
    private val shareUseCase: ShareUseCase,
    private val networkUseCase: NetworkUseCase,
    private val attractionDisplayUseCase: AttractionDisplayUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    // New marker overlay state for the improved marker system
    private val _markerOverlayState = MutableStateFlow(MarkerOverlayState())
    val markerOverlayState: StateFlow<MarkerOverlayState> = _markerOverlayState.asStateFlow()
    
    private val _attractions = MutableStateFlow<List<Attraction>>(emptyList())
    val attractions: StateFlow<List<Attraction>> = _attractions.asStateFlow()
    
    private val _selectedAttraction = MutableStateFlow<Attraction?>(null)
    val selectedAttraction: StateFlow<Attraction?> = _selectedAttraction.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategories = MutableStateFlow<Set<AttractionCategory>>(emptySet())
    val selectedCategories: StateFlow<Set<AttractionCategory>> = _selectedCategories.asStateFlow()
    
    // New: Selected category for carousel
    private val _selectedCategoryFilter = MutableStateFlow<CategoryFilter>(CategoryFilter.All)
    val selectedCategoryFilter: StateFlow<CategoryFilter> = _selectedCategoryFilter.asStateFlow()
    
    // New: Sort options for list view
    private val _sortBy = MutableStateFlow(SortBy.NAME)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()
    
    // New: View mode for list (LIST or GRID)
    private val _listViewMode = MutableStateFlow(ListViewMode.LIST)
    val listViewMode: StateFlow<ListViewMode> = _listViewMode.asStateFlow()
    
    // Main view mode (MAP or LIST)
    private val _viewMode = MutableStateFlow(ViewMode.MAP)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()
    
    val filteredAttractions: StateFlow<List<Attraction>> = combine(
        _attractions,
        _searchQuery,
        _selectedCategoryFilter,
        _sortBy
    ) { attractions, query, categoryFilter, sortBy ->
        var filtered = attractions
        
        // Apply search filter
        if (query.isNotEmpty()) {
            filtered = filtered.filter { attraction ->
                attraction.name.contains(query, ignoreCase = true) ||
                attraction.description.contains(query, ignoreCase = true) ||
                attraction.tags.any { it.contains(query, ignoreCase = true) }
            }
        }
        
        // Apply category filter from carousel
        filtered = when (categoryFilter) {
            is CategoryFilter.All -> filtered
            is CategoryFilter.Favorites -> filtered.filter { it.isFavorite }
            is CategoryFilter.Category -> filtered.filter { it.category == categoryFilter.category }
        }
        
        // Apply sorting
        filtered = when (sortBy) {
            SortBy.NAME -> filtered.sortedBy { it.name }
            SortBy.CATEGORY -> filtered.sortedBy { it.category.displayName }
            SortBy.RATING -> filtered.sortedByDescending { it.rating ?: 0f }
            SortBy.DISTANCE -> {
                // TODO: Implement distance sorting when user location is available
                filtered
            }
        }
        
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        loadAttractions()
        checkAndLoadInitialData()
    }
    
    private fun checkAndLoadInitialData() {
        viewModelScope.launch {
            try {
                // Always call loadInitialData() - it now handles version checking internally
                Timber.d("Checking data version and loading if needed...")
                attractionRepository.loadInitialData()
            } catch (e: Exception) {
                Timber.e(e, "Error loading initial data")
            }
        }
    }
    
    private fun loadAttractions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            attractionRepository.getAllAttractions()
                .catch { error ->
                    Timber.e(error, "Error loading attractions")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
                .collect { attractionList ->
                    _attractions.value = attractionList
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    Timber.d("Loaded ${attractionList.size} attractions")
                }
        }
    }
    
    fun selectAttraction(attraction: Attraction) {
        Timber.d("Selecting attraction: ${attraction.name}")
        _selectedAttraction.value = attraction
        _uiState.update { it.copy(showAttractionDetail = true) }
    }
    
    fun dismissAttractionDetail() {
        _uiState.update { it.copy(showAttractionDetail = false) }
    }
    
    fun filterByCategory(category: AttractionCategory?) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedCategory = category) }
            
            if (category == null) {
                loadAttractions()
            } else {
                attractionRepository.getAttractionsByCategory(category)
                    .catch { error ->
                        Timber.e(error, "Error filtering attractions")
                    }
                    .collect { filteredList ->
                        _attractions.value = filteredList
                    }
            }
        }
    }
    
    fun toggleFavorite(attractionId: String) {
        viewModelScope.launch {
            val attraction = _attractions.value.find { it.id == attractionId }
            attraction?.let {
                val newFavoriteStatus = !it.isFavorite
                
                // Обновляем в репозитории
                attractionRepository.updateFavoriteStatus(
                    attractionId = attractionId,
                    isFavorite = newFavoriteStatus
                )
                
                // Мгновенно обновляем локальный список без перезагрузки
                _attractions.value = _attractions.value.map { attr ->
                    if (attr.id == attractionId) {
                        attr.copy(isFavorite = newFavoriteStatus)
                    } else {
                        attr
                    }
                }
                
                // Update selected attraction if it's the same one
                if (_selectedAttraction.value?.id == attractionId) {
                    _selectedAttraction.value = _selectedAttraction.value?.copy(
                        isFavorite = newFavoriteStatus
                    )
                }
                
                // НЕ вызываем loadAttractions() - это вызывало пропадание контейнера!
            }
        }
    }
    
    fun onLocationPermissionGranted() {
        _uiState.update { it.copy(hasLocationPermission = true) }
        startLocationTracking()
    }
    
    fun onLocationPermissionDenied() {
        _uiState.update { it.copy(hasLocationPermission = false) }
    }
    
    private fun startLocationTracking() {
        viewModelScope.launch {
            getLocationUseCase.getLocationUpdates(5000L)
                .catch { error ->
                    Timber.e(error, "Error getting location updates")
                }
                .collect { location ->
                    location?.let {
                        _uiState.update { state ->
                            state.copy(
                                userLocation = Pair(location.latitude, location.longitude)
                            )
                        }
                        Timber.d("User location updated: ${location.latitude}, ${location.longitude}")
                    }
                }
        }
    }
    
    fun getCurrentLocation() {
        viewModelScope.launch {
            val location = getLocationUseCase.getCurrentLocation()
            location?.let {
                _uiState.update { state ->
                    state.copy(
                        userLocation = Pair(location.latitude, location.longitude)
                    )
                }
            }
        }
    }
    
    fun navigateToAttraction(attraction: Attraction) {
        val userLocation = _uiState.value.userLocation
        if (userLocation != null) {
            navigationUseCase.buildRouteFromCurrentLocation(
                attraction = attraction,
                userLatitude = userLocation.first,
                userLongitude = userLocation.second
            )
        } else {
            navigationUseCase.buildRouteToAttraction(attraction)
        }
    }
    
    fun shareAttraction(attraction: Attraction) {
        shareUseCase.shareAttraction(attraction)
    }
    
    fun shareAttractionLocation(attraction: Attraction) {
        shareUseCase.shareAttractionLocation(attraction)
    }
    
    fun openAttractionInMaps(attraction: Attraction) {
        navigationUseCase.openAttractionInMaps(attraction)
    }
    
    fun filterAttractionsByDistance(maxDistanceKm: Float) {
        val userLocation = _uiState.value.userLocation
        if (userLocation != null) {
            viewModelScope.launch {
                attractionRepository.getAllAttractions()
                    .collect { allAttractions ->
                        val filteredAttractions = attractionDisplayUseCase.filterAttractionsForDisplay(
                            attractions = allAttractions,
                            userLatitude = userLocation.first,
                            userLongitude = userLocation.second,
                            maxDistance = maxDistanceKm
                        )
                        _attractions.value = filteredAttractions
                    }
            }
        }
    }
    
    fun sortAttractions(sortBy: SortCriteria) {
        val userLocation = _uiState.value.userLocation
        val currentAttractions = _attractions.value
        
        val sortedAttractions = attractionDisplayUseCase.sortAttractionsByRelevance(
            attractions = currentAttractions,
            userLatitude = userLocation?.first,
            userLongitude = userLocation?.second,
            sortBy = sortBy
        )
        
        _attractions.value = sortedAttractions
    }
    
    fun getRecommendedAttractions(limit: Int = 5): List<Attraction> {
        viewModelScope.launch {
            val allAttractions = _attractions.value
            val favorites = attractionRepository.getFavoriteAttractions().first()
            
            val recommended = attractionDisplayUseCase.getRecommendedAttractions(
                allAttractions = allAttractions,
                favoriteAttractions = favorites,
                recentlyViewed = emptyList(), // TODO: Implement recently viewed tracking
                limit = limit
            )
            
            _uiState.update { it.copy(recommendedAttractions = recommended) }
        }
        return emptyList()
    }
    
    fun checkNetworkStatus() {
        viewModelScope.launch {
            networkUseCase.getNetworkStatus().collect { status ->
                _uiState.update { 
                    it.copy(
                        isOnline = status is NetworkStatus.Connected,
                        networkStatus = status
                    )
                }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun search() {
        // Filtering is done automatically through the filteredAttractions flow
        Timber.d("Searching with query: ${_searchQuery.value}")
    }
    
    fun toggleCategory(category: AttractionCategory) {
        _selectedCategories.update { categories ->
            if (categories.contains(category)) {
                categories - category
            } else {
                categories + category
            }
        }
    }
    
    fun clearFilters() {
        _selectedCategories.value = emptySet()
    }
    
    fun clearSelection() {
        Timber.d("Clearing attraction selection")
        _selectedAttraction.value = null
        _uiState.update { it.copy(showAttractionDetail = false) }
    }
    
    fun navigateToAttractionById(attractionId: String) {
        val attraction = _attractions.value.find { it.id == attractionId }
        attraction?.let { navigateToAttraction(it) }
    }
    
    fun shareAttractionById(attractionId: String) {
        val attraction = _attractions.value.find { it.id == attractionId }
        attraction?.let { shareAttraction(it) }
    }
    
    /**
     * Update marker positions for the new overlay system
     * Called when the map camera moves
     */
    fun updateMarkerPositions() {
        viewModelScope.launch {
            val updatedMarkers = filteredAttractions.value.map { attraction ->
                MarkerState(
                    attraction = attraction,
                    screenPosition = null, // Will be calculated in MarkerOverlay
                    isVisible = true,
                    isSelected = attraction.id == _selectedAttraction.value?.id,
                    isLoading = false,
                    imageUrl = attraction.images.firstOrNull()
                )
            }
            
            _markerOverlayState.update { state ->
                state.copy(
                    markers = updatedMarkers,
                    selectedMarkerId = _selectedAttraction.value?.id,
                    isUpdating = false
                )
            }
            
            Timber.d("📍 Updated ${updatedMarkers.size} marker positions")
        }
    }
    
    /**
     * Handle marker click from the new overlay system
     * This provides 100% reliable click detection
     */
    fun onMarkerClick(attraction: Attraction) {
        Timber.d("✅ Marker clicked via overlay: ${attraction.name}")
        selectAttraction(attraction)
    }
    
    // New functions for list view enhancements
    fun selectCategoryFilter(filter: CategoryFilter) {
        Timber.d("🔍 CATEGORY FILTER: Selecting filter: $filter")
        _selectedCategoryFilter.value = filter
        
        // Log the effect on attractions
        viewModelScope.launch {
            delay(100) // Small delay to ensure flow updates
            Timber.d("🔍 CATEGORY FILTER: After filter, attractions count: ${filteredAttractions.value.size}")
            Timber.d("🔍 CATEGORY FILTER: Filtered attractions: ${filteredAttractions.value.map { it.name }.joinToString(", ")}")
        }
    }
    
    fun setSortBy(sortOption: SortBy) {
        _sortBy.value = sortOption
    }
    
    fun toggleListViewMode() {
        _listViewMode.value = when (_listViewMode.value) {
            ListViewMode.LIST -> ListViewMode.GRID
            ListViewMode.GRID -> ListViewMode.LIST
        }
    }
    
    fun toggleViewMode() {
        _viewMode.value = when (_viewMode.value) {
            ViewMode.MAP -> ViewMode.LIST
            ViewMode.LIST -> ViewMode.MAP
        }
    }
    
    // Enums for new functionality
    enum class SortBy(val displayName: String) {
        NAME("По названию"),
        CATEGORY("По категории"),
        RATING("По рейтингу"),
        DISTANCE("По расстоянию")
    }
    
    enum class ListViewMode {
        LIST,
        GRID
    }
    
    sealed class CategoryFilter {
        object All : CategoryFilter()
        object Favorites : CategoryFilter()
        data class Category(val category: AttractionCategory) : CategoryFilter()
    }
}

/**
 * UI state for the Map screen
 */
data class MapUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: AttractionCategory? = null,
    val showAttractionDetail: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val userLocation: Pair<Double, Double>? = null,
    val isOnline: Boolean = true,
    val networkStatus: NetworkStatus? = null,
    val recommendedAttractions: List<Attraction> = emptyList()
)
