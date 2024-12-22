package com.example.healthhive.notifications

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters


class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val TAG = "ReminderWorker"
    private val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    override fun doWork(): Result {
        // Check if the work has been stopped
        if (isStopped) {
            Log.d(TAG, "Work canceled, stopping ReminderWorker.")
            return Result.failure()
        }

        // Retrieve input data
        val reminderId = inputData.getString("reminderId") ?: run {
            Log.e(TAG, "Reminder ID is missing. Work cannot proceed.")
            return Result.failure()
        }
        val reminderTime = inputData.getLong("reminderTime", 0)
        if (reminderTime == 0L) {
            Log.e(TAG, "Invalid reminder time. Work cannot proceed.")
            return Result.failure()
        }
        val reminderType = inputData.getString("reminderType") ?: "Reminder"

        // Check if notifications are enabled in shared preferences
        val notificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)

        // If notifications are disabled, cancel the work and don't show the notification
        if (!notificationsEnabled) {
            Log.d(TAG, "Notifications are disabled, skipping reminder.")
            return Result.success() // No need to show notification if notifications are disabled
        }

        Log.d(TAG, "Running ReminderWorker: reminderId=$reminderId, type=$reminderType")

        try {
            // Show notification
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showReminderNotification(reminderId, reminderTime, reminderType)
            Log.d(TAG, "Notification displayed successfully for reminderId=$reminderId")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying notification for reminderId=$reminderId: ${e.message}")
            return Result.failure()
        }
    }
}

