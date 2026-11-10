package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioColorScheme = darkColorScheme(
    primary = AdobeBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF003875),
    onPrimaryContainer = AdobeCyan,
    secondary = AdobeCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D61),
    onSecondaryContainer = Color(0xFFB5EFFF),
    tertiary = AdobePurple,
    onTertiary = Color.White,
    background = StudioBg,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioCard,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    error = AdobeRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
