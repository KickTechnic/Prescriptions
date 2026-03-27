package com.prescription.tracker.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("prescription_settings", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(currentSettings())
    val settingsFlow: StateFlow<Settings> = _settingsFlow.asStateFlow()

    data class Settings(
        val globalLeadDays: Int = 7,
        val notificationHour: Int = 9,
        val notificationMinute: Int = 0,
        val notificationsEnabled: Boolean = true
    )

    private fun currentSettings() = Settings(
        globalLeadDays = prefs.getInt("global_lead_days", 7),
        notificationHour = prefs.getInt("notification_hour", 9),
        notificationMinute = prefs.getInt("notification_minute", 0),
        notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
    )

    fun setGlobalLeadDays(days: Int) {
        prefs.edit().putInt("global_lead_days", days).apply()
        _settingsFlow.value = currentSettings()
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt("notification_hour", hour)
            .putInt("notification_minute", minute)
            .apply()
        _settingsFlow.value = currentSettings()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _settingsFlow.value = currentSettings()
    }
}
