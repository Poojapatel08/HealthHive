package com.example.healthhive.ui.screens

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
import com.example.healthhive.models.Appointment
import com.example.healthhive.models.Doctor
import com.example.healthhive.viewmodels.AppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen(

    viewModel: AppointmentViewModel = AppointmentViewModel()
) {
    val context = LocalContext.current
    val doctors by remember { viewModel.doctorsList }
    val validAppointments by remember { viewModel.validAppointmentsList }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var filteredDoctors by remember { mutableStateOf(doctors) }
    val specialties = listOf(
        "Cardiologist", "Dermatologist", "Neurologist",
        "Pediatrician", "Psychiatrist", "Orthopedic Surgeon", "Ophthalmologist"
    )

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments", style = MaterialTheme.typography.headlineSmall) },


                )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 10.dp)
            ) {
                // Show Upcoming Appointments
                if (validAppointments.isNotEmpty()) {
                    Text(
                        text = "Upcoming Appointments",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 2.dp)
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
                        DoctorCard(
                            doctor,
                            onBookAppointment = { viewModel.bookAppointment(doctor, context) })
                    }
                }
            }
        }
    )
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

