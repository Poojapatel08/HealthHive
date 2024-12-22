package com.example.healthhive.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healthhive.ui.navigation.Destinations
import com.example.healthhive.ui.navigation.MainNavHost


@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    // Screens with a bottom navigation bar
    val screensWithBottomNavBar = listOf(
        Destinations.HOME,
        Destinations.PROFILE,
        Destinations.APPOINTMENT,
        Destinations.REMINDERS,
        Destinations.SETTINGS,
        Destinations.MEDICINE,
        Destinations.CART,
        Destinations.ORDERS
    )



    Scaffold(
        bottomBar = {
            if (currentRoute in screensWithBottomNavBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            MainNavHost(navController = navController)

        }

    }
}



