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
    primary = Color(0xFFBE8EF7),
    secondary = Color(0xFFF795B4),
    tertiary = Color(0xFFE5D5F8),
    background = DarkBgLavender,
    surface = PhoneSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF2F2F7),
    onSurface = Color(0xFFF2F2F7)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFBE8EF7),
    secondary = Color(0xFFF795B4),
    tertiary = Color(0xFFE5D5F8),
    background = BgLavender,
    surface = PhoneSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1E1E24),
    onSurface = Color(0xFF1E1E24)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
