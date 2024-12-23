package com.example.healthhive.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.healthhive.models.Medicine
import com.example.healthhive.viewmodels.MedicineViewModel


@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicines", style = MaterialTheme.typography.headlineSmall) },

                actions = {
                    IconButton(onClick = { navController.navigate("cart") }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart")
                    }

                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Orders")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(6.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search medicines") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
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
    )
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

