package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adygyes.app.domain.model.Attraction
import com.adygyes.app.domain.repository.AttractionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the attraction detail screen
 */
@HiltViewModel
class AttractionDetailViewModel @Inject constructor(
    private val repository: AttractionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private var currentAttractionId: String? = null
    
    fun loadAttraction(attractionId: String) {
        if (attractionId == currentAttractionId && _uiState.value is UiState.Success) {
            // Already loaded this attraction
            return
        }
        
        currentAttractionId = attractionId
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val attraction = repository.getAttractionById(attractionId)
                if (attraction != null) {
                    _uiState.value = UiState.Success(
                        attraction = attraction,
                        reviewCount = (10..100).random() // Mock review count for now
                    )
                } else {
                    _uiState.value = UiState.Error(
                        message = "Attraction not found"
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = UiState.Error(
                    message = exception.message ?: "Failed to load attraction"
                )
            }
        }
    }
    
    fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            viewModelScope.launch {
                val updatedAttraction = currentState.attraction.copy(
                    isFavorite = !currentState.attraction.isFavorite
                )
                
                repository.updateFavoriteStatus(
                    attractionId = updatedAttraction.id,
                    isFavorite = updatedAttraction.isFavorite
                )
                
                _uiState.value = currentState.copy(attraction = updatedAttraction)
            }
        }
    }
    
    fun refresh() {
        currentAttractionId?.let { loadAttraction(it) }
    }
    
    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val attraction: Attraction,
            val reviewCount: Int = 0
        ) : UiState
        data class Error(val message: String) : UiState
    }
}
