package com.example.healthhive.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthhive.models.Appointment
import com.example.healthhive.models.Doctor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppointmentScreen(viewModel: AppointmentViewModel = AppointmentViewModel()) {
    val context = LocalContext.current
    val doctors by remember { viewModel.doctorsList }
    val validAppointments by remember { viewModel.validAppointmentsList }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var filteredDoctors by remember { mutableStateOf(doctors) }
    val specialties = listOf("Cardiologist", "Dermatologist", "Neurologist", "Pediatrician", "Psychiatrist", "Orthopedic Surgeon", "Ophthalmologist")

    // Fetch doctors from Firestore when the screen is opened
    LaunchedEffect(Unit) {
        viewModel.fetchDoctors()
    }

    // Update filtered doctors based on search query
    LaunchedEffect(searchQuery, doctors) {
        filteredDoctors = if (searchQuery.text.isNotEmpty()) {
            doctors.filter {
                it.name.contains(searchQuery.text, ignoreCase = true) ||
                        it.specialty.contains(searchQuery.text, ignoreCase = true)
            }
        } else {
            doctors
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
        // Show Upcoming Appointments
        if (validAppointments.isNotEmpty()) {
            Text(
                text = "Upcoming Appointments",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(validAppointments.take(1)) { appointment ->
                    AppointmentCard1(appointment)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search doctors or specialties") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            maxLines = 1,
            singleLine = true
        )

        // Specialty Filter Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(specialties) { specialty ->
                Button(
                    onClick = { searchQuery = TextFieldValue(specialty) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = specialty, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Doctor List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredDoctors) { doctor ->
                DoctorCard(doctor, onBookAppointment = { viewModel.bookAppointment(doctor, context) })
            }
        }
    }
}

@Composable
fun AppointmentCard1(appointment: Appointment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = appointment.doctorName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(text = "Date: ${appointment.date}")
            Text(text = "Time: ${appointment.time}")
        }
    }
}

@Composable
fun DoctorCard(doctor: Doctor, onBookAppointment: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = doctor.specialty,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Experience: ${doctor.experience} years",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Rating: ${doctor.rating} ★",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = onBookAppointment,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                )
            ) {
                Text("Book", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

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

    // Fetch appointments from Firebase Firestore

    init {
        listenToAppointments()
    }



    // Real-time listener for appointments
    private fun listenToAppointments() {
        val userId = getCurrentUserId()
        appointmentsCollection
            .whereEqualTo("userId", userId)
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
    fun bookAppointment(doctor: Doctor, context: Context) {
        val userId = getCurrentUserId()
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(context, { _, year, month, dayOfMonth ->
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
                        appointmentsCollection.document(appointmentId).set(appointment).await()
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
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
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