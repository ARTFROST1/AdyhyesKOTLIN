package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.model.AttractionCategory
import com.adygyes.app.domain.repository.AttractionRepository
import com.adygyes.app.domain.usecase.GetLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Map screen
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val attractionRepository: AttractionRepository,
    private val getLocationUseCase: GetLocationUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    private val _attractions = MutableStateFlow<List<Attraction>>(emptyList())
    val attractions: StateFlow<List<Attraction>> = _attractions.asStateFlow()
    
    private val _selectedAttraction = MutableStateFlow<Attraction?>(null)
    val selectedAttraction: StateFlow<Attraction?> = _selectedAttraction.asStateFlow()
    
    init {
        loadAttractions()
        checkAndLoadInitialData()
    }
    
    private fun checkAndLoadInitialData() {
        viewModelScope.launch {
            try {
                if (!attractionRepository.isDataLoaded()) {
                    Timber.d("Loading initial data...")
                    attractionRepository.loadInitialData()
                }
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
                attractionRepository.updateFavoriteStatus(
                    attractionId = attractionId,
                    isFavorite = !it.isFavorite
                )
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
    val userLocation: Pair<Double, Double>? = null
)
