package com.example.healthhive.models

data class Reminder(
    val medicationName: String = "",
    val dose: String = "",
    val date: String = "",
    val reminderTime: String = ""
)
