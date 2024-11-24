package com.example.healthhive.models


data class Cart(
    val userId: String = "",
    val items: MutableList<CartItem> = mutableListOf()
)

