package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adygyes.app.data.local.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _themeMode = MutableStateFlow("system") // "light" | "dark" | "system"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.userPreferencesFlow.collect { prefs ->
                _themeMode.value = prefs.themeMode.lowercase()
            }
        }
    }
}
