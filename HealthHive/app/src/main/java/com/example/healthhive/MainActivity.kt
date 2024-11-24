package com.example.healthhive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.healthhive.ui.screens.MainScreen
import com.example.healthhive.ui.theme.HealthHiveTheme
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            HealthHiveTheme{
                MainScreen() // Launch the main screen with persistent bottom navigation
            }
        }


    }


}




