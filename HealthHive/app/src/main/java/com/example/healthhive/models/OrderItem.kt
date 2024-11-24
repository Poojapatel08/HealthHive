package com.example.healthhive.models

data class OrderItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
)
