package com.prescription.tracker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.updateAll
import com.prescription.tracker.data.SettingsManager
import com.prescription.tracker.widget.PrescriptionWidget
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val settings = SettingsManager(application)

    fun setWidgetBackgroundAlpha(alpha: Float) {
        settings.setWidgetBackgroundAlpha(alpha)
        refreshWidget()
    }

    fun setWidgetItemAlpha(alpha: Float) {
        settings.setWidgetItemAlpha(alpha)
        refreshWidget()
    }

    private fun refreshWidget() {
        viewModelScope.launch {
            PrescriptionWidget().updateAll(getApplication())
        }
    }
}
