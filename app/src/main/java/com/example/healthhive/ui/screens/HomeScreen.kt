package com.example.healthhive.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.healthhive.R
import com.example.healthhive.models.Appointment
import com.example.healthhive.models.Order
import com.example.healthhive.ui.navigation.Destinations
import com.example.healthhive.viewmodels.HomeScreenViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeScreenViewModel = viewModel()

    val orders = viewModel.orders.value
    val appointments = viewModel.appointments.value
    val isLoading = viewModel.isLoading.value
    val isNewUser = viewModel.isNewUser.value
    val userName = getCurrentUserName()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
        }
    } else {
        if (isNewUser) {
            NewUserScreen(navController = navController)
        } else {
            ReturningUserScreen(
                userName = userName, orders = orders, appointments = appointments
            )
        }
    }
}


fun getCurrentUserName(): String {
    return FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
}

@Composable
fun NewUserScreen(navController: NavController) {
    val userName = getCurrentUserName()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFFE3F2FD)), startY = 0f, endY = 1500f
                )
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, $userName!", style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White
            ), modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "It looks like you're new here! Start exploring the app by:",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.DarkGray, lineHeight = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = painterResource(id = R.drawable.welcome_image),
            contentDescription = "Welcome image",
            modifier = Modifier
                .size(280.dp)
                .padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = """
                - Booking an appointment
                - Placing your first order
                - Setting a medication reminder
            """.trimIndent(), style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.DarkGray, lineHeight = 20.sp
            ), modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { navController.navigate(Destinations.APPOINTMENT) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(text = "Get Started", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ReturningUserScreen(
    userName: String,
    orders: List<Order>,
    appointments: List<Appointment>,

    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color.White), startY = 0f, endY = 1500f
                )
            )
    ) {
        // Header Section
        HeaderSection(
            title = "Welcome Back, $userName !", subtitle = "Manage your health journey"

        )

        Spacer(modifier = Modifier.height(16.dp))
        // Orders Section
        SectionHeader(title = "Order History", icon = R.drawable.orders2)
        if (orders.isNotEmpty()) {
            orders.forEach { order -> OrderCard(order) }
        } else {
            EmptyStateMessage("No orders found. Place your orders now!")
        }
        // Appointments Section
        SectionHeader(title = "Upcoming Appointments", icon = R.drawable.appointments)
        if (appointments.isNotEmpty()) {
            appointments.forEach { AppointmentCard(it) }
        } else {
            EmptyStateMessage("No appointments scheduled. Book one now!")
        }


    }
}


@Composable
fun EmptyStateMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
        ),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}


@Composable
fun HeaderSection(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title, style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 28.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
        )
    }
}


@Composable
fun SectionHeader(title: String, icon: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = "$title Icon",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}


@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            order.items.forEach { cartItem ->
                Text(
                    text = "${cartItem.medicineName} x${cartItem.quantity}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("Total Price: \$${order.totalPrice}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AppointmentCard(appointment: Appointment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(appointment.doctorName, style = MaterialTheme.typography.bodyLarge)
            Text(
                "Appointment Date: ${appointment.date}", style = MaterialTheme.typography.bodyMedium
            )
            Text("Time: ${appointment.time}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

