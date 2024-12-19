package com.example.healthhive.viewmodels

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.healthhive.models.Appointment
import com.example.healthhive.models.Order

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import java.util.*

class HomeScreenViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val _orders = mutableStateOf<List<Order>>(emptyList())
    val orders: State<List<Order>> = _orders

    private val _appointments = mutableStateOf<List<Appointment>>(emptyList())
    val appointments: State<List<Appointment>> = _appointments



    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _isNewUser = mutableStateOf(false)
    val isNewUser: State<Boolean> = _isNewUser

    init {
        fetchData()
    }

    private fun fetchData() {
        userId?.let { id ->
            _isLoading.value = true

            // Fetch user document to check if the user is new or returning
            db.collection("users").document(id).get().addOnSuccessListener { document ->
                // Check the value of isNewUser field
                val isNewUser = document.getBoolean("isNewUser") ?: true  // Default to true if field not found
                _isNewUser.value = isNewUser  // Update the _isNewUser state

                // Orders Listener
                db.collection("orders").whereEqualTo("userId", id).addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e("HomeScreen", "Error listening to orders: ${error.message}")
                        return@addSnapshotListener
                    }
                    val orders = value?.toObjects<Order>().orEmpty()
                    _orders.value = orders.sortedByDescending { it.orderDate }.take(3)
                }

                // Appointments Listener
                db.collection("appointments").whereEqualTo("userId", id).addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e("HomeScreen", "Error listening to appointments: ${error.message}")
                        return@addSnapshotListener
                    }
                    val allAppointments = value?.toObjects<Appointment>().orEmpty()
                    val validAppointments = allAppointments.filter {
                        isValidDateTime(it.date, it.time)
                    }.sortedBy {
                        combineDateTime(it.date, it.time)
                    }.take(3)
                    _appointments.value = validAppointments
                }

                _isLoading.value = false
            }.addOnFailureListener { e ->
                Log.e("HomeScreen", "Error fetching user data: ${e.message}")
                _isLoading.value = false
            }
        }
    }

}
private fun isValidDateTime(date: String, time: String): Boolean {
    val combined = combineDateTime(date, time)
    return combined?.after(Date()) == true
}