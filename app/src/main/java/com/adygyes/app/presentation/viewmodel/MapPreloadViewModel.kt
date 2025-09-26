package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.adygyes.app.presentation.ui.util.MapPreloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel wrapper for MapPreloadManager to enable Hilt injection in @Composable
 */
@HiltViewModel
class MapPreloadViewModel @Inject constructor(
    val preloadManager: MapPreloadManager
) : ViewModel() {
    
    override fun onCleared() {
        super.onCleared()
        preloadManager.onDestroy()
    }
}
