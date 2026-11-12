package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFB0BEC5),
    onSecondary = Color(0xFF263238),
    tertiary = Color(0xFFFFB74D),
    background = SlateBackgroundDark,
    surface = SlateSurfaceDark,
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF263740),
    onSurfaceVariant = Color(0xFFCFD8DC)
)

private val LightColorScheme = lightColorScheme(
    primary = TealAccent,
    onPrimary = Color.White,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = Color(0xFF00201A),
    secondary = SlateSecondary,
    onSecondary = Color.White,
    tertiary = Color(0xFFF57C00),
    background = SageBackgroundLight,
    surface = SageSurfaceLight,
    onBackground = SlatePrimary,
    onSurface = SlatePrimary,
    surfaceVariant = Color(0xFFE8EEED),
    onSurfaceVariant = Color(0xFF37474F)
)

@Composable
fun EcoMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional clinical theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = EcoMapTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
