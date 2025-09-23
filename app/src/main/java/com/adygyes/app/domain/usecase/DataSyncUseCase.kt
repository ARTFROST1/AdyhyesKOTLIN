package com.adygyes.app.domain.usecase

import com.adygyes.app.data.local.preferences.PreferencesManager
import com.adygyes.app.domain.repository.AttractionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for data synchronization and update checking
 */
@Singleton
class DataSyncUseCase @Inject constructor(
    private val attractionRepository: AttractionRepository,
    private val preferencesManager: PreferencesManager,
    private val networkUseCase: NetworkUseCase
) {
    
    companion object {
        private const val LAST_UPDATE_CHECK_KEY = "last_update_check"
        private const val LAST_SUCCESSFUL_SYNC_KEY = "last_successful_sync"
        private const val DATA_VERSION_KEY = "data_version"
        private const val UPDATE_CHECK_INTERVAL_HOURS = 24L
        private const val FORCE_UPDATE_INTERVAL_DAYS = 7L
    }
    
    /**
     * Check if data needs to be updated
     */
    suspend fun shouldCheckForUpdates(): Boolean {
        // For now, we'll use a simple time-based check
        // In a real implementation, this would check against stored preferences
        return true // Always check for updates for now
    }
    
    /**
     * Check for data updates from remote source
     */
    suspend fun checkForUpdates(): UpdateCheckResult {
        return try {
            if (!networkUseCase.isOnline()) {
                return UpdateCheckResult.NoConnection
            }
            
            // Simulate checking for updates (in real app, this would call an API)
            delay(1000) // Simulate network delay
            
            val currentVersion = "1.0.0" // Default version
            val latestVersion = getLatestDataVersion() // This would be an API call
            
            if (isNewerVersion(latestVersion, currentVersion)) {
                Timber.d("Update available: $currentVersion -> $latestVersion")
                UpdateCheckResult.UpdateAvailable(latestVersion)
            } else {
                Timber.d("Data is up to date: $currentVersion")
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking for updates")
            UpdateCheckResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Perform data synchronization
     */
    fun syncData(): Flow<SyncProgress> = flow {
        try {
            emit(SyncProgress.Started)
            
            if (!networkUseCase.isOnline()) {
                emit(SyncProgress.Error("No internet connection"))
                return@flow
            }
            
            emit(SyncProgress.CheckingForUpdates)
            delay(500)
            
            val updateResult = checkForUpdates()
            when (updateResult) {
                is UpdateCheckResult.UpdateAvailable -> {
                    emit(SyncProgress.DownloadingData(0))
                    
                    // Simulate downloading data with progress
                    for (progress in 0..100 step 10) {
                        delay(200)
                        emit(SyncProgress.DownloadingData(progress))
                    }
                    
                    emit(SyncProgress.ProcessingData)
                    delay(1000)
                    
                    // In real implementation, this would download and process new data
                    // For now, we'll just reload existing data
                    attractionRepository.loadInitialData()
                    
                    // Update version and sync timestamp
                    preferencesManager.updateDataVersion(updateResult.version)
                    preferencesManager.updateLastSyncTime(System.currentTimeMillis())
                    
                    emit(SyncProgress.Completed("Data updated to version ${updateResult.version}"))
                }
                UpdateCheckResult.UpToDate -> {
                    preferencesManager.updateLastSyncTime(System.currentTimeMillis())
                    emit(SyncProgress.Completed("Data is already up to date"))
                }
                UpdateCheckResult.NoConnection -> {
                    emit(SyncProgress.Error("No internet connection"))
                }
                is UpdateCheckResult.Error -> {
                    emit(SyncProgress.Error(updateResult.message))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during data sync")
            emit(SyncProgress.Error(e.message ?: "Sync failed"))
        }
    }
    
    /**
     * Force data refresh (bypass cache)
     */
    suspend fun forceRefresh(): Boolean {
        return try {
            if (!networkUseCase.isOnline()) {
                return false
            }
            
            // Clear existing data and reload
            attractionRepository.loadInitialData()
            preferencesManager.updateLastSyncTime(System.currentTimeMillis())
            
            Timber.d("Force refresh completed")
            true
        } catch (e: Exception) {
            Timber.e(e, "Force refresh failed")
            false
        }
    }
    
    /**
     * Get last sync information
     */
    suspend fun getLastSyncInfo(): SyncInfo {
        // For now, return default values since we need to access preferences flow
        return SyncInfo(
            lastSyncTime = null, // Would need to get from preferences flow
            dataVersion = "1.0.0", // Default version
            lastUpdateCheck = null, // Would need to get from preferences flow
            isDataLoaded = attractionRepository.isDataLoaded()
        )
    }
    
    /**
     * Check if force update is needed (data is too old)
     */
    suspend fun needsForceUpdate(): Boolean {
        // For now, return false since we don't have easy access to sync timestamp
        // In a real implementation, this would check the preferences
        return false
    }
    
    /**
     * Simulate getting latest version from server
     */
    private suspend fun getLatestDataVersion(): String {
        // In real implementation, this would be an API call
        delay(500)
        return "1.1.0" // Simulate newer version available
    }
    
    /**
     * Compare version strings
     */
    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        return try {
            val newParts = newVersion.split(".").map { it.toInt() }
            val currentParts = currentVersion.split(".").map { it.toInt() }
            
            for (i in 0 until maxOf(newParts.size, currentParts.size)) {
                val newPart = newParts.getOrElse(i) { 0 }
                val currentPart = currentParts.getOrElse(i) { 0 }
                
                when {
                    newPart > currentPart -> return true
                    newPart < currentPart -> return false
                }
            }
            false
        } catch (e: Exception) {
            Timber.e(e, "Error comparing versions: $newVersion vs $currentVersion")
            false
        }
    }
}

/**
 * Result of update check
 */
sealed class UpdateCheckResult {
    object UpToDate : UpdateCheckResult()
    object NoConnection : UpdateCheckResult()
    data class UpdateAvailable(val version: String) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

/**
 * Sync progress states
 */
sealed class SyncProgress {
    object Started : SyncProgress()
    object CheckingForUpdates : SyncProgress()
    data class DownloadingData(val progress: Int) : SyncProgress()
    object ProcessingData : SyncProgress()
    data class Completed(val message: String) : SyncProgress()
    data class Error(val message: String) : SyncProgress()
}

/**
 * Information about last sync
 */
data class SyncInfo(
    val lastSyncTime: Date?,
    val dataVersion: String,
    val lastUpdateCheck: Date?,
    val isDataLoaded: Boolean
) {
    fun getLastSyncFormatted(): String {
        return lastSyncTime?.let { 
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: "Никогда"
    }
    
    fun getLastCheckFormatted(): String {
        return lastUpdateCheck?.let { 
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: "Никогда"
    }
}
