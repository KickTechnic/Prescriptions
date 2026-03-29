package com.prescription.tracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.prescription.tracker.MainActivity
import com.prescription.tracker.data.AppDatabase
import com.prescription.tracker.data.SettingsManager
import com.prescription.tracker.domain.MedicationStatus
import com.prescription.tracker.domain.MedicationWithStatus
import com.prescription.tracker.domain.StatusCalculator
import java.time.format.DateTimeFormatter

class PrescriptionWidget : GlanceAppWidget() {

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE dd MMM")

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.getInstance(context).medicationDao()
        val settings = SettingsManager(context).settingsFlow.value
        val medications = dao.getAll()
        val withStatus = StatusCalculator.calculateAll(medications, settings.globalLeadDays, settings.useBusinessDays)
        val bgAlpha = settings.widgetBackgroundAlpha
        val itemAlpha = settings.widgetItemAlpha

        provideContent {
            GlanceTheme {
                WidgetContent(withStatus, bgAlpha, itemAlpha)
            }
        }
    }

    @Composable
    private fun WidgetContent(
        medications: List<MedicationWithStatus>,
        bgAlpha: Float,
        itemAlpha: Float
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WIDGET_BG.copy(alpha = bgAlpha))
                .padding(8.dp)
        ) {
            // Header
            Text(
                text = "\uD83D\uDC8A Prescriptions",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorProvider(Color(0xFFE0E0E0))
                ),
                modifier = GlanceModifier.padding(bottom = 4.dp)
            )

            if (medications.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No prescriptions",
                        style = TextStyle(color = ColorProvider(Color(0xFF9E9E9E)))
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(medications, itemId = { it.id }) { med ->
                        WidgetRow(med, itemAlpha)
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetRow(med: MedicationWithStatus, itemAlpha: Float) {
        val (bgColor, textColor, emoji) = when (med.status) {
            MedicationStatus.OK -> Triple(Color(0xFF1B3A26), Color(0xFF8CD99E), "\uD83D\uDFE2")
            MedicationStatus.ORDER_SOON -> Triple(Color(0xFF3A3118), Color(0xFFFFD966), "\uD83D\uDFE1")
            MedicationStatus.ORDER_NOW -> Triple(Color(0xFF3A1820), Color(0xFFFF8A9E), "\uD83D\uDD34")
            MedicationStatus.OVERDUE -> Triple(Color(0xFF3A1820), Color(0xFFFF8A9E), "\u274C")
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(bgColor.copy(alpha = itemAlpha))
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                val label = buildString {
                    append(med.name)
                    if (!med.personName.isNullOrBlank()) {
                        append(" (${med.personName})")
                    }
                }
                Text(
                    text = label,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = ColorProvider(textColor)
                    ),
                    maxLines = 1
                )
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Runs out: ${med.runsOutDate.format(dateFormatter)}",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(textColor)
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "Order by: ${med.orderByDate.format(dateFormatter)}",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(textColor)
                        ),
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(modifier = GlanceModifier.height(2.dp))
    }

    companion object {
        private val WIDGET_BG = Color(0xFF1A1A2E)
    }
}
