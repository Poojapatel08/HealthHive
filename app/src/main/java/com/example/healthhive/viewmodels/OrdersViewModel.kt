package com.example.healthhive.viewmodels

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.healthhive.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrdersViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = mutableStateOf<List<Order>>(emptyList())
    val orders: State<List<Order>> get() = _orders

    init {
        fetchOrders()
    }

    // Fetch orders from Firestore
    fun fetchOrders() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING) // New to old order
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        println("Error fetching orders: $exception")
                        return@addSnapshotListener
                    }

                    val ordersList = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Order::class.java)
                    } ?: emptyList()

                    _orders.value = ordersList
                }
        }
    }
}
