package com.prescription.tracker.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prescription.tracker.domain.MedicationStatus
import com.prescription.tracker.domain.MedicationWithStatus
import com.prescription.tracker.ui.theme.StatusAmberBg
import com.prescription.tracker.ui.theme.StatusAmberText
import com.prescription.tracker.ui.theme.StatusGreenBg
import com.prescription.tracker.ui.theme.StatusGreenText
import com.prescription.tracker.ui.theme.StatusRedBg
import com.prescription.tracker.ui.theme.StatusRedText
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onAddClick: () -> Unit,
    onMedicationClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MedicationListViewModel = viewModel()
) {
    val medications by viewModel.medications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prescriptions") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add medication")
            }
        }
    ) { padding ->
        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No prescriptions yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap + to add your first medication",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(medications, key = { it.id }) { med ->
                    MedicationCard(
                        medication = med,
                        onClick = { onMedicationClick(med.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(
    medication: MedicationWithStatus,
    onClick: () -> Unit
) {
    val (bgColor, textColor, statusLabel, statusEmoji) = when (medication.status) {
        MedicationStatus.OK -> StatusColors(StatusGreenBg, StatusGreenText, "OK", "\uD83D\uDFE2")
        MedicationStatus.ORDER_SOON -> StatusColors(StatusAmberBg, StatusAmberText, "Order Soon", "\uD83D\uDFE1")
        MedicationStatus.ORDER_NOW -> StatusColors(StatusRedBg, StatusRedText, "Order Now", "\uD83D\uDD34")
        MedicationStatus.OVERDUE -> StatusColors(StatusRedBg, StatusRedText, "Overdue", "\u274C")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Medication info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!medication.personName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = medication.personName!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Runs out: ${medication.runsOutDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Order by: ${medication.orderByDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$statusEmoji $statusLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class StatusColors(
    val bg: Color,
    val text: Color,
    val label: String,
    val emoji: String
)
