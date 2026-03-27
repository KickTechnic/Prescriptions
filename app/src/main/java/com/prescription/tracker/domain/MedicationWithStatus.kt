package com.prescription.tracker.domain

import com.prescription.tracker.data.MedicationEntity
import java.time.LocalDate

data class MedicationWithStatus(
    val medication: MedicationEntity,
    val runsOutDate: LocalDate,
    val orderByDate: LocalDate,
    val status: MedicationStatus
) {
    val id: Long get() = medication.id
    val name: String get() = medication.name
    val personName: String? get() = medication.personName
}
