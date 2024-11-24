package com.example.healthhive.ui.theme

import androidx.compose.material3.MaterialTheme


import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


// Define the Shapes for the app
val shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

// Define the Light Color Scheme for the app
val lightColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

// Define the Dark Color Scheme for the app
val darkColorScheme = darkColorScheme(
    primary = Color(0xFFffffff),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)


@Composable
fun HealthHiveTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Select color scheme based on dark or light theme
    val colors = if (darkTheme) darkColorScheme else lightColorScheme

    // Wrap the MaterialTheme with color scheme, typography, and shapes
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),  // Use the default Typography
        shapes = shapes,  // Set the custom shapes
        content = content  // Apply the theme to the content
    )
}
