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
        val notificationsEnabled: Boolean = true,
        val widgetBackgroundAlpha: Float = 0.85f,
        val widgetItemAlpha: Float = 0.95f
    )

    private fun currentSettings() = Settings(
        globalLeadDays = prefs.getInt("global_lead_days", 7),
        notificationHour = prefs.getInt("notification_hour", 9),
        notificationMinute = prefs.getInt("notification_minute", 0),
        notificationsEnabled = prefs.getBoolean("notifications_enabled", true),
        widgetBackgroundAlpha = prefs.getFloat("widget_bg_alpha", 0.85f),
        widgetItemAlpha = prefs.getFloat("widget_item_alpha", 0.95f)
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

    fun setWidgetBackgroundAlpha(alpha: Float) {
        val clamped = alpha.coerceIn(0f, 1f)
        prefs.edit().putFloat("widget_bg_alpha", clamped).apply()
        // Ensure item alpha is at least as high as background alpha
        val currentItemAlpha = prefs.getFloat("widget_item_alpha", 0.95f)
        if (currentItemAlpha < clamped) {
            prefs.edit().putFloat("widget_item_alpha", clamped).apply()
        }
        _settingsFlow.value = currentSettings()
    }

    fun setWidgetItemAlpha(alpha: Float) {
        val bgAlpha = prefs.getFloat("widget_bg_alpha", 0.85f)
        val clamped = alpha.coerceIn(bgAlpha, 1f)
        prefs.edit().putFloat("widget_item_alpha", clamped).apply()
        _settingsFlow.value = currentSettings()
    }
}
