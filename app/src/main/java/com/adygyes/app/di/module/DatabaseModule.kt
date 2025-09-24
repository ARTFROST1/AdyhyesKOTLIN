package com.adygyes.app.di.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.adygyes.app.data.local.cache.CacheManager
import com.adygyes.app.data.local.dao.AttractionDao
import com.adygyes.app.data.local.database.AdygyesDatabase
import com.adygyes.app.data.local.JsonFileManager
import com.adygyes.app.data.local.preferences.PreferencesManager
import com.adygyes.app.data.repository.AttractionRepositoryImpl
import com.adygyes.app.domain.repository.AttractionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database dependency injection module
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAdygyesDatabase(
        @ApplicationContext context: Context
    ): AdygyesDatabase {
        return Room.databaseBuilder(
            context,
            AdygyesDatabase::class.java,
            AdygyesDatabase.DATABASE_NAME
        )
            .addMigrations(*AdygyesDatabase.getMigrations())
            .fallbackToDestructiveMigration() // Fallback only if no migration path
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAttractionDao(database: AdygyesDatabase): AttractionDao {
        return database.attractionDao()
    }
    
    @Provides
    @Singleton
    fun provideAttractionRepository(
        attractionDao: AttractionDao,
        preferencesManager: PreferencesManager,
        @ApplicationContext context: Context
    ): AttractionRepository {
        return AttractionRepositoryImpl(attractionDao, preferencesManager, context)
    }
    
    @Provides
    @Singleton
    fun providePreferencesManager(
        dataStore: DataStore<Preferences>
    ): PreferencesManager {
        return PreferencesManager(dataStore)
    }
    
    @Provides
    @Singleton
    fun provideCacheManager(
        preferencesManager: PreferencesManager
    ): CacheManager {
        return CacheManager(preferencesManager)
    }
    
    @Provides
    @Singleton
    fun provideJsonFileManager(
        @ApplicationContext context: Context
    ): JsonFileManager {
        return JsonFileManager(context)
    }
}
