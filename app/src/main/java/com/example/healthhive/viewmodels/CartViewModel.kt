package com.example.healthhive.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthhive.models.CartItem
import com.example.healthhive.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.collections.forEach

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
    fun checkout(
        userId: String,
        cartItems: List<CartItem>,
        totalAmount: Float,
        deliveryAddress: String,
        onResult: (Boolean) -> Unit
    ) {
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

                // Update the user status to 'not new user' after the first order
                updateUserStatus(userId)

                onResult(true)
            } catch (e: Exception) {
                println("Error during checkout: $e")
                onResult(false)
            }
        }
    }

    // Function to update the user status to "not new user"
    private fun updateUserStatus(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // Data to update or create
        val userData = mapOf("isNewUser" to false)

        // Use set() with SetOptions.merge() to ensure the document is created if it doesn't exist
        db.collection("users").document(userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("HomeScreen", "User status updated to 'not new user'")
            }
            .addOnFailureListener { e ->
                Log.e("HomeScreen", "Error updating user status: ${e.message}")
            }
    }

}
