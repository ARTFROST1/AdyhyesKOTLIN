// Temporary test file to verify compilation
import com.adygyes.app.data.sync.DataSyncManager
import com.adygyes.app.presentation.navigation.NavDestination

fun testCompilation() {
    // Test DataSyncManager.SyncState
    val syncState: DataSyncManager.SyncState = DataSyncManager.SyncState.IDLE
    val syncState2: DataSyncManager.SyncState = DataSyncManager.SyncState.SYNCING
    val syncState3: DataSyncManager.SyncState = DataSyncManager.SyncState.SUCCESS
    val syncState4: DataSyncManager.SyncState = DataSyncManager.SyncState.ERROR("Test error")
    
    // Test NavDestination
    val destination: NavDestination = NavDestination.Map
    val destination2: NavDestination = NavDestination.Search
    
    println("Compilation test successful")
}
