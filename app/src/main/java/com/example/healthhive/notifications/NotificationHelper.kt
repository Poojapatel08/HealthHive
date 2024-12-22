package com.example.healthhive.notifications

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.healthhive.MainActivity
import com.example.healthhive.R
import androidx.core.app.NotificationManagerCompat
import java.util.Date
import java.util.UUID

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Health Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Health reminders"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(reminderId: String, reminderTime: Long, reminderType: String) {
        val notificationId = reminderId.hashCode() // Unique ID for each reminder
        val formattedTime = java.text.SimpleDateFormat("HH:mm").format(Date(reminderTime))
        val contentText = "Reminder for $reminderType at $formattedTime"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification) // Ensure this drawable exists
            .setContentTitle("Health Reminder")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(createContentIntent())
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Includes sound, vibration, and lights
            .build()

        notificationManager.notify(notificationId, notification)
    }

    // Method to cancel a specific notification
    fun cancelNotification(reminderId: String) {
        val notificationId = reminderId.hashCode()
        notificationManager.cancel(notificationId)
    }

    // Method to cancel all notifications
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    companion object {
        const val CHANNEL_ID = "HEALTH_REMINDER_CHANNEL"
    }
}

