package com.adygyes.app.domain.usecase

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for network connectivity monitoring and offline mode handling
 */
@Singleton
class NetworkUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Check if device is currently connected to internet
     */
    fun isOnline(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Timber.e(e, "Error checking network status")
            false
        }
    }
    
    /**
     * Get network connectivity status as a Flow
     */
    fun getNetworkStatus(): Flow<NetworkStatus> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("Network available")
                trySend(NetworkStatus.Available)
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                if (hasInternet && isValidated) {
                    Timber.d("Network connected with internet")
                    trySend(NetworkStatus.Connected)
                } else {
                    Timber.d("Network available but no internet")
                    trySend(NetworkStatus.Available)
                }
            }
            
            override fun onLost(network: Network) {
                Timber.d("Network lost")
                trySend(NetworkStatus.Lost)
            }
            
            override fun onUnavailable() {
                Timber.d("Network unavailable")
                trySend(NetworkStatus.Unavailable)
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        // Send initial status
        trySend(if (isOnline()) NetworkStatus.Connected else NetworkStatus.Unavailable)
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()
    
    /**
     * Get connection type information
     */
    fun getConnectionType(): ConnectionType {
        return try {
            val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NONE
            
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                else -> ConnectionType.OTHER
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting connection type")
            ConnectionType.NONE
        }
    }
    
    /**
     * Check if connection is metered (limited data)
     */
    fun isMeteredConnection(): Boolean {
        return try {
            connectivityManager.isActiveNetworkMetered
        } catch (e: Exception) {
            Timber.e(e, "Error checking if connection is metered")
            false
        }
    }
    
    /**
     * Get detailed network information
     */
    fun getNetworkInfo(): NetworkInfo {
        val isOnline = isOnline()
        val connectionType = getConnectionType()
        val isMetered = isMeteredConnection()
        
        return NetworkInfo(
            isOnline = isOnline,
            connectionType = connectionType,
            isMetered = isMetered,
            canDownloadLargeFiles = isOnline && !isMetered,
            canSync = isOnline
        )
    }
}

/**
 * Network status states
 */
sealed class NetworkStatus {
    object Connected : NetworkStatus()
    object Available : NetworkStatus()
    object Lost : NetworkStatus()
    object Unavailable : NetworkStatus()
}

/**
 * Connection types
 */
enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER,
    NONE
}

/**
 * Comprehensive network information
 */
data class NetworkInfo(
    val isOnline: Boolean,
    val connectionType: ConnectionType,
    val isMetered: Boolean,
    val canDownloadLargeFiles: Boolean,
    val canSync: Boolean
)
