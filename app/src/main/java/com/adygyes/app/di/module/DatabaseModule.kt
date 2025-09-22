package com.adygyes.app.di.module

import android.content.Context
import androidx.room.Room
import com.adygyes.app.data.local.dao.AttractionDao
import com.adygyes.app.data.local.database.AdygyesDatabase
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
            .fallbackToDestructiveMigration()
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
        @ApplicationContext context: Context
    ): AttractionRepository {
        return AttractionRepositoryImpl(attractionDao, context)
    }
}
