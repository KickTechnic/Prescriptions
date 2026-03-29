package com.prescription.tracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "pickup_history",
    foreignKeys = [ForeignKey(
        entity = MedicationEntity::class,
        parentColumns = ["id"],
        childColumns = ["medicationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medicationId")]
)
data class PickupHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val pickupDate: LocalDate,
    val createdAt: Long = System.currentTimeMillis()
)
