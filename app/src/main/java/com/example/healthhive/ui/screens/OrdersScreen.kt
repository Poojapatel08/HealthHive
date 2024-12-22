package com.example.healthhive.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healthhive.models.Order
import com.example.healthhive.viewmodels.OrdersViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavController, ordersViewModel: OrdersViewModel = viewModel()) {
    val orders by remember { ordersViewModel.orders }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Orders", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("cart") }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart")
                    }

                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (orders.isEmpty()) {
                    Text(
                        text = "You have no orders yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(orders) { order ->
                            OrderCard1(order)
                        }
                    }
                }
            }
        }
    )
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

