package com.example.healthhive.model

data class UserProfile(
    val userId: String = "",        // Unique identifier for the user
    val name: String = "",          // User's name
    val age: String = "",
    val mobileNumber: String = "",
    val address: String = "",

)