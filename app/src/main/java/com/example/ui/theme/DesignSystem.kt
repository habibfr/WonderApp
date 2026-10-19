package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralized Playful Children's Game Design System for WonderQuest.
 * Provides harmonious color tokens, child-friendly rounded typography constants,
 * tactile rounded shapes, soft elevation styles, and accessible touch/spacing dimensions.
 */
object WonderQuestDesignSystem {

    /**
     * Playful, high-contrast color palette tailored for ages 4–13 in both
     * Daylight Island Mode and Low-Eye-Strain Starry Night (Dark) Mode.
     */
    object Colors {
        // Core Brand & Playful Palette
        val SkyBlue = SkyBluePrimary              // #55C7F7
        val SkyBlueInteractive = SkyBlueDeep      // #0284C7
        val SkyBlueSoft = SkyBlueContainer        // #E0F4FE
        val OnSkyBlueSoft = OnSkyBlueContainer    // #0C4A6E

        val SunshineYellow = com.example.ui.theme.SunshineYellow // #FFD95A
        val SunshineGold = SunshineAmber          // #F59E0B
        val SunshineSoft = SunshineContainer      // #FFF6D6
        val OnSunshineSoft = OnSunshineContainer  // #78350F

        val CoralAccent = PlayfulCoral            // #FF7F73
        val CoralInteractive = PlayfulCoralDeep   // #E05347
        val CoralSoft = PlayfulCoralContainer     // #FFE6E4
        val OnCoralSoft = OnPlayfulCoralContainer // #7F1D1D

        val MintSuccess = MintGreenSuccess        // #72DDA8
        val MintInteractive = MintGreenDeep       // #10B981
        val MintSoft = MintGreenContainer         // #DDFBEF
        val OnMintSoft = OnMintGreenContainer     // #064E3B

        val PlayfulPurple = com.example.ui.theme.PlayfulPurple // #A98BFA
        val PurpleInteractive = PlayfulPurpleDeep // #7C3AED
        val PurpleSoft = PlayfulPurpleContainer   // #EDE9FE
        val OnPurpleSoft = OnPlayfulPurpleContainer // #3B0764

        // Surfaces & Text (Light Mode)
        val CreamBackground = WarmCreamBackground // #FFF9EE
        val WhiteSurface = CrispWhiteSurface      // #FFFFFF
        val CreamSurfaceVariant = SoftCreamSurfaceVariant // #F5EFE2
        val PrimaryText = DeepSlateText           // #30334A
        val SecondaryText = MutedSlateText        // #5A5E78

        // Starry Night Surfaces & Text (Dark Mode)
        val NightBackground = DarkNightBackground // #141829
        val NightSurface = DarkSlateSurface       // #1F253D
        val NightSurfaceVariant = DarkSurfaceVariant // #2C3454
        val NightPrimaryText = DarkOnBackground   // #F8FAFC
        val NightSecondaryText = DarkOnSurfaceVariant // #CBD5E1

        // Gamified Reward & Feedback Tokens
        val StarGold = Color(0xFFFFB300)
        val TrophyAmber = Color(0xFFEAB308)
        val BerryPink = Color(0xFFEC4899)
        val CrystalTeal = Color(0xFF0D9488)
        val StreakFlame = Color(0xFFFF7043)

        // Pre-built Playful Brushes
        val IslandSkyGradient = Brush.verticalGradient(
            colors = listOf(SkyBlue, PlayfulPurple, SunshineYellow)
        )

        val NightSkyGradient = Brush.verticalGradient(
            colors = listOf(NightBackground, Color(0xFF1E1B4B), Color(0xFF312E81))
        )

        val GoldenRewardGradient = Brush.horizontalGradient(
            colors = listOf(SunshineYellow, StarGold, SunshineGold)
        )
    }

    /**
     * Child-friendly typography constants using rounded display (`Fredoka`)
     * and highly legible rounded body (`Nunito`) fonts.
     */
    object TypographyTokens {
        val DisplayFontFamily: FontFamily = FredokaFontFamily
        val BodyFontFamily: FontFamily = NunitoFontFamily

        // Font Size Scale
        val HeroTitleSize: TextUnit = 40.sp
        val ScreenTitleSize: TextUnit = 28.sp
        val SectionTitleSize: TextUnit = 22.sp
        val CardTitleSize: TextUnit = 19.sp
        val EquationDisplaySize: TextUnit = 26.sp
        val PuzzlePromptSize: TextUnit = 18.sp
        val BodyLargeSize: TextUnit = 16.sp
        val BodyMediumSize: TextUnit = 14.sp
        val ButtonLabelSize: TextUnit = 17.sp
        val BadgeLabelSize: TextUnit = 13.sp
        val ScoreCounterSize: TextUnit = 20.sp

