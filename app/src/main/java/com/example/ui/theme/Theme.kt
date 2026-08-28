package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.HudTheme

fun getHudColorScheme(hudTheme: HudTheme) = when (hudTheme) {
    HudTheme.ULTRON_RED -> darkColorScheme(
        primary = UltronPrimary,
        onPrimary = Color.White,
        primaryContainer = FrostedGlassBg,
        onPrimaryContainer = UltronPrimaryGlow,
        secondary = UltronSecondary,
        onSecondary = Color.White,
        tertiary = UltronAccent,
        background = FrostedDarkBg,
        onBackground = FrostedTextPrimary,
        surface = UltronDarkSurface,
        onSurface = FrostedTextPrimary,
        surfaceVariant = FrotronSurfaceVariant(HudTheme.ULTRON_RED),
        onSurfaceVariant = FrostedTextSecondary,
        outline = FrostedGlassBorder
    )
    HudTheme.STARK_CYAN -> darkColorScheme(
        primary = StarkCyanPrimary,
        onPrimary = Color.Black,
        primaryContainer = FrostedGlassBg,
        onPrimaryContainer = StarkCyanGlow,
        secondary = StarkCyanGlow,
        onSecondary = Color.Black,
        tertiary = StarkCyanPrimary,
        background = FrostedDarkBg,
        onBackground = FrostedTextPrimary,
        surface = StarkCyanSurface,
        onSurface = FrostedTextPrimary,
        surfaceVariant = FrotronSurfaceVariant(HudTheme.STARK_CYAN),
        onSurfaceVariant = FrostedTextSecondary,
        outline = FrostedGlassBorder
    )
    HudTheme.MARK_GOLD -> darkColorScheme(
        primary = MarkGoldPrimary,
        onPrimary = Color.Black,
        primaryContainer = FrostedGlassBg,
        onPrimaryContainer = MarkGoldGlow,
        secondary = MarkGoldGlow,
        onSecondary = Color.Black,
        tertiary = MarkGoldPrimary,
        background = FrostedDarkBg,
        onBackground = FrostedTextPrimary,
        surface = MarkGoldSurface,
        onSurface = FrostedTextPrimary,
        surfaceVariant = FrotronSurfaceVariant(HudTheme.MARK_GOLD),
        onSurfaceVariant = FrostedTextSecondary,
        outline = FrostedGlassBorder
    )
}

private fun FrotronSurfaceVariant(hudTheme: HudTheme): Color = when (hudTheme) {
    HudTheme.ULTRON_RED -> Color(0xFF141216)
    HudTheme.STARK_CYAN -> Color(0xFF0F1B24)
    HudTheme.MARK_GOLD -> Color(0xFF1C170E)
}

@Composable
fun UltronTheme(
    hudTheme: HudTheme = HudTheme.ULTRON_RED,
    content: @Composable () -> Unit
) {
    val colorScheme = getHudColorScheme(hudTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

