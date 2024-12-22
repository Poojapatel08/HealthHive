package com.example.healthhive.viewmodels

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthhive.models.Appointment
import com.example.healthhive.models.Doctor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppointmentViewModel : ViewModel() {


    private val db = FirebaseFirestore.getInstance()
    private val doctorsCollection = db.collection("doctors")
    private val appointmentsCollection = db.collection("appointments")
    private val _doctorsList = mutableStateOf<List<Doctor>>(emptyList())
    private val _appointmentsList = mutableStateOf<List<Appointment>>(emptyList())
    val doctorsList: State<List<Doctor>> get() = _doctorsList

    val validAppointmentsList: State<List<Appointment>> = derivedStateOf {
        _appointmentsList.value.filter { appointment ->
            val appointmentDateTime = parseDateTime(appointment.date, appointment.time)
            appointmentDateTime?.after(Date()) == true
        }.sortedWith(compareBy({ parseDateTime(it.date, it.time) }))
    }

    // Fetch doctors from Firebase Firestore
    fun fetchDoctors() {
        viewModelScope.launch {
            try {
                val snapshot = doctorsCollection.get().await()
                val doctors = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Doctor::class.java)
                }
                _doctorsList.value = doctors
            } catch (e: Exception) {
                println("Error fetching doctors: $e")
            }
        }
    }


    init {
        listenToAppointments()
    }


    // Real-time listener for appointments
    private fun listenToAppointments() {
        val userId = getCurrentUserId()
        appointmentsCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error listening to appointments: $error")
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)
                } ?: emptyList()
                _appointmentsList.value = appointments.sortedWith(compareBy({ it.date }))
            }
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "Anonymous"
    }

    // Book an appointment and save to Firebase
    @SuppressLint("DefaultLocale")
    fun bookAppointment(doctor: Doctor, context: Context) {
        val userId = getCurrentUserId()
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(context, { _, hourOfDay, minute ->
                    val appointmentId = UUID.randomUUID().toString()
                    val appointment = Appointment(
                        appointmentId = appointmentId,
                        userId = userId,
                        doctorName = doctor.name,
                        date = "$dayOfMonth-${month + 1}-$year",
                        time = String.format("%02d:%02d", hourOfDay, minute)
                    )

                    viewModelScope.launch {
                        try {
                            // Save the appointment to Firestore
                            appointmentsCollection.document(appointmentId).set(appointment).await()

                            // Update the user status to "not a new user" after the first appointment
                            updateUserStatus(userId)

                            Toast.makeText(
                                context,
                                "Appointment booked with ${doctor.name} on ${appointment.date} at ${appointment.time}",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Failed to book appointment: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun updateUserStatus(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // Data to update or create
        val userData = mapOf("isNewUser" to false)

        // Use set() with SetOptions.merge() to ensure the document is created if it doesn't exist
        db.collection("users").document(userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("HomeScreen", "User status updated to 'not new user'")
            }
            .addOnFailureListener { e ->
                Log.e("HomeScreen", "Error updating user status: ${e.message}")
            }
    }



    private fun parseDateTime(date: String, time: String): Date? {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return try {
            dateFormat.parse("$date $time")
        } catch (e: Exception) {
            null
        }
    }
}