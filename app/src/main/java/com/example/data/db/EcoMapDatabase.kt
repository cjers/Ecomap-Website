package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [EcoMapEntity::class, NodeEntity::class, ConnectionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class EcoMapDatabase : RoomDatabase() {
    abstract fun ecomapDao(): EcoMapDao

    companion object {
        @Volatile
        private var INSTANCE: EcoMapDatabase? = null

        fun getDatabase(context: Context): EcoMapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EcoMapDatabase::class.java,
                    "ecomap_clinical_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
