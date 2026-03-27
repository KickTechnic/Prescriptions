package com.prescription.tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications ORDER BY personName, name")
    fun getAllFlow(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications")
    suspend fun getAll(): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntity?

    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun delete(medication: MedicationEntity)

    @Query("UPDATE medications SET lastPickupDate = :date, remainingDaysOverride = NULL, overrideDate = NULL WHERE id = :id")
    suspend fun markPickedUp(id: Long, date: LocalDate)

    @Query("UPDATE medications SET remainingDaysOverride = :daysRemaining, overrideDate = :today WHERE id = :id")
    suspend fun overrideSupply(id: Long, daysRemaining: Int, today: LocalDate)
}
