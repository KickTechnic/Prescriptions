package com.prescription.tracker.ui.settings

import android.app.Activity
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.settingsFlow.collectAsState()
    val context = LocalContext.current
    var leadDaysText by remember(settings.globalLeadDays) {
        mutableStateOf(settings.globalLeadDays.toString())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lead days
            Text("Ordering", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = leadDaysText,
                onValueChange = { input ->
                    leadDaysText = input.filter { it.isDigit() }
                    leadDaysText.toIntOrNull()?.let { days ->
                        if (days in 1..30) viewModel.settings.setGlobalLeadDays(days)
                    }
                },
                label = { Text("Order X days in advance") },
                supportingText = { Text("How many days before running out should you reorder?") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Skip weekends", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Doctors don't process orders on weekends",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.useBusinessDays,
                    onCheckedChange = { viewModel.settings.setUseBusinessDays(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notifications section
            Text("Notifications", style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Daily reminders", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Get notified when prescriptions need ordering",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { viewModel.settings.setNotificationsEnabled(it) }
                        )
                    }

                    if (settings.notificationsEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            viewModel.settings.setNotificationTime(hour, minute)
                                        },
                                        settings.notificationHour,
                                        settings.notificationMinute,
                                        true
                                    ).show()
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reminder time", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = String.format("%02d:%02d", settings.notificationHour, settings.notificationMinute),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Widget section
            Text("Widget", style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Widget background opacity", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${(settings.widgetBackgroundAlpha * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = settings.widgetBackgroundAlpha,
                        onValueChange = { viewModel.setWidgetBackgroundAlpha(it) },
                        valueRange = 0f..1f,
                        steps = 19
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Item background opacity", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${(settings.widgetItemAlpha * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = settings.widgetItemAlpha,
                        onValueChange = { viewModel.setWidgetItemAlpha(it) },
                        valueRange = settings.widgetBackgroundAlpha..1f,
                        steps = ((1f - settings.widgetBackgroundAlpha) * 20).toInt().coerceAtLeast(0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ads section
            Text("Ads", style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (settings.adsRemoved) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ads removed", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        Column {
                            Text("Remove ads", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "One-time purchase to remove banner ads",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = {
                            (context as? Activity)?.let { viewModel.launchRemoveAds(it) }
                        }) {
                            Text("Purchase")
                        }
                    }
                }
            }
        }
    }
}
