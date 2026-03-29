package com.prescription.tracker.domain

import com.prescription.tracker.data.MedicationEntity
import java.time.DayOfWeek
import java.time.LocalDate

object StatusCalculator {

    fun calculate(
        medication: MedicationEntity,
        globalLeadDays: Int,
        useBusinessDays: Boolean = true,
        today: LocalDate = LocalDate.now()
    ): MedicationWithStatus {
        val runsOutDate = if (medication.remainingDaysOverride != null && medication.overrideDate != null) {
            medication.overrideDate.plusDays(medication.remainingDaysOverride.toLong())
        } else {
            medication.lastPickupDate.plusDays(medication.daysSupply.toLong())
        }

        val effectiveLeadDays = medication.orderLeadDays ?: globalLeadDays
        val orderByDate = if (useBusinessDays) {
            minusBusinessDays(runsOutDate, effectiveLeadDays)
        } else {
            runsOutDate.minusDays(effectiveLeadDays.toLong())
        }

        val status = when {
            runsOutDate.isBefore(today) || runsOutDate.isEqual(today) -> MedicationStatus.OVERDUE
            orderByDate.isBefore(today) || orderByDate.isEqual(today) -> MedicationStatus.ORDER_NOW
            orderByDate.isBefore(today.plusDays(4)) || orderByDate.isEqual(today.plusDays(3)) -> MedicationStatus.ORDER_SOON
            else -> MedicationStatus.OK
        }

        return MedicationWithStatus(
            medication = medication,
            runsOutDate = runsOutDate,
            orderByDate = orderByDate,
            status = status
        )
    }

    private fun minusBusinessDays(from: LocalDate, businessDays: Int): LocalDate {
        var date = from
        var remaining = businessDays
        while (remaining > 0) {
            date = date.minusDays(1)
            if (date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY) {
                remaining--
            }
        }
        return date
    }

    fun calculateAll(
        medications: List<MedicationEntity>,
        globalLeadDays: Int,
        useBusinessDays: Boolean = true,
        today: LocalDate = LocalDate.now()
    ): List<MedicationWithStatus> {
        return medications
            .map { calculate(it, globalLeadDays, useBusinessDays, today) }
            .sortedWith(
                compareBy<MedicationWithStatus> { it.status.ordinal }
                    .reversed()
                    .thenBy { it.runsOutDate }
            )
    }
}
