package com.example.healthhive

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.healthhive.notifications.NotificationHelper
import com.example.healthhive.ui.screens.MainScreen
import com.example.healthhive.ui.theme.HealthHiveTheme

class MainActivity : ComponentActivity() {

    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        notificationHelper = NotificationHelper(this).apply {
            createNotificationChannel()
        }

        // Install the splash screen
        installSplashScreen()

        // Request notification permission for Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handleNotificationPermission()
        } else {
            Log.d("MainActivity", "Notification permission not required for this Android version.")
        }

        setContent {
            HealthHiveTheme {
                MainScreen()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                Log.d("MainActivity", "Notification permission already granted.")
            }

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Show rationale and request permission
                Log.d("MainActivity", "Showing rationale for notification permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            else -> {

                Log.d("MainActivity", "Requesting notification permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted.")
        } else {
            Log.e("MainActivity", "Notification permission denied.")

        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Log.d("MainActivity", "Configuration changed: ${newConfig.orientation}")
    }
}
