package com.example.healthhive.ui.screens

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.example.healthhive.notifications.NotificationHelper
import com.example.healthhive.notifications.ReminderWorker

import java.util.concurrent.TimeUnit

@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit, onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Load the initial state of the notifications toggle
    var notificationsEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("notificationsEnabled", true))
    }

    fun onNotificationToggleChanged(enabled: Boolean) {
        notificationsEnabled = enabled
        sharedPreferences.edit().putBoolean("notificationsEnabled", enabled).apply()

        if (enabled) {
            // Reschedule all reminders using the rescheduleReminderNotifications function
            rescheduleReminderNotifications(context)
            Log.d("NotificationToggle", "Notifications enabled. Rescheduling reminders.")
        } else {
            // Cancel all scheduled notifications
            cancelReminderNotifications(context)
            Log.d("NotificationToggle", "Notifications disabled. All reminders canceled.")
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Notifications Section
        NotificationToggleSection(
            notificationsEnabled = notificationsEnabled,
            onToggleChange = ::onNotificationToggleChanged
        )

        // Settings Options
        SettingsOption(
            label = "Profile", icon = Icons.Default.Person, onClick = onNavigateToProfile
        )
        SettingsOption(
            label = "Privacy", icon = Icons.Default.Lock
        ) { /* Handle Privacy Navigation */ }
        SettingsOption(
            label = "Share", icon = Icons.Default.Share
        ) { /* Handle Share Navigation */ }
        SettingsOption(label = "Help", icon = Icons.Default.Info) { /* Handle Help Navigation */ }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = onLogout, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ), modifier = Modifier.fillMaxWidth(), elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Text("Log Out", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun NotificationToggleSection(
    notificationsEnabled: Boolean, onToggleChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Receive reminders and updates",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = onToggleChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
fun SettingsOption(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .padding(4.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

fun scheduleReminderNotifications(
    context: Context, reminderId: String, reminderTime: Long, reminderType: String
) {
    val workManager = WorkManager.getInstance(context)

    val currentTime = System.currentTimeMillis()
    val delay = reminderTime - currentTime

    // If the delay is negative (past time), schedule the reminder immediately
    val actualDelay = if (delay < 0) 0 else delay

    // Cancel any existing work for the same reminder ID to avoid duplicates
    workManager.cancelAllWorkByTag("reminder_$reminderId")

    // Create a new work request for this reminder
    val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(
            actualDelay,
            TimeUnit.MILLISECONDS
        ).setInputData(
            workDataOf(
                "reminderId" to reminderId,
                "reminderTime" to reminderTime,
                "reminderType" to reminderType
            )
        ).addTag("reminder_$reminderId") // Unique tag for this reminder
        .build()

    workManager.enqueue(workRequest)
    Log.d(
        "ScheduleReminder",
        "Scheduled ReminderWorker for reminderId=$reminderId with delay=$actualDelay ms."
    )
}


fun cancelReminderNotifications(context: Context) {
    val workManager = WorkManager.getInstance(context)
    Log.d("SettingsScreen", "Cancelling tasks with tag 'reminder_notification'.")

    // Cancel scheduled work by tag
    workManager.cancelAllWorkByTag("reminder_notification")

    // Cancel any active notifications as well
    val notificationHelper = NotificationHelper(context)
    notificationHelper.cancelAllNotifications()
    Log.d("SettingsScreen", "All active notifications canceled.")
}

fun rescheduleReminderNotifications(context: Context) {
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val workManager = WorkManager.getInstance(context)
    val allReminders = sharedPreferences.all

    var validRemindersFound = false

    for ((key, value) in allReminders) {
        if (key.startsWith("reminderId_")) {
            val reminderId = value as? String ?: continue
            val reminderTime = sharedPreferences.getLong("reminderTime_$reminderId", 0)
            val reminderType = sharedPreferences.getString("reminderType_$reminderId", null)

            // Ensure the reminder has valid time and type
            if (reminderTime > System.currentTimeMillis() && !reminderType.isNullOrBlank()) {
                validRemindersFound = true // At least one valid reminder exists

                // Cancel existing work for the same reminder ID to avoid duplicates
                workManager.cancelAllWorkByTag("reminder_$reminderId")

                // Calculate the delay for the reminder
                val delay = reminderTime - System.currentTimeMillis()

                // Schedule the new work request
                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(
                        delay,
                        TimeUnit.MILLISECONDS
                    ).setInputData(
                        workDataOf(
                            "reminderId" to reminderId,
                            "reminderTime" to reminderTime,
                            "reminderType" to reminderType
                        )
                    ).addTag("reminder_$reminderId") // Unique tag for this reminder
                    .build()

                workManager.enqueue(workRequest)

                Log.d(
                    "RescheduleReminder",
                    "Rescheduled ReminderWorker for reminderId=$reminderId with delay=${delay}ms."
                )
            } else {
                Log.d(
                    "RescheduleReminder",
                    "Invalid or past reminderTime for reminderId=$reminderId. Skipping."
                )
            }
        }
    }

    // Log if no valid reminders were found
    if (!validRemindersFound) {
        Log.d("RescheduleReminder", "No valid reminders found to reschedule.")
    }
}





