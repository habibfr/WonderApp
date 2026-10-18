package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkIndigoPrimary,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = DarkIndigoContainer,
    onPrimaryContainer = OnDarkIndigoContainer,
    secondary = DarkCoralSecondary,
    onSecondary = Color(0xFF431407),
    secondaryContainer = DarkCoralContainer,
    onSecondaryContainer = OnDarkCoralContainer,
    tertiary = DarkMintTertiary,
    onTertiary = Color(0xFF064E3B),
    tertiaryContainer = DarkMintContainer,
    onTertiaryContainer = OnDarkMintContainer,
    background = DarkNightBackground,
    onBackground = DarkOnBackground,
    surface = DarkSlateSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = WonderIndigo,
    onPrimary = Color.White,
    primaryContainer = WonderIndigoContainer,
    onPrimaryContainer = OnWonderIndigoContainer,
    secondary = SparkyCoral,
    onSecondary = Color.White,
    secondaryContainer = SparkyCoralContainer,
    onSecondaryContainer = OnSparkyCoralContainer,
    tertiary = EmeraldMint,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldMintContainer,
    onTertiaryContainer = OnEmeraldMintContainer,
    background = LightCreamBackground,
    onBackground = LightOnBackground,
    surface = LightCardSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

val WonderQuestShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun WonderQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = WonderQuestShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WonderQuestTheme(darkTheme = darkTheme, content = content)
}
