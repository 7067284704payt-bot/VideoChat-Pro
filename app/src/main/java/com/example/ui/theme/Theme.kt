package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VideoChatColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = TextPrimary,
    primaryContainer = VioletDeep,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    secondaryContainer = CardSurfaceElevated,
    onSecondaryContainer = SkyCyan,
    tertiary = GoldEnd,
    onTertiary = GoldDarkText,
    background = DeepSpaceBg,
    onBackground = TextPrimary,
    surface = MidnightIndigo,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = BadgeRed,
    onError = TextPrimary
)

@Composable
fun VideoChatProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VideoChatColorScheme,
        typography = AppTypography,
        content = content
    )
}
