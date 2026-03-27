package com.prescription.tracker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.prescription.tracker.data.SettingsManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val settings = SettingsManager(application)
}
