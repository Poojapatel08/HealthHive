package com.example.healthhive.models

data class Medication(
    val medicationId: String = "",
    val userId: String = "",
    val medicineName: String = "",
    val reminderTimes: List<String> = emptyList()
)