package com.prescription.tracker.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.prescription.tracker.data.AppDatabase
import com.prescription.tracker.data.MedicationEntity
import com.prescription.tracker.data.SettingsManager
import com.prescription.tracker.widget.PrescriptionWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditMedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).medicationDao()
    val settings = SettingsManager(application)

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private var editingId: Long? = null

    fun loadMedication(id: Long) {
        viewModelScope.launch {
            val med = dao.getById(id) ?: return@launch
            editingId = med.id
            _uiState.value = EditUiState(
                personName = med.personName ?: "",
                name = med.name,
                lastPickupDate = med.lastPickupDate,
                daysSupply = med.daysSupply.toString(),
                orderLeadDays = med.orderLeadDays?.toString() ?: "",
                remainingDaysOverride = med.remainingDaysOverride?.toString() ?: "",
                isEditing = true
            )
        }
    }

    fun updatePersonName(value: String) {
        _uiState.value = _uiState.value.copy(personName = value)
    }

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun updateLastPickupDate(value: LocalDate) {
        _uiState.value = _uiState.value.copy(lastPickupDate = value)
    }

    fun updateDaysSupply(value: String) {
        _uiState.value = _uiState.value.copy(daysSupply = value)
    }

    fun updateOrderLeadDays(value: String) {
        _uiState.value = _uiState.value.copy(orderLeadDays = value)
    }

    fun updateRemainingOverride(value: String) {
        _uiState.value = _uiState.value.copy(remainingDaysOverride = value)
    }

    private fun refreshWidget() {
        viewModelScope.launch {
            PrescriptionWidget().updateAll(getApplication())
        }
    }

    fun markPickedUp(date: LocalDate) {
        val id = editingId ?: return
        viewModelScope.launch {
            dao.markPickedUp(id, date)
            refreshWidget()
            _saved.value = true
        }
    }

    fun adjustSupply(daysRemaining: Int) {
        val id = editingId ?: return
        viewModelScope.launch {
            dao.overrideSupply(id, daysRemaining, LocalDate.now())
            refreshWidget()
            _saved.value = true
        }
    }

    fun save() {
        val state = _uiState.value
        val supply = state.daysSupply.toIntOrNull() ?: return
        if (state.name.isBlank()) return

        viewModelScope.launch {
            val entity = MedicationEntity(
                id = editingId ?: 0,
                personName = state.personName.ifBlank { null },
                name = state.name.trim(),
                lastPickupDate = state.lastPickupDate,
                daysSupply = supply,
                orderLeadDays = state.orderLeadDays.toIntOrNull(),
                remainingDaysOverride = state.remainingDaysOverride.toIntOrNull(),
                overrideDate = if (state.remainingDaysOverride.toIntOrNull() != null) LocalDate.now() else null
            )
            if (editingId != null) {
                dao.update(entity)
            } else {
                dao.insert(entity)
            }
            refreshWidget()
            _saved.value = true
        }
    }

    fun delete() {
        val id = editingId ?: return
        viewModelScope.launch {
            val med = dao.getById(id) ?: return@launch
            dao.delete(med)
            refreshWidget()
            _deleted.value = true
        }
    }
}

data class EditUiState(
    val personName: String = "",
    val name: String = "",
    val lastPickupDate: LocalDate = LocalDate.now(),
    val daysSupply: String = "28",
    val orderLeadDays: String = "",
    val remainingDaysOverride: String = "",
    val isEditing: Boolean = false
)
