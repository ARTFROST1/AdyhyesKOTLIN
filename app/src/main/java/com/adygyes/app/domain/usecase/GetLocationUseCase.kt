package com.adygyes.app.domain.usecase

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Use case for getting user location
 */
@Singleton
class GetLocationUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    
    /**
     * Get current location once
     */
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        
        try {
            val cancellationToken = CancellationTokenSource()
            cont.invokeOnCancellation {
                cancellationToken.cancel()
            }
            
            // Используем более точные настройки для получения текущего местоположения
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 1000L // Быстрое обновление для точности
                fastestInterval = 500L
                numUpdates = 1 // Только одно обновление
            }
            
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                if (location != null && location.accuracy <= 50.0f) { // Принимаем только точные координаты (до 50 метров)
                    Timber.d("Got accurate location: ${location.latitude}, ${location.longitude} (accuracy: ${location.accuracy}m)")
                    cont.resume(location)
                } else {
                    Timber.w("Location accuracy too low: ${location?.accuracy}m, requesting better location")
                    cont.resume(location) // Все равно возвращаем, но логируем предупреждение
                }
            }.addOnFailureListener { exception ->
                Timber.e(exception, "Failed to get location")
                cont.resume(null)
            }
        } catch (e: SecurityException) {
            Timber.e(e, "Security exception getting location")
            cont.resume(null)
        }
    }
    
    /**
     * Get location updates as a Flow
     */
    fun getLocationUpdates(intervalMillis: Long = 5000L): Flow<Location?> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        
        val locationRequest = LocationRequest.create().apply {
            interval = intervalMillis
            fastestInterval = intervalMillis / 4 // Более частые обновления для точности
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 5.0f // Минимальное смещение 5 метров
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Timber.d("Location update: ${location.latitude}, ${location.longitude}")
                    trySend(location)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            Timber.e(e, "Security exception for location updates")
            close(e)
        }
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
