package com.example.healthhive.ui.screens


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healthhive.R
import com.example.healthhive.models.CartItem
import com.example.healthhive.viewmodels.CartViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, cartViewModel: CartViewModel = viewModel()) {
    val cartItems by remember { cartViewModel.cartItems }
    val totalAmount by remember { cartViewModel.totalAmount }
    var deliveryAddress by remember { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    Scaffold(topBar = {
        TopAppBar(title = { Text("Shopping Cart", style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
                    )
                }
            },
            actions = {

                IconButton(onClick = { navController.navigate("orders") }) {
                    Icon(
                        imageVector = Icons.Default.List, contentDescription = "Orders"
                    )
                }
            })
    }, content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (cartItems.isEmpty()) {
                Text(
                    text = "Your cart is empty.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cartItems) { cartItem ->
                        CartItemCard(cartItem, cartViewModel)
                    }
                }
            }


            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total: $${"%.2f".format(totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Button(
                    onClick = {
                        if (deliveryAddress.isNotEmpty()) {
                            cartViewModel.checkout(
                                userId, cartItems, totalAmount, deliveryAddress
                            ) { isSuccess ->
                                if (isSuccess) {
                                    Toast.makeText(
                                        navController.context,
                                        "Order placed successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("home") // Redirect to home or order summary page
                                } else {
                                    Toast.makeText(
                                        navController.context,
                                        "Error placing order. Try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                navController.context,
                                "Please enter a delivery address",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, modifier = Modifier.width(150.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3), contentColor = Color.White
                    )
                ) {
                    Text("Checkout")
                }
            }
        }
    })
}


@Composable
fun CartItemCard(cartItem: CartItem, cartViewModel: CartViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cartItem.medicineName, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Price: $${"%.2f".format(cartItem.price)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Quantity: ${cartItem.quantity}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { cartViewModel.decreaseQuantity(cartItem) }) {
                    Image(
                        painter = painterResource(id = R.drawable.minus),
                        contentDescription = "Decrease Quantity",
                        modifier = Modifier.size(28.dp)

                    )
                }

                IconButton(onClick = { cartViewModel.increaseQuantity(cartItem) }) {
                    Icon(
                        imageVector = Icons.Default.Add, contentDescription = "Increase quantity"
                    )
                }

                IconButton(onClick = { cartViewModel.removeItem(cartItem) }) {
                    Icon(
                        imageVector = Icons.Default.Delete, contentDescription = "Remove item"
                    )
                }
            }
        }
    }
}








