package com.adygyes.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adygyes.app.data.local.preferences.PreferencesManager
import com.yandex.mapkit.map.CameraPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Holds and persists Yandex Map camera state across screens and app restarts
 */
@HiltViewModel
class MapStateViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    data class CameraState(
        val lat: Double = 44.6098,
        val lon: Double = 40.1006,
        val zoom: Float = 10f,
        val azimuth: Float = 0f,
        val tilt: Float = 0f
    )

    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    init {
        // Load persisted camera state
        viewModelScope.launch {
            preferencesManager.cameraStateFlow.collectLatest { prefCam ->
                _cameraState.value = CameraState(
                    lat = prefCam.lat,
                    lon = prefCam.lon,
                    zoom = prefCam.zoom,
                    azimuth = prefCam.azimuth,
                    tilt = prefCam.tilt
                )
            }
        }
    }

    fun onCameraPositionChanged(pos: CameraPosition) {
        val newState = CameraState(
            lat = pos.target.latitude,
            lon = pos.target.longitude,
            zoom = pos.zoom,
            azimuth = pos.azimuth,
            tilt = pos.tilt
        )
        _cameraState.value = newState
        // Persist immediately; can be optimized with debounce if needed
        viewModelScope.launch {
            preferencesManager.updateCameraState(
                lat = newState.lat,
                lon = newState.lon,
                zoom = newState.zoom,
                azimuth = newState.azimuth,
                tilt = newState.tilt
            )
        }
    }
}
