package com.biometricapp.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    object Light {
        val Primary = Color(0xFFD0BCFF)
        val Secondary = Color(0xFFCCC2DC)
        val Tertiary = Color(0xFFEFB8C8)
    }

    object Dark {
        val Primary = Color(0xFF6650a4)
        val Secondary = Color(0xFF625b71)
        val Tertiary = Color(0xFF7D5260)
    }

    // You could also have common/neutral colors here
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
}

// Usage:
// val backgroundColor = AppColors.Light.Primary