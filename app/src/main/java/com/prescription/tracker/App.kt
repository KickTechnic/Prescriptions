package com.prescription.tracker

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.prescription.tracker.billing.BillingManager
import com.prescription.tracker.data.SettingsManager
import com.prescription.tracker.notification.NotificationScheduler

class App : Application() {

    lateinit var billingManager: BillingManager
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.createNotificationChannel(this)
        NotificationScheduler.scheduleDailyAlarm(this)
        MobileAds.initialize(this) {}
        billingManager = BillingManager(this, SettingsManager(this))
        billingManager.startConnection()
    }
}
