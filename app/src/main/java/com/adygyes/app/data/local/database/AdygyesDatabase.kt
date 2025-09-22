package com.adygyes.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adygyes.app.data.local.dao.AttractionDao
import com.adygyes.app.data.local.entities.AttractionEntity
import com.adygyes.app.data.local.entities.Converters

/**
 * Main Room database for the Adygyes app
 */
@Database(
    entities = [AttractionEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AdygyesDatabase : RoomDatabase() {
    
    abstract fun attractionDao(): AttractionDao
    
    companion object {
        const val DATABASE_NAME = "adygyes_database"
    }
}
