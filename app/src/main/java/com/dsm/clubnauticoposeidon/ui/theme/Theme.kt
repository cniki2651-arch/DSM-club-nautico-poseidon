package com.dsm.clubnauticoposeidon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PoseidonColorScheme = darkColorScheme(
    primary = Gold500,
    onPrimary = Navy900,
    primaryContainer = Gold600,
    onPrimaryContainer = Ink,

    secondary = Gold400,
    onSecondary = Navy900,

    tertiary = Muted,
    onTertiary = Navy900,

    background = Navy900,
    onBackground = Ink,

    surface = Navy800,
    onSurface = Ink,

    surfaceVariant = Navy700,
    onSurfaceVariant = Muted,

    outline = Navy600,

    error = ErrorRed,
)

@Composable
fun ClubNauticoPoseidonTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PoseidonColorScheme,
        typography = Typography,
        content = content
    )
}