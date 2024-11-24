package com.example.healthhive.ui.screens

import android.app.Application

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.healthhive.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.Query


@Composable
fun OrdersScreen(navController: NavController, ordersViewModel: OrdersViewModel = viewModel()) {
    val orders by remember { ordersViewModel.orders }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Orders",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            fontSize = 30.sp
        )

        // Navigation buttons to Cart and Medicine screens
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(onClick = { navController.navigate("cart") }) {
                Text("Go to Cart")
            }
            Button(onClick = { navController.navigate("medicine") }) {
                Text("Go to Medicines")
            }
        }

        if (orders.isEmpty()) {
            Text("You have no orders yet.", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(orders) { order ->
                    OrderCard1(order)
                }
            }
        }
    }
}

@Composable
fun OrderCard1(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // List cart items for the order
            order.items.forEach { cartItem ->
                Text(text = "${cartItem.medicineName} x${cartItem.quantity}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "Delivery Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Total Price: $${"%.2f".format(order.totalPrice)}", style = MaterialTheme.typography.bodyMedium)




        }
    }
}

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
