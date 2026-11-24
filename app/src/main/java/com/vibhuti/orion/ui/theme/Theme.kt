package com.vibhuti.orion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OrionColorScheme = darkColorScheme(
    primary = OrionColors.SleepAge,
    background = OrionColors.Background,
    onBackground = OrionColors.TextPrimary,
    surface = OrionColors.Surface,
    onSurface = OrionColors.TextPrimary,
    onSurfaceVariant = OrionColors.TextSecondary,
    outline = OrionColors.SurfaceBorder,
    error = OrionColors.Poor,
)

/** The design is dark-only. */
@Composable
fun OrionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = OrionColorScheme, typography = OrionTypography, content = content)
}
