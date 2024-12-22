package com.example.healthhive.ui.screens


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthhive.models.Appointment
import com.example.healthhive.models.Medication
import com.example.healthhive.models.Reminder
import com.example.healthhive.viewmodels.ReminderViewModel
import com.example.healthhive.viewmodels.calculateReminderTime
import com.example.healthhive.viewmodels.combineDateTime
import com.example.healthhive.viewmodels.showTimePicker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(viewModel: ReminderViewModel = viewModel()) {
    val reminders by viewModel.reminders.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    val dialogType = remember { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text("Reminders", style = MaterialTheme.typography.headlineSmall)
        })

    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { showDialog.value = true },
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Reminder",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(reminders) { reminder ->
                ReminderCard(reminder = reminder, onDelete = { viewModel.deleteReminder(reminder) })
            }
        }

        if (showDialog.value) {
            ReminderTypeDialog(onDismiss = { showDialog.value = false }, onSelectType = { type ->
                dialogType.value = type
                showDialog.value = false
            })
        }

        when (dialogType.value) {
            "Appointment" -> {
                AppointmentReminderDialog(appointments = viewModel.appointments.collectAsState().value,
                    onDismiss = { dialogType.value = "" },
                    onSetReminder = { appointmentId, time ->
                        viewModel.addAppointmentReminder(appointmentId, time)
                        dialogType.value = ""
                    })
            }

            "Medication" -> {
                MedicationReminderDialog(medications = viewModel.medications.collectAsState().value,
                    onDismiss = { dialogType.value = "" },
                    onSetReminder = { medicationId, time ->
                        viewModel.addMedicationReminder(medicationId, time)
                        dialogType.value = ""
                    })
            }

            else -> {}
        }
    }
}

@Composable
fun ReminderTypeDialog(onDismiss: () -> Unit, onSelectType: (String) -> Unit) {
    AlertDialog(onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Select Reminder Type",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = { onSelectType("Appointment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("Appointment")
                }
                Button(
                    onClick = { onSelectType("Medication") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("Medication")
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
    )
}


@Composable
fun MedicationReminderDialog(
    medications: List<Medication>, onDismiss: () -> Unit, onSetReminder: (String, Long) -> Unit
) {
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var selectedTime by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    AlertDialog(

        onDismissRequest = { onDismiss() }, title = {
            Text(
                text = "Set Medication Reminder", style = MaterialTheme.typography.headlineSmall
            )
        }, text = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (selectedMedication == null) {
                    Text(
                        text = "Choose a medication:", style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        items(medications) { medication ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedMedication = medication },
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = medication.medicineName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                } else {

                    Text("Set Reminder Time for:", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = selectedMedication?.medicineName.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showTimePicker(context) { hour, minute ->
                                val calendar = Calendar.getInstance()
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                calendar.set(Calendar.SECOND, 0)
                                calendar.set(Calendar.MILLISECOND, 0)
                                selectedTime = calendar.timeInMillis
                            }
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pick Time")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    selectedTime?.let { time ->
                        Text(
                            text = "Reminder Time: ${Date(time)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = {
                selectedMedication?.let { medication ->
                    selectedTime?.let { time ->
                        if (time > System.currentTimeMillis()) {
                            onSetReminder(medication.medicationId, time)
                        } else {
                            Log.e("MedicationReminder", "Selected time is in the past.")
                        }
                    }
                }
                onDismiss()
            }) {
                Text("Set Reminder")
            }
        }, dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        })
}

@Composable
fun AppointmentReminderDialog(
    appointments: List<Appointment>, onDismiss: () -> Unit, onSetReminder: (String, Long) -> Unit
) {
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }

    AlertDialog(onDismissRequest = { onDismiss() }, title = {
        Text(
            "Set Appointment Reminder", style = MaterialTheme.typography.headlineSmall
        )
    }, text = {
        Column(modifier = Modifier.padding(16.dp)) {
            if (selectedAppointment == null) {
                Text(
                    "Choose an appointment:", style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(appointments) { appointment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedAppointment = appointment },
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = appointment.doctorName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${appointment.date} at ${appointment.time}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            } else {

                Text(
                    "Set Reminder Time for:", style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${selectedAppointment?.doctorName} on ${selectedAppointment?.date} at ${selectedAppointment?.time}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        val time = combineDateTime(
                            selectedAppointment!!.date, selectedAppointment!!.time
                        )?.time
                        if (time != null) reminderTime = time
                    }) {
                        Text("On Time")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        val time = calculateReminderTime(
                            selectedAppointment!!.date, selectedAppointment!!.time, 60
                        ) // 1 hour before
                        if (time != null) reminderTime = time
                    }) {
                        Text("1 Hour Before")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        val time = calculateReminderTime(
                            selectedAppointment!!.date, selectedAppointment!!.time, 30
                        ) // 30 minutes before
                        if (time != null) reminderTime = time
                    }) {
                        Text("30 Minutes Before")
                    }
                }

                reminderTime?.let { time ->
                    Text(
                        text = "Reminder Time: ${Date(time)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }, confirmButton = {
        TextButton(onClick = {
            selectedAppointment?.let { appointment ->
                reminderTime?.let { time ->
                    onSetReminder(appointment.appointmentId, time)
                }
            }
            onDismiss()
        }) {
            Text("Set Reminder")
        }
    }, dismissButton = {
        TextButton(onClick = { onDismiss() }) {
            Text("Cancel")
        }
    })
}


@Composable
fun ReminderCard(reminder: Reminder, onDelete: (Reminder) -> Unit) {
    // Date formatting
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Format the date and time
    val formattedDate = dateFormat.format(Date(reminder.time))
    val formattedTime = timeFormat.format(Date(reminder.time))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.type, // Display medicine name
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$formattedTime | $formattedDate", // Display formatted time and date
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = { onDelete(reminder) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Reminder",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}








