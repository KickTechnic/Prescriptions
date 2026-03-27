package com.prescription.tracker.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prescription.tracker.MainActivity
import com.prescription.tracker.R
import com.prescription.tracker.data.AppDatabase
import com.prescription.tracker.data.SettingsManager
import com.prescription.tracker.domain.MedicationStatus
import com.prescription.tracker.domain.StatusCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendNotifications(context)
            } finally {
                // Re-schedule for tomorrow
                NotificationScheduler.scheduleDailyAlarm(context)
                pendingResult.finish()
            }
        }
    }

    private suspend fun sendNotifications(context: Context) {
        val dao = AppDatabase.getInstance(context).medicationDao()
        val settings = SettingsManager(context).settingsFlow.value
        val medications = dao.getAll()
        val withStatus = StatusCalculator.calculateAll(medications, settings.globalLeadDays)

        val needsAttention = withStatus.filter {
            it.status != MedicationStatus.OK
        }

        if (needsAttention.isEmpty()) return

        val notificationManager = NotificationManagerCompat.from(context)

        needsAttention.forEachIndexed { index, med ->
            val (title, text) = when (med.status) {
                MedicationStatus.OVERDUE -> {
                    val person = if (med.personName != null) "${med.personName}'s " else ""
                    "${person}${med.name} has run out!" to "This prescription is overdue. Pick it up as soon as possible."
                }
                MedicationStatus.ORDER_NOW -> {
                    val person = if (med.personName != null) "${med.personName}'s " else ""
                    "Order ${person}${med.name} now!" to "The order deadline is today or has passed."
                }
                MedicationStatus.ORDER_SOON -> {
                    val person = if (med.personName != null) "${med.personName}'s " else ""
                    "${person}${med.name} needs ordering soon" to "Runs out on ${med.runsOutDate}. Order by ${med.orderByDate}."
                }
                else -> return@forEachIndexed
            }

            // Open app intent
            val openIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("medication_id", med.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPendingIntent = PendingIntent.getActivity(
                context, (2000 + index), openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // "Picked Up" action intent
            val pickedUpIntent = Intent(context, PickedUpReceiver::class.java).apply {
                putExtra("medication_id", med.id)
                putExtra("notification_id", (3000 + index))
            }
            val pickedUpPendingIntent = PendingIntent.getBroadcast(
                context, (3000 + index), pickedUpIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)
                .addAction(
                    R.drawable.ic_notification,
                    "Picked Up",
                    pickedUpPendingIntent
                )
                .build()

            try {
                notificationManager.notify(3000 + index, notification)
            } catch (_: SecurityException) {
                // Permission not granted
            }
        }
    }
}
