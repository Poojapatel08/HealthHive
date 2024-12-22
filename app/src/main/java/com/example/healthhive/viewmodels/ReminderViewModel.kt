package com.example.healthhive.viewmodels

import android.app.Application
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.healthhive.models.Appointment
import com.example.healthhive.models.Medication
import com.example.healthhive.models.Order
import com.example.healthhive.models.Reminder
import com.example.healthhive.notifications.ReminderWorker

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.collections.filter
import kotlin.collections.orEmpty
import kotlin.collections.sortedBy

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUserId"
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    private val _orders = MutableStateFlow<List<Order>>(emptyList())

    val reminders: StateFlow<List<Reminder>> get() = _reminders
    val appointments: StateFlow<List<Appointment>> get() = _appointments
    val medications: StateFlow<List<Medication>> get() = _medications
    val orders: StateFlow<List<Order>> get() = _orders

    private val workManager: WorkManager = WorkManager.getInstance(application)
    private val sharedPreferences =
        application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Boolean flag to check if notifications are enabled
    private var notificationsEnabled: Boolean
        get() = sharedPreferences.getBoolean("notificationsEnabled", true)
        set(value) {
            sharedPreferences.edit().putBoolean("notificationsEnabled", value).apply()
        }

    init {
        fetchReminders()
        fetchAppointments()
        fetchOrders()
    }

    // Add appointment reminder
    fun addAppointmentReminder(appointmentId: String, time: Long) {
        viewModelScope.launch {
            val reminder = Reminder(
                reminderId = UUID.randomUUID().toString(),
                type = "Appointment",
                userId = userId,
                linkedId = appointmentId,
                time = time,
                status = "Scheduled"
            )
            db.collection("reminders").document(reminder.reminderId).set(reminder)
                .addOnSuccessListener {
                    if (notificationsEnabled) {
                        scheduleReminder(reminder)
                    }
                    Log.d("ReminderViewModel", "Appointment reminder added and scheduled.")
                }.addOnFailureListener { e ->
                    Log.e("ReminderViewModel", "Error adding appointment reminder: ${e.message}")
                }
        }
    }

    // Add medication reminder
    fun addMedicationReminder(medicationId: String, time: Long) {
        viewModelScope.launch {
            val reminder = Reminder(
                reminderId = UUID.randomUUID().toString(),
                type = "Medication",
                userId = userId,
                linkedId = medicationId,
                time = time,
                status = "Scheduled"
            )
            db.collection("reminders").document(reminder.reminderId).set(reminder)
                .addOnSuccessListener {
                    if (notificationsEnabled) {
                        scheduleReminder(reminder)
                    }
                    Log.d("ReminderViewModel", "Medication reminder added and scheduled.")
                }.addOnFailureListener { e ->
                    Log.e("ReminderViewModel", "Error adding medication reminder: ${e.message}")
                }
        }
    }

    // Fetch reminders from Firestore
    private fun fetchReminders() {
        db.collection("reminders").whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val allReminders = snapshot?.toObjects(Reminder::class.java) ?: emptyList()
                val validReminders =
                    allReminders.filter { reminder -> reminder.time > System.currentTimeMillis() }
                _reminders.value = validReminders
            }
    }

    // Schedule reminder notifications
    private fun scheduleReminder(reminder: Reminder) {
        val currentTime = System.currentTimeMillis()
        val delay = reminder.time - currentTime

        if (delay <= 0) {
            Log.e(
                "ReminderViewModel",
                "Cannot schedule a reminder in the past or immediately. Delay: $delay"
            )
            return
        }

        val inputData = workDataOf(
            "reminderId" to reminder.reminderId,
            "reminderTime" to reminder.time,
            "reminderType" to reminder.type
        )

        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>().setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(reminder.reminderId) // Tag added for canceling later
            .build()

        workManager.enqueue(reminderRequest)
        Log.d("ReminderViewModel", "Reminder scheduled successfully!")
    }

    // Fetch appointments
    private fun fetchAppointments() {
        db.collection("appointments").whereEqualTo("userId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("ReminderViewModel", "Error fetching appointments: ${error.message}")
                    return@addSnapshotListener
                }

                val allAppointments = value?.toObjects<Appointment>().orEmpty()
                val currentDateTime = Date()
                val validAppointments = allAppointments.filter {
                    val appointmentDateTime = combineDateTime(it.date, it.time)
                    appointmentDateTime?.after(currentDateTime) == true
                }.sortedBy { combineDateTime(it.date, it.time) }

                _appointments.value = validAppointments
            }
    }

    // Fetch orders
    private fun fetchOrders() {
        db.collection("orders").whereEqualTo("userId", userId).addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                val ordersList = it.toObjects(Order::class.java)
                _orders.value = ordersList
                val allMedications = ordersList.flatMap { order ->
                    order.items.map { item ->
                        Medication(
                            medicationId = item.medicineId, medicineName = item.medicineName
                        )
                    }
                }
                _medications.value = allMedications
            }
        }
    }

    // Delete reminder and cancel associated work
    fun deleteReminder(reminder: Reminder) {
        workManager.cancelAllWorkByTag(reminder.reminderId)
        db.collection("reminders").document(reminder.reminderId).delete().addOnSuccessListener {
            Log.d("ReminderViewModel", "Reminder deleted successfully.")
        }.addOnFailureListener { e ->
            Log.e("ReminderViewModel", "Error deleting reminder: ${e.message}")
        }
    }

    // Reschedule reminders
    fun rescheduleReminders() {
        val validReminders =
            _reminders.value.filter { reminder -> reminder.time > System.currentTimeMillis() }
        Log.d("ReminderViewModel", "Rescheduling ${validReminders.size} reminders.")
        validReminders.forEach { reminder ->
            if (notificationsEnabled) {
                scheduleReminder(reminder)
            }
        }
    }

    // Cancel all reminders and their associated notifications
    fun cancelAllReminders() {
        val validReminders =
            _reminders.value.filter { reminder -> reminder.time > System.currentTimeMillis() }
        validReminders.forEach { reminder ->
            workManager.cancelAllWorkByTag(reminder.reminderId)
        }
        Log.d("ReminderViewModel", "All reminders canceled.")
    }



}

fun combineDateTime(date: String, time: String): Date? {
    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
    return try {
        formatter.parse("$date $time")
    } catch (e: Exception) {
        null
    }
}

fun showTimePicker(context: Context, onTimeSelected: (Int, Int) -> Unit) {
    val currentTime = Calendar.getInstance()
    val hour = currentTime.get(Calendar.HOUR_OF_DAY)
    val minute = currentTime.get(Calendar.MINUTE)

    TimePickerDialog(
        context, { _, selectedHour, selectedMinute ->
            onTimeSelected(selectedHour, selectedMinute)
        }, hour, minute, true
    ).show()
}

fun calculateReminderTime(date: String, time: String, minutesBefore: Int): Long? {
    val appointmentDateTime = combineDateTime(date, time) ?: return null
    val calendar = Calendar.getInstance()
    calendar.time = appointmentDateTime
    calendar.add(Calendar.MINUTE, -minutesBefore)
    return calendar.timeInMillis
}