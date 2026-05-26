package com.raulcn.freeed.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = FreeEdGold500,
    onPrimary = FreeEdInk900,
    primaryContainer = FreeEdBlue700,
    onPrimaryContainer = FreeEdWhite,
    secondary = FreeEdGray300,
    onSecondary = FreeEdInk900,
    tertiary = FreeEdGold200,
    onTertiary = FreeEdInk900,
    background = FreeEdInk900,
    onBackground = FreeEdWhite,
    surface = ColorTokens.DarkSurface,
    onSurface = FreeEdWhite,
    surfaceVariant = ColorTokens.DarkSurfaceVariant,
    onSurfaceVariant = FreeEdGray300,
    outline = FreeEdGray500
)

private val LightColorScheme = lightColorScheme(
    primary = FreeEdBlue900,
    onPrimary = FreeEdWhite,
    primaryContainer = FreeEdGray100,
    onPrimaryContainer = FreeEdBlue900,
    secondary = FreeEdGold500,
    onSecondary = FreeEdInk900,
    tertiary = FreeEdGold200,
    onTertiary = FreeEdBlue900,
    background = FreeEdGray050,
    onBackground = FreeEdInk900,
    surface = FreeEdWhite,
    onSurface = FreeEdInk900,
    surfaceVariant = FreeEdGray100,
    onSurfaceVariant = FreeEdGray700,
    outline = FreeEdGray300
)

@Composable
fun FreeedTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = FreeEdShapes,
        content = content
    )
}

private val FreeEdShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

private object ColorTokens {
    val DarkSurface = FreeEdBlue800
    val DarkSurfaceVariant = FreeEdBlue700
}
