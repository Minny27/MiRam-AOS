package com.example.miram.shared.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val AccentColor = Color(0xFFFF0A54)
val AccentSurfaceColor = Color(0xFFFFB0C8)

private val LightColors = lightColorScheme(
    primary = AccentColor,
    onPrimary = Color.White,
    primaryContainer = AccentColor,
    onPrimaryContainer = Color.White,
    secondary = AccentColor,
    onSecondary = Color.White,
    secondaryContainer = AccentColor,
    onSecondaryContainer = Color.White,
    tertiary = AccentColor,
    onTertiary = Color.White,
    tertiaryContainer = AccentColor,
    onTertiaryContainer = Color.White,
    surfaceTint = AccentColor
)

private val DarkColors = darkColorScheme(
    primary = AccentColor,
    onPrimary = Color.White,
    primaryContainer = AccentColor,
    onPrimaryContainer = Color.White,
    secondary = AccentColor,
    onSecondary = Color.White,
    secondaryContainer = AccentColor,
    onSecondaryContainer = Color.White,
    tertiary = AccentColor,
    onTertiary = Color.White,
    tertiaryContainer = AccentColor,
    onTertiaryContainer = Color.White,
    surfaceTint = AccentColor
)

private val AppTypography = Typography().run {
    Typography(
        displayLarge = displayLarge.scaled(),
        displayMedium = displayMedium.scaled(),
        displaySmall = displaySmall.scaled(),
        headlineLarge = headlineLarge.scaled(),
        headlineMedium = headlineMedium.scaled(),
        headlineSmall = headlineSmall.scaled(),
        titleLarge = titleLarge.scaled(),
        titleMedium = titleMedium.scaled(),
        titleSmall = titleSmall.scaled(),
        bodyLarge = bodyLarge.scaled(),
        bodyMedium = bodyMedium.scaled(),
        bodySmall = bodySmall.scaled(),
        labelLarge = labelLarge.scaled(),
        labelMedium = labelMedium.scaled(),
        labelSmall = labelSmall.scaled()
    )
}

@Composable
fun MiRamTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) {
        DarkColors
    } else {
        LightColors
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        content = content
    )
}

private fun TextStyle.scaled(): TextStyle = copy(
    fontSize = (fontSize.value + 4f).sp,
    lineHeight = if (lineHeight.value.isFinite()) (lineHeight.value + 4f).sp else lineHeight
)
