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
    primary = DarkSkyPrimary,
    onPrimary = Color(0xFF082F49),
    primaryContainer = DarkSkyContainer,
    onPrimaryContainer = OnDarkSkyContainer,
    secondary = DarkCoralSecondary,
    onSecondary = Color(0xFF450A0A),
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
    primary = SkyBlueDeep,
    onPrimary = Color.White,
    primaryContainer = SkyBlueContainer,
    onPrimaryContainer = OnSkyBlueContainer,
    secondary = PlayfulCoralDeep,
    onSecondary = Color.White,
    secondaryContainer = PlayfulCoralContainer,
    onSecondaryContainer = OnPlayfulCoralContainer,
    tertiary = MintGreenDeep,
    onTertiary = Color.White,
    tertiaryContainer = MintGreenContainer,
    onTertiaryContainer = OnMintGreenContainer,
    background = WarmCreamBackground,
    onBackground = DeepSlateText,
    surface = CrispWhiteSurface,
    onSurface = DeepSlateText,
    surfaceVariant = SoftCreamSurfaceVariant,
    onSurfaceVariant = MutedSlateText
)

val WonderQuestShapes = Shapes(
    small = RoundedCornerShape(16.dp),
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
    androidx.compose.runtime.CompositionLocalProvider(
        LocalPlayfulThemeTokens provides PlayfulThemeTokens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WonderQuestDesignSystem.TypographyTokens.MaterialTypography,
            shapes = WonderQuestDesignSystem.ShapeTokens.MaterialShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WonderQuestTheme(darkTheme = darkTheme, content = content)
}
