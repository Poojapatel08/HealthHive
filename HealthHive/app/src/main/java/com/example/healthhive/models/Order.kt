package com.example.healthhive.models

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val orderDate: String = "",
    val deliveryAddress: String = ""
)
