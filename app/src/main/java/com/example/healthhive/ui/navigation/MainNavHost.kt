package com.example.healthhive.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healthhive.ui.screens.ReminderScreen
import com.example.healthhive.ui.screens.*
import com.google.firebase.auth.FirebaseAuth

object Destinations {

    const val LOGIN = "login"
    const val HOME = "home"
    const val MEDICINE = "medicine"
    const val PROFILE = "profile"
    const val APPOINTMENT = "appointment"
    const val REMINDERS = "reminders"
    const val SIGN_UP = "sign_up"
    const val SETTINGS = "settings"
    const val CART = "cart"
    const val ORDERS = "orders"

}

@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String = if (FirebaseAuth.getInstance().currentUser != null) Destinations.HOME else Destinations.LOGIN
) {

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Destinations.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Destinations.HOME) {
                    popUpTo(Destinations.LOGIN) { inclusive = true }
                }
            }, onSignUp = {
                navController.navigate(Destinations.SIGN_UP)
            })
        }
        composable(Destinations.HOME) {
            HomeScreen(navController = navController)

        }
        composable(Destinations.APPOINTMENT) {
            AppointmentScreen()
        }

        composable(Destinations.PROFILE) {
            ProfileScreen()
        }
        composable(Destinations.MEDICINE) {
            MedicineScreen(navController = navController)
        }
        composable(Destinations.CART) {
            CartScreen(navController = navController)
        }
        composable(Destinations.ORDERS) {
            OrdersScreen(navController = navController)
        }


        composable(Destinations.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.SIGN_UP) { inclusive = true }
                    }
                }, navController = navController
            )
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(onNavigateToProfile = { navController.navigate("profile") }, onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("settings") { inclusive = true }
                }
            })
        }
        composable(Destinations.REMINDERS) {
            ReminderScreen()
        }

    }
}


