package com.prescription.tracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MedicationEntity::class, PickupHistoryEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun pickupHistoryDao(): PickupHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pickup_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        medicationId INTEGER NOT NULL,
                        pickupDate INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(medicationId) REFERENCES medications(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pickup_history_medicationId ON pickup_history(medicationId)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prescription_tracker.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
