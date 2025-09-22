package com.adygyes.app.data.sync

import com.adygyes.app.data.local.cache.CacheManager
import com.adygyes.app.data.local.preferences.PreferencesManager
import com.adygyes.app.domain.repository.AttractionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling data synchronization
 */
@Singleton
class DataSyncManager @Inject constructor(
    private val attractionRepository: AttractionRepository,
    private val cacheManager: CacheManager,
    private val preferencesManager: PreferencesManager
) {
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Sync data based on cache policy
     */
    fun syncData() {
        coroutineScope.launch {
            try {
                _syncState.value = SyncState.SYNCING
                _syncProgress.value = 0f
                
                // Check cache policy
                val cachePolicy = cacheManager.getCachePolicy()
                Timber.d("Starting sync with policy: $cachePolicy")
                
                when (cachePolicy) {
                    CacheManager.CachePolicy.OFFLINE_FIRST -> {
                        // In offline mode, just ensure data is loaded
                        ensureLocalDataLoaded()
                    }
                    CacheManager.CachePolicy.NETWORK_FIRST -> {
                        // Try to fetch from network first
                        fetchFromNetwork()
                    }
                    CacheManager.CachePolicy.CACHE_FIRST -> {
                        // Use cache, no sync needed
                        ensureLocalDataLoaded()
                    }
                    CacheManager.CachePolicy.NO_CACHE -> {
                        // Always fetch fresh data
                        fetchFromNetwork()
                    }
                }
                
                _syncProgress.value = 1f
                _syncState.value = SyncState.SUCCESS
                
                // Mark sync completed
                cacheManager.markAttractionsSynced()
                
            } catch (e: Exception) {
                Timber.e(e, "Sync failed")
                _syncState.value = SyncState.ERROR(e.message ?: "Unknown error")
                _syncProgress.value = 0f
            }
        }
    }
    
    /**
     * Force refresh data from network
     */
    fun forceRefresh() {
        coroutineScope.launch {
            try {
                _syncState.value = SyncState.SYNCING
                fetchFromNetwork()
                _syncState.value = SyncState.SUCCESS
            } catch (e: Exception) {
                Timber.e(e, "Force refresh failed")
                _syncState.value = SyncState.ERROR(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Ensure local data is loaded
     */
    private suspend fun ensureLocalDataLoaded() {
        _syncProgress.value = 0.3f
        
        // Check if data is already loaded
        if (!attractionRepository.isDataLoaded()) {
            Timber.d("Loading initial data from assets")
            attractionRepository.loadInitialData()
        }
        
        _syncProgress.value = 1f
        Timber.d("Local data loaded successfully")
    }
    
    /**
     * Fetch data from network (placeholder for future API implementation)
     */
    private suspend fun fetchFromNetwork() {
        _syncProgress.value = 0.2f
        
        // TODO: Implement actual network fetching when API is ready
        // For now, just ensure local data is loaded
        Timber.d("Network fetch not yet implemented, loading from local assets")
        
        ensureLocalDataLoaded()
        
        // Simulate network progress
        _syncProgress.value = 0.8f
        
        Timber.d("Data sync completed")
    }
    
    /**
     * Check if sync is needed
     */
    suspend fun isSyncNeeded(): Boolean {
        return cacheManager.shouldRefreshAttractions()
    }
    
    /**
     * Cancel ongoing sync
     */
    fun cancelSync() {
        _syncState.value = SyncState.IDLE
        _syncProgress.value = 0f
        Timber.d("Sync cancelled")
    }
    
    /**
     * Sync state sealed class
     */
    sealed class SyncState {
        data object IDLE : SyncState()
        data object SYNCING : SyncState()
        data object SUCCESS : SyncState()
        data class ERROR(val message: String) : SyncState()
    }
}
