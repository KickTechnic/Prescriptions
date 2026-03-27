package com.prescription.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String? = null,
    val name: String,
    val lastPickupDate: LocalDate,
    val daysSupply: Int = 28,
    val orderLeadDays: Int? = null,
    val remainingDaysOverride: Int? = null,
    val overrideDate: LocalDate? = null
)
