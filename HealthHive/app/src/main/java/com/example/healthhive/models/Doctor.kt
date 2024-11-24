package com.example.healthhive.models

data class Doctor(

    val name: String = "",
    val contact: String="",
    val specialty: String = "", // Specialty of the doctor (e.g., Cardiologist)
    val experience: Int = 0, // Years of experience
    val rating: Float =0f// Average rating of the doctor
)