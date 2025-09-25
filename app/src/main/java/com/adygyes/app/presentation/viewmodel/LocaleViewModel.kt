package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adygyes.app.data.local.locale.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing app locale/language
 */
@HiltViewModel
class LocaleViewModel @Inject constructor(
    private val localeManager: LocaleManager
) : ViewModel() {
    
    /**
     * Current language state
     */
    val currentLanguage: StateFlow<String> = localeManager.currentLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocaleManager.DEFAULT_LANGUAGE
        )
    
    /**
     * Set application language
     */
    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            localeManager.setLanguage(languageCode)
        }
    }
}
