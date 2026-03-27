package com.prescription.tracker

import android.app.Application
import com.prescription.tracker.notification.NotificationScheduler

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.createNotificationChannel(this)
        NotificationScheduler.scheduleDailyAlarm(this)
    }
}
