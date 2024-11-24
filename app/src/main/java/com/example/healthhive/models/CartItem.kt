package com.example.healthhive.models

data class CartItem(
    val cartItemId: String = "",
    val medicineId: String = "",
    val medicineName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val userId: String = ""
)
