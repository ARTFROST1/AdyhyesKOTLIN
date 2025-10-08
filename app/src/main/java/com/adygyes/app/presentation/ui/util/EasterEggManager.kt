package com.adygyes.app.presentation.ui.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Session-scoped Easter egg controller.
 * - Activated by 7 taps on Settings title
 * - Not persisted (resets on app restart)
 */
object EasterEggManager {
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive

    fun activate() {
        _isActive.value = true
    }

    fun deactivate() {
        _isActive.value = false
    }
}
