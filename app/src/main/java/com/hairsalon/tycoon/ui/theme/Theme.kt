package com.hairsalon.tycoon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SalonColors = lightColorScheme(
    primary = Pink500,
    onPrimary = Color.White,
    secondary = Teal,
    onSecondary = Color.White,
    tertiary = Amber,
    onTertiary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF3E5EA),
    onSurfaceVariant = Color(0xFF5A4A50)
)

@Composable
fun SalonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SalonColors,
        typography = Typography(),
        content = content
    )
}
