package com.example.healthhive.models

data class Appointment(
    val appointmentId: String = "", // Unique appointment ID
    val userId: String = "", // User ID who is booking the appointment
    val doctorName: String = "", // Name of the doctor
    val date: String = "", // Appointment date
    val time: String = "", // Appointment time
)
