package com.adygyes.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        
        /**
         * Migration from version 1 to 2 (Example for future use)
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration: Add a new column
                // database.execSQL("ALTER TABLE attractions ADD COLUMN new_column TEXT")
            }
        }
        
        /**
         * Migration from version 2 to 3 (Example for future use)
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration: Create a new table
                // database.execSQL("""
                //     CREATE TABLE IF NOT EXISTS `reviews` (
                //         `id` TEXT NOT NULL,
                //         `attraction_id` TEXT NOT NULL,
                //         `user_name` TEXT NOT NULL,
                //         `rating` REAL NOT NULL,
                //         `comment` TEXT,
                //         `date` INTEGER NOT NULL,
                //         PRIMARY KEY(`id`)
                //     )
                // """)
            }
        }
        
        /**
         * Get all migrations array
         */
        fun getMigrations(): Array<Migration> {
            return arrayOf(
                // Add migrations here as database evolves
                // MIGRATION_1_2,
                // MIGRATION_2_3
            )
        }
    }
}
