package com.prescription.tracker.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prescription.tracker.data.AppDatabase
import com.prescription.tracker.data.SettingsManager
import com.prescription.tracker.domain.MedicationWithStatus
import com.prescription.tracker.domain.StatusCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MedicationListViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).medicationDao()
    private val settings = SettingsManager(application)

    val medications: StateFlow<List<MedicationWithStatus>> =
        combine(dao.getAllFlow(), settings.settingsFlow) { meds, s ->
            StatusCalculator.calculateAll(meds, s.globalLeadDays, s.useBusinessDays)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adsRemoved: StateFlow<Boolean> =
        settings.settingsFlow.map { it.adsRemoved }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
