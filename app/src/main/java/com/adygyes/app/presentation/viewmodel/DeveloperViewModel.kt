package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adygyes.app.data.local.JsonFileManager
import com.adygyes.app.domain.model.*
import com.adygyes.app.domain.repository.AttractionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Developer Mode screens
 */
@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val attractionRepository: AttractionRepository,
    private val jsonFileManager: JsonFileManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DeveloperUiState())
    val uiState: StateFlow<DeveloperUiState> = _uiState.asStateFlow()
    
    private val _editorState = MutableStateFlow(AttractionEditorState())
    val editorState: StateFlow<AttractionEditorState> = _editorState.asStateFlow()
    
    private val _operationResult = MutableSharedFlow<OperationResult>()
    val operationResult: SharedFlow<OperationResult> = _operationResult.asSharedFlow()
    
    val attractions = attractionRepository.getAllAttractions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        initializeJsonManager()
        loadAttractions()
        checkProjectStatus()
    }
    
    private fun initializeJsonManager() {
        viewModelScope.launch {
            jsonFileManager.initializeEditableJson()
        }
    }
    
    private fun loadAttractions() {
        viewModelScope.launch {
            attractions.collect { list ->
                _uiState.update { it.copy(attractionsCount = list.size) }
            }
        }
    }
    
    /**
     * Проверяет доступность проекта для автообновления
     */
    private fun checkProjectStatus() {
        viewModelScope.launch {
            val (isAvailable, projectPath) = jsonFileManager.checkProjectAvailability()
            _uiState.update { 
                it.copy(
                    projectPath = projectPath,
                    autoSaveStatus = if (isAvailable) AutoSaveStatus.IDLE else AutoSaveStatus.PROJECT_NOT_FOUND
                )
            }
        }
    }
    
    /**
     * Load attraction for editing
     */
    fun loadAttractionForEdit(attractionId: String) {
        viewModelScope.launch {
            _editorState.update { it.copy(isLoading = true) }
            
            val attraction = attractionRepository.getAttractionById(attractionId)
            if (attraction != null) {
                _editorState.update {
                    it.copy(
                        isLoading = false,
                        isEditMode = true,
                        id = attraction.id,
                        name = attraction.name,
                        description = attraction.description,
                        category = attraction.category,
                        latitude = attraction.location.latitude.toString(),
                        longitude = attraction.location.longitude.toString(),
                        address = attraction.location.address ?: "",
                        directions = attraction.location.directions ?: "",
                        images = attraction.images.joinToString("\n"),
                        rating = attraction.rating?.toString() ?: "",
                        workingHours = attraction.workingHours ?: "",
                        phoneNumber = attraction.contactInfo?.phone ?: "",
                        email = attraction.contactInfo?.email ?: "",
                        website = attraction.contactInfo?.website ?: "",
                        tags = attraction.tags.joinToString(", "),
                        priceInfo = attraction.priceInfo ?: "",
                        amenities = attraction.amenities.joinToString(", ")
                    )
                }
            } else {
                _editorState.update { it.copy(isLoading = false) }
                _operationResult.emit(OperationResult.Error("Attraction not found"))
            }
        }
    }
    
    /**
     * Initialize editor for new attraction
     */
    fun initializeNewAttraction() {
        val nextId = jsonFileManager.generateNextId()
        _editorState.value = AttractionEditorState(
            isEditMode = false,
            id = nextId
        )
    }
    
    /**
     * Update editor field
     */
    fun updateEditorField(field: EditorField, value: String) {
        _editorState.update { state ->
            when (field) {
                EditorField.NAME -> state.copy(name = value)
                EditorField.DESCRIPTION -> state.copy(description = value)
                EditorField.LATITUDE -> state.copy(latitude = value)
                EditorField.LONGITUDE -> state.copy(longitude = value)
                EditorField.ADDRESS -> state.copy(address = value)
                EditorField.DIRECTIONS -> state.copy(directions = value)
                EditorField.IMAGES -> state.copy(images = value)
                EditorField.RATING -> state.copy(rating = value)
                EditorField.WORKING_HOURS -> state.copy(workingHours = value)
                EditorField.PHONE -> state.copy(phoneNumber = value)
                EditorField.EMAIL -> state.copy(email = value)
                EditorField.WEBSITE -> state.copy(website = value)
                EditorField.TAGS -> state.copy(tags = value)
                EditorField.PRICE_INFO -> state.copy(priceInfo = value)
                EditorField.AMENITIES -> state.copy(amenities = value)
            }
        }
    }
    
    /**
     * Update category
     */
    fun updateCategory(category: AttractionCategory) {
        _editorState.update { it.copy(category = category) }
    }
    
    /**
     * Save attraction (add or update)
     */
    fun saveAttraction() {
        viewModelScope.launch {
            val state = _editorState.value
            
            // Validate required fields
            if (state.name.isBlank() || state.description.isBlank() ||
                state.latitude.isBlank() || state.longitude.isBlank()) {
                _operationResult.emit(
                    OperationResult.Error("Please fill all required fields")
                )
                return@launch
            }
            
            // Parse and validate coordinates
            val lat = state.latitude.toDoubleOrNull()
            val lng = state.longitude.toDoubleOrNull()
            
            if (lat == null || lng == null) {
                _operationResult.emit(
                    OperationResult.Error("Invalid coordinates")
                )
                return@launch
            }
            
            // Create attraction object
            val attraction = Attraction(
                id = state.id,
                name = state.name,
                description = state.description,
                category = state.category,
                location = Location(
                    latitude = lat,
                    longitude = lng,
                    address = state.address.takeIf { it.isNotBlank() },
                    directions = state.directions.takeIf { it.isNotBlank() }
                ),
                images = state.images.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() },
                rating = state.rating.toFloatOrNull(),
                workingHours = state.workingHours.takeIf { it.isNotBlank() },
                contactInfo = if (state.phoneNumber.isNotBlank() || 
                                  state.email.isNotBlank() || 
                                  state.website.isNotBlank()) {
                    ContactInfo(
                        phone = state.phoneNumber.takeIf { it.isNotBlank() },
                        email = state.email.takeIf { it.isNotBlank() },
                        website = state.website.takeIf { it.isNotBlank() }
                    )
                } else null,
                tags = state.tags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() },
                priceInfo = state.priceInfo.takeIf { it.isNotBlank() },
                amenities = state.amenities.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            )
            
            // Save to repository
            val success = if (state.isEditMode) {
                attractionRepository.updateAttraction(attraction)
            } else {
                attractionRepository.addAttraction(attraction)
            }
            
            if (success) {
                _operationResult.emit(
                    OperationResult.Success(
                        if (state.isEditMode) "Attraction updated successfully"
                        else "Attraction added successfully"
                    )
                )
            } else {
                _operationResult.emit(
                    OperationResult.Error("Failed to save attraction")
                )
            }
        }
    }
    
    /**
     * Delete attraction
     */
    fun deleteAttraction(attractionId: String) {
        viewModelScope.launch {
            val success = attractionRepository.deleteAttraction(attractionId)
            
            if (success) {
                _operationResult.emit(
                    OperationResult.Success("Attraction deleted successfully")
                )
            } else {
                _operationResult.emit(
                    OperationResult.Error("Failed to delete attraction")
                )
            }
        }
    }
    
    /**
     * Reload all data from JSON
     */
    fun reloadFromJson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isReloading = true) }
            
            val success = attractionRepository.reloadFromJson()
            
            _uiState.update { it.copy(isReloading = false) }
            
            if (success) {
                _operationResult.emit(
                    OperationResult.Success("Data reloaded from JSON successfully")
                )
            } else {
                _operationResult.emit(
                    OperationResult.Error("Failed to reload data from JSON")
                )
            }
        }
    }
    
    /**
     * Reset to original JSON
     */
    fun resetToOriginal() {
        viewModelScope.launch {
            val success = jsonFileManager.resetToOriginal()
            if (success) {
                reloadFromJson()
            } else {
                _operationResult.emit(
                    OperationResult.Error("Failed to reset to original")
                )
            }
        }
    }
    
    /**
     * Export JSON to Downloads folder for manual copying to project
     */
    fun exportToDownloads() {
        viewModelScope.launch {
            val filePath = jsonFileManager.exportToDownloads()
            if (filePath != null) {
                _operationResult.emit(
                    OperationResult.Success("JSON exported to:\n$filePath\n\nCopy this file to app/src/main/assets/attractions.json in your project")
                )
            } else {
                _operationResult.emit(
                    OperationResult.Error("Failed to export JSON")
                )
            }
        }
    }
    
    /**
     * Get JSON string for sharing or copying
     */
    fun getJsonForClipboard(clipboardManager: android.content.ClipboardManager) {
        viewModelScope.launch {
            val jsonString = jsonFileManager.getClipboardJson()
            if (jsonString != null) {
                val clip = android.content.ClipData.newPlainText("attractions.json", jsonString)
                clipboardManager.setPrimaryClip(clip)
                _operationResult.emit(
                    OperationResult.Success("JSON copied to clipboard! Paste it into app/src/main/assets/attractions.json")
                )
            } else {
                _operationResult.emit(
                    OperationResult.Error("Failed to get JSON for clipboard")
                )
            }
        }
    }
    
    /**
     * Validate image URL
     */
    fun validateImageUrl(url: String): Boolean {
        return url.startsWith("http://") || 
               url.startsWith("https://") ||
               url.startsWith("file:///android_asset/")
    }
    
    data class DeveloperUiState(
        val attractionsCount: Int = 0,
        val isReloading: Boolean = false,
        val autoSaveStatus: AutoSaveStatus = AutoSaveStatus.IDLE,
        val projectPath: String? = null
    )
    
    enum class AutoSaveStatus {
        IDLE,           // Нет активности
        SAVING,         // Сохранение в процессе
        SUCCESS,        // Успешно сохранено в проект
        FAILED,         // Ошибка сохранения
        PROJECT_NOT_FOUND // Проект не найден
    }
    
    data class AttractionEditorState(
        val isLoading: Boolean = false,
        val isEditMode: Boolean = false,
        val id: String = "",
        val name: String = "",
        val description: String = "",
        val category: AttractionCategory = AttractionCategory.NATURE,
        val latitude: String = "",
        val longitude: String = "",
        val address: String = "",
        val directions: String = "",
        val images: String = "",
        val rating: String = "",
        val workingHours: String = "",
        val phoneNumber: String = "",
        val email: String = "",
        val website: String = "",
        val tags: String = "",
        val priceInfo: String = "",
        val amenities: String = ""
    )
    
    enum class EditorField {
        NAME, DESCRIPTION, LATITUDE, LONGITUDE, ADDRESS, DIRECTIONS,
        IMAGES, RATING, WORKING_HOURS, PHONE, EMAIL, WEBSITE,
        TAGS, PRICE_INFO, AMENITIES
    }
    
    sealed class OperationResult {
        data class Success(val message: String) : OperationResult()
        data class Error(val message: String) : OperationResult()
    }
}
