package com.example.healthhive.ui.screens

import android.R.attr.icon
import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthhive.models.CartItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthhive.R

import com.google.firebase.auth.FirebaseAuth

import androidx.navigation.NavController
import com.example.healthhive.models.Order
import java.util.UUID


@Composable
fun CartScreen(navController: NavController, cartViewModel: CartViewModel = viewModel()) {
    val cartItems by remember { cartViewModel.cartItems }
    val totalAmount by remember { cartViewModel.totalAmount }
    var deliveryAddress by remember { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Shopping Cart",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 30.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navController.navigate("medicine") }) {
                Text("Go to Medicines")
            }
            Button(onClick = { navController.navigate("orders") }) {
                Text("Go to Orders")
            }
        }

        if (cartItems.isEmpty()) {
            Text(
                text = "Your cart is empty.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems) { cartItem ->
                    CartItemCard(cartItem, cartViewModel)
                }
            }
        }
        // Delivery Address Field
        OutlinedTextField(
            value = deliveryAddress,
            onValueChange = { deliveryAddress = it },
            label = { Text("Enter Delivery Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            maxLines = 1,
            singleLine = true
        )



        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total: $${"%.2f".format(totalAmount)}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            Button(
                onClick = {
                    if (deliveryAddress.isNotEmpty()) {
                        cartViewModel.checkout(userId, cartItems, totalAmount, deliveryAddress) { isSuccess ->
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
                        Toast.makeText(navController.context, "Please enter a delivery address", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.width(150.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White)
            ) {
                Text("Checkout")
            }
        }
    }
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
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase quantity"
                    )
                }

                IconButton(onClick = { cartViewModel.removeItem(cartItem) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove item"
                    )
                }
            }
        }
    }
}

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val cartCollection = db.collection("cart")
    private val _cartItems = mutableStateOf<List<CartItem>>(emptyList())
    val cartItems: State<List<CartItem>> get() = _cartItems

    val totalAmount: State<Float> = derivedStateOf {
        _cartItems.value.sumOf { it.price * it.quantity }.toFloat()
    }

    init {
        fetchCartItems()
    }

    // Fetch cart items from Firestore for the logged-in user
    private fun fetchCartItems() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            cartCollection.whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        println("Error fetching cart items: $exception")
                        return@addSnapshotListener
                    }
                    _cartItems.value = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(CartItem::class.java)?.copy(cartItemId = doc.id)
                    } ?: emptyList()
                }
        }
    }

    // Increase item quantity
    fun increaseQuantity(cartItem: CartItem) {
        val updatedItem = cartItem.copy(quantity = cartItem.quantity + 1)
        updateCartItemInFirestore(updatedItem)
    }

    // Decrease item quantity
    fun decreaseQuantity(cartItem: CartItem) {
        if (cartItem.quantity > 1) {
            val updatedItem = cartItem.copy(quantity = cartItem.quantity - 1)
            updateCartItemInFirestore(updatedItem)
        }
    }

    // Remove item from cart
    fun removeItem(cartItem: CartItem) {
        viewModelScope.launch {
            try {
                cartCollection.document(cartItem.cartItemId).delete().await()
                _cartItems.value = _cartItems.value.filter { it.cartItemId != cartItem.cartItemId }
            } catch (e: Exception) {
                println("Error removing item: $e")
            }
        }
    }

    // Update cart item in Firestore
    private fun updateCartItemInFirestore(updatedItem: CartItem) {
        viewModelScope.launch {
            try {
                cartCollection.document(updatedItem.cartItemId).set(updatedItem).await()
                _cartItems.value = _cartItems.value.map {
                    if (it.cartItemId == updatedItem.cartItemId) updatedItem else it
                }
            } catch (e: Exception) {
                println("Error updating item: $e")
            }
        }
    }

    // Checkout function
    fun checkout(userId: String, cartItems: List<CartItem>, totalAmount: Float, deliveryAddress: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val orderId = UUID.randomUUID().toString()
                val order = Order(
                    id = orderId,
                    userId = userId,
                    items = cartItems,
                    totalPrice = totalAmount.toDouble(),
                    orderDate = System.currentTimeMillis().toString(),
                    deliveryAddress = deliveryAddress
                )

                // Add the order to Firestore
                db.collection("orders").document(orderId).set(order).await()

                // Clear the cart after successful order
                cartItems.forEach { cartItem ->
                    db.collection("cart").document(cartItem.cartItemId).delete().await()
                }

                onResult(true)
            } catch (e: Exception) {
                println("Error during checkout: $e")
                onResult(false)
            }
        }
    }
}







