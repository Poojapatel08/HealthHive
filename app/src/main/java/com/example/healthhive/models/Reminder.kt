package com.example.healthhive.models



data class Reminder(
    val reminderId: String = "",
    val type: String = "",
    val userId: String = "",
    val linkedId: String = "",
    val time: Long = 0L,
    val status: String = ""
)