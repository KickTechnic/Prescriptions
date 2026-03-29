package com.prescription.tracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import com.prescription.tracker.data.AppDatabase
import com.prescription.tracker.data.PickupHistoryEntity
import com.prescription.tracker.widget.PrescriptionWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class PickedUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val medicationId = intent?.getLongExtra("medication_id", -1L) ?: return
        val notificationId = intent.getIntExtra("notification_id", -1)
        if (medicationId < 0) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val dao = db.medicationDao()
                val today = LocalDate.now()
                dao.markPickedUp(medicationId, today)
                db.pickupHistoryDao().insert(PickupHistoryEntity(medicationId = medicationId, pickupDate = today))

                // Dismiss the notification
                if (notificationId >= 0) {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }

                // Refresh widget
                PrescriptionWidget().updateAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
