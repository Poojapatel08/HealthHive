package com.example.healthhive.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.healthhive.models.Medicine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Composable
fun MedicineScreen(
    navController: NavController,
    viewModel: MedicineViewModel = MedicineViewModel()
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val medicines by remember { viewModel.medicineList }
    var filteredMedicines by remember { mutableStateOf(medicines) }

    // Fetch medicines when the screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchMedicines()
    }

    // Update filtered list based on search query
    LaunchedEffect(searchQuery, medicines) {
        filteredMedicines = if (searchQuery.text.isNotEmpty()) {
            medicines.filter {
                it.name.contains(searchQuery.text, ignoreCase = true)
            }
        } else {
            medicines
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Medicines",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 30.sp,
        )
        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,

            ) {
            Button(onClick = { navController.navigate("cart") }) {
                Text("Go to Cart")
            }
            Button(onClick = { navController.navigate("orders") }) {
                Text("Go to Orders")
            }
        }
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search medicines") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            maxLines = 1,
            singleLine = true
        )

        if (filteredMedicines.isEmpty()) {
            // Show message if no medicines match the search
            Text(
                text = "No medicines found for '${searchQuery.text}'",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Medicine List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredMedicines) { medicine ->
                    MedicineCard(
                        medicine = medicine,
                        onAddToCart = { viewModel.addToCart(medicine, navController) }
                    )
                }
            }
        }


    }
}

@Composable
fun MedicineCard(medicine: Medicine, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Price: ${medicine.price}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onAddToCart) {
                Text("Add to Cart")
            }
        }
    }
}

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
