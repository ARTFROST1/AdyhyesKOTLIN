package com.adygyes.app.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.adygyes.app.domain.model.Attraction
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for navigation and route building functionality
 */
@Singleton
class NavigationUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Build route to attraction using Yandex Maps
     */
    fun buildRouteToAttraction(attraction: Attraction) {
        try {
            val yandexMapsUri = Uri.parse(
                "yandexmaps://maps.yandex.ru/?rtext=~${attraction.location.latitude},${attraction.location.longitude}&rtt=auto"
            )
            
            val intent = Intent(Intent.ACTION_VIEW, yandexMapsUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("ru.yandex.yandexmaps")
            }
            
            // Check if Yandex Maps is installed
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Timber.d("Opened route to ${attraction.name} in Yandex Maps")
            } else {
                // Fallback to web version
                openYandexMapsWeb(attraction)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to open route to ${attraction.name}")
            // Fallback to web version
            openYandexMapsWeb(attraction)
        }
    }
    
    /**
     * Build route from current location to attraction
     */
    fun buildRouteFromCurrentLocation(
        attraction: Attraction,
        userLatitude: Double,
        userLongitude: Double
    ) {
        try {
            val yandexMapsUri = Uri.parse(
                "yandexmaps://maps.yandex.ru/?rtext=$userLatitude,$userLongitude~${attraction.location.latitude},${attraction.location.longitude}&rtt=auto"
            )
            
            val intent = Intent(Intent.ACTION_VIEW, yandexMapsUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("ru.yandex.yandexmaps")
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Timber.d("Opened route from current location to ${attraction.name} in Yandex Maps")
            } else {
                openYandexMapsWebWithRoute(attraction, userLatitude, userLongitude)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to open route from current location to ${attraction.name}")
            openYandexMapsWebWithRoute(attraction, userLatitude, userLongitude)
        }
    }
    
    /**
     * Open attraction location in Yandex Maps (without route)
     */
    fun openAttractionInMaps(attraction: Attraction) {
        try {
            val yandexMapsUri = Uri.parse(
                "yandexmaps://maps.yandex.ru/?ll=${attraction.location.longitude},${attraction.location.latitude}&z=16&text=${attraction.name}"
            )
            
            val intent = Intent(Intent.ACTION_VIEW, yandexMapsUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("ru.yandex.yandexmaps")
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Timber.d("Opened ${attraction.name} location in Yandex Maps")
            } else {
                openYandexMapsWeb(attraction)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to open ${attraction.name} in Yandex Maps")
            openYandexMapsWeb(attraction)
        }
    }
    
    /**
     * Fallback to web version of Yandex Maps
     */
    private fun openYandexMapsWeb(attraction: Attraction) {
        try {
            val webUri = Uri.parse(
                "https://yandex.ru/maps/?ll=${attraction.location.longitude},${attraction.location.latitude}&z=16&text=${Uri.encode(attraction.name)}"
            )
            
            val intent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            Timber.d("Opened ${attraction.name} in web browser")
        } catch (e: Exception) {
            Timber.e(e, "Failed to open ${attraction.name} in web browser")
        }
    }
    
    /**
     * Fallback to web version with route
     */
    private fun openYandexMapsWebWithRoute(
        attraction: Attraction,
        userLatitude: Double,
        userLongitude: Double
    ) {
        try {
            val webUri = Uri.parse(
                "https://yandex.ru/maps/?rtext=$userLatitude,$userLongitude~${attraction.location.latitude},${attraction.location.longitude}&rtt=auto"
            )
            
            val intent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            Timber.d("Opened route to ${attraction.name} in web browser")
        } catch (e: Exception) {
            Timber.e(e, "Failed to open route to ${attraction.name} in web browser")
        }
    }
    
    /**
     * Check if Yandex Maps app is installed
     */
    fun isYandexMapsInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("ru.yandex.yandexmaps", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
