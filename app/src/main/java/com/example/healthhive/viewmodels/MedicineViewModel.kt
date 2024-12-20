package com.example.healthhive.viewmodels

import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.healthhive.models.Medicine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MedicineViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _medicineList = mutableStateOf<List<Medicine>>(emptyList())
    val medicineList: State<List<Medicine>> get() = _medicineList

    fun fetchMedicines() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("medicines").get().await()
                val medicines = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Medicine::class.java)
                }
                _medicineList.value = medicines
            } catch (e: Exception) {
                println("Error fetching medicines: $e")
            }
        }
    }

    fun addToCart(medicine: Medicine, navController: NavController) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Check if the item already exists in the cart for the current user
                val querySnapshot = db.collection("cart")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("medicineId", medicine.id)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    // Item doesn't exist, create and add it to the cart
                    val cartItemId = UUID.randomUUID().toString()
                    val cartItem = hashMapOf(
                        "cartItemId" to cartItemId,
                        "userId" to userId,
                        "medicineId" to medicine.id,
                        "medicineName" to medicine.name,
                        "price" to medicine.price,
                        "quantity" to 1
                    )

                    // Add new cart item to the Firestore collection
                    db.collection("cart").add(cartItem).await()
                    Toast.makeText(
                        navController.context,
                        "${medicine.name} added to cart",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Item exists, update the quantity
                    val existingItem = querySnapshot.documents.first()
                    val currentQuantity = existingItem.getLong("quantity") ?: 0
                    val updatedQuantity = currentQuantity + 1

                    // Update the quantity of the existing item in Firestore
                    db.collection("cart")
                        .document(existingItem.id)
                        .update("quantity", updatedQuantity)
                        .await()

                    Toast.makeText(
                        navController.context,
                        "Quantity of ${medicine.name} increased to $updatedQuantity",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    navController.context,
                    "Error adding to cart: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}