        // Pre-configured Semantic Game TextStyles
        val HeroDisplayStyle = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = HeroTitleSize,
            lineHeight = 48.sp
        )

        val ScreenHeaderStyle = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = ScreenTitleSize,
            lineHeight = 36.sp
        )

        val PuzzleEquationStyle = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = EquationDisplaySize,
            lineHeight = 34.sp
        )

        val ChildInstructionStyle = TextStyle(
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = PuzzlePromptSize,
            lineHeight = 25.sp
        )

        val GameButtonTextStyle = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = ButtonLabelSize,
            lineHeight = 22.sp
        )

        val ScoreBadgeTextStyle = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = ScoreCounterSize,
            lineHeight = 24.sp
        )

        val MaterialTypography: Typography = Typography
    }

    /**
     * Soft, rounded shape constants ensuring every interactive card, button,
     * token tile, and modal feels safe, friendly, and toy-like.
     */
    object ShapeTokens {
        val CornerExtraSmall: Dp = 12.dp
        val CornerSmall: Dp = 16.dp
        val CornerMedium: Dp = 22.dp
        val CornerLarge: Dp = 28.dp
        val CornerExtraLarge: Dp = 36.dp

        val TokenTileShape: Shape = RoundedCornerShape(18.dp)
        val GameButtonShape: Shape = RoundedCornerShape(22.dp)
        val AnswerOptionShape: Shape = RoundedCornerShape(24.dp)
        val QuestCardShape: Shape = RoundedCornerShape(28.dp)
        val HeroBannerShape: Shape = RoundedCornerShape(32.dp)
        val CelebrationModalShape: Shape = RoundedCornerShape(36.dp)
        val PillBadgeShape: Shape = RoundedCornerShape(percent = 50)
        val AvatarCircleShape: Shape = CircleShape

        val MaterialShapes: Shapes = WonderQuestShapes
    }

    /**
     * Soft shadow and layered surface elevation constants for tactile depth.
     */
    object ElevationTokens {
        val Flat: Dp = 0.dp
        val SubtleSurface: Dp = 2.dp
        val InteractiveTile: Dp = 4.dp
        val QuestCard: Dp = 6.dp
        val HeroStage: Dp = 8.dp
        val CelebrationModal: Dp = 12.dp
        val ButtonBottomLipHeight: Dp = 4.dp
        val ButtonPressedLipHeight: Dp = 1.dp

        @Composable
        fun questCardElevation(): CardElevation = CardDefaults.cardElevation(
            defaultElevation = QuestCard,
            pressedElevation = SubtleSurface,
            hoveredElevation = HeroStage,
            focusedElevation = QuestCard
        )

        @Composable
        fun heroCardElevation(): CardElevation = CardDefaults.cardElevation(
            defaultElevation = HeroStage,
            pressedElevation = InteractiveTile
        )

        @Composable
        fun celebrationCardElevation(): CardElevation = CardDefaults.cardElevation(
            defaultElevation = CelebrationModal
        )
    }

    /**
     * Child-ergonomic spacing and touch target constants following the 8.dp grid
     * and exceeding the 48.dp minimum accessibility touch target standard.
     */
    object SpacingTokens {
        val SpaceXXS: Dp = 4.dp
        val SpaceXS: Dp = 8.dp
        val SpaceSM: Dp = 12.dp
        val SpaceMD: Dp = 16.dp
        val SpaceLG: Dp = 20.dp
        val SpaceXL: Dp = 24.dp
        val SpaceXXL: Dp = 32.dp

        val MinTouchTarget: Dp = 48.dp
        val GameButtonMinHeight: Dp = 54.dp
        val AnswerTileHeight: Dp = 76.dp
        val MaxReadableContentWidth: Dp = 620.dp
    }
}

@Immutable
data class PlayfulThemeTokens(
    val colors: WonderQuestDesignSystem.Colors = WonderQuestDesignSystem.Colors,
    val typography: WonderQuestDesignSystem.TypographyTokens = WonderQuestDesignSystem.TypographyTokens,
    val shapes: WonderQuestDesignSystem.ShapeTokens = WonderQuestDesignSystem.ShapeTokens,
    val elevations: WonderQuestDesignSystem.ElevationTokens = WonderQuestDesignSystem.ElevationTokens,
    val spacing: WonderQuestDesignSystem.SpacingTokens = WonderQuestDesignSystem.SpacingTokens
)

val LocalPlayfulThemeTokens = staticCompositionLocalOf { PlayfulThemeTokens() }

object PlayfulTheme {
    val tokens: PlayfulThemeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalPlayfulThemeTokens.current
}
