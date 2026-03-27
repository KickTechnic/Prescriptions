package com.prescription.tracker.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationScreen(
    medicationId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: EditMedicationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val deleted by viewModel.deleted.collectAsState()
    val settings by viewModel.settings.settingsFlow.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPickupDatePicker by remember { mutableStateOf(false) }
    var showLastPickupDatePicker by remember { mutableStateOf(false) }
    var showSupplyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(medicationId) {
        if (medicationId != null && medicationId > 0) {
            viewModel.loadMedication(medicationId)
        }
    }

    LaunchedEffect(saved) { if (saved) onNavigateBack() }
    LaunchedEffect(deleted) { if (deleted) onNavigateBack() }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medication") },
            text = { Text("Are you sure you want to delete ${state.name}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete() }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPickupDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPickupDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.markPickedUp(date)
                    }
                    showPickupDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showPickupDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showLastPickupDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.lastPickupDate
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showLastPickupDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.updateLastPickupDate(date)
                    }
                    showLastPickupDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showLastPickupDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showSupplyDialog) {
        var supplyInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSupplyDialog = false },
            title = { Text("Adjust Remaining Supply") },
            text = {
                OutlinedTextField(
                    value = supplyInput,
                    onValueChange = { supplyInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Days of supply remaining") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    supplyInput.toIntOrNull()?.let { viewModel.adjustSupply(it) }
                    showSupplyDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSupplyDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Medication" else "Add Medication") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.personName,
                onValueChange = viewModel::updatePersonName,
                label = { Text("Person (optional)") },
                placeholder = { Text("e.g. Lucy, Mark") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("Medication name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Last pickup date with calendar button
            OutlinedTextField(
                value = state.lastPickupDate.format(dateFormatter),
                onValueChange = {},
                label = { Text("Last pickup date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showLastPickupDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.daysSupply,
                onValueChange = { viewModel.updateDaysSupply(it.filter { c -> c.isDigit() }) },
                label = { Text("Days supply") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.orderLeadDays,
                onValueChange = { viewModel.updateOrderLeadDays(it.filter { c -> c.isDigit() }) },
                label = { Text("Order lead days (optional)") },
                placeholder = { Text("Default: ${settings.globalLeadDays} days") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.name.isNotBlank() && state.daysSupply.toIntOrNull() != null
            ) {
                Text(if (state.isEditing) "Save Changes" else "Add Medication")
            }

            // Edit-only actions
            if (state.isEditing) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPickupDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Mark Picked Up")
                    }

                    OutlinedButton(
                        onClick = { showSupplyDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Adjust Supply")
                    }
                }
            }
        }
    }
}
