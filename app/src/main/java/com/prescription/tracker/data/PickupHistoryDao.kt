package com.prescription.tracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PickupHistoryDao {

    @Insert
    suspend fun insert(entry: PickupHistoryEntity)

    @Query("SELECT * FROM pickup_history WHERE medicationId = :medicationId ORDER BY pickupDate DESC")
    fun getHistoryForMedication(medicationId: Long): Flow<List<PickupHistoryEntity>>

    @Query("DELETE FROM pickup_history WHERE medicationId = :medicationId")
    suspend fun deleteForMedication(medicationId: Long)
}
