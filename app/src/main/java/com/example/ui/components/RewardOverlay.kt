package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BadgeCatalog
import com.example.data.BuddyCharacter
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.WonderQuestDesignSystem
import com.example.util.SoundManager

/**
 * Reusable 'RewardOverlay' component that plays a joyful celebration animation and sound effect
 * when a level or quest is completed, displaying the stars and badges earned inside a playful,
 * bouncy popup modal.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RewardOverlay(
    visible: Boolean = true,
    title: String = "Level Selesai!",
    subtitle: String? = null,
    earnedStars: Int = 3,
    maxStars: Int = 3,
    correctAnswers: Int = 5,
    totalQuestions: Int = 5,
    unlockedBadgeIds: List<String> = emptyList(),
    buddy: BuddyCharacter = BuddyCharacter.KOMODO,
    languageCode: String = "id",
    accentColor: Color = WonderQuestDesignSystem.Colors.MintInteractive,
    isSoundMuted: Boolean = false,
    primaryButtonText: String = "Main Level Berikutnya",
    secondaryButtonText: String = "Kembali ke Pulau",
    questionProgressLabel: String = "Skor Jawaban",
    starsEarnedLabel: String = "Bintang",
    newBadgesUnlockedLabel: String = "Lencana Baru Terbuka!",
    onPrimaryAction: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Trigger joyful level-complete fanfare and badge sparkle chime when overlay appears
    LaunchedEffect(visible, earnedStars, unlockedBadgeIds) {
        if (visible) {
            SoundManager.playLevelComplete(!isSoundMuted)
            if (unlockedBadgeIds.isNotEmpty()) {
                SoundManager.playRewardTrigger(!isSoundMuted)
            }
        }
    }

    var bounceTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        bounceTriggered = visible
    }

    val modalScale by animateFloatAsState(
        targetValue = if (bounceTriggered) 1f else 0.78f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "reward_modal_bounce_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "reward_overlay_infinite")
    val trophyBobOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_bob_offset"
    )
    val starPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_pulse_scale"
    )
    val badgeTiltDeg by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_tilt_deg"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260)) + scaleIn(
            initialScale = 0.82f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.9f)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.38f),
                            accentColor.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(20.dp)
                .testTag("level_complete_card"),
            contentAlignment = Alignment.Center
        ) {
            // 1. Continuous Joyful Star & Sparkle Confetti Burst
            StarSparkleCelebrationOverlay(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("reward_overlay_sparkles")
            )

            // 2. Playful, Bouncy Popup Modal Card
            Card(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .scale(modalScale)
                    .testTag("reward_overlay_modal"),
                shape = WonderQuestDesignSystem.ShapeTokens.CelebrationModalShape,
                border = BorderStroke(3.5.dp, WonderQuestDesignSystem.Colors.SunshineGold),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    WonderQuestDesignSystem.Colors.SunshineSoft.copy(alpha = 0.65f),
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Hero Celebration Row: Floating Trophy + Happy Buddy Character
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_illus_level_complete),
                            contentDescription = title,
                            modifier = Modifier
                                .size(92.dp)
                                .offset(y = trophyBobOffset.dp)
                                .testTag("reward_overlay_trophy_icon")
                        )
                        com.example.ui.components.BuddyCharacter(
                            state = BuddyCharacterState.HAPPY,
                            character = buddy,
                            languageCode = languageCode,
                            showMoodPill = true,
                            modifier = Modifier
                                .size(116.dp)
                                .testTag("reward_overlay_buddy")
                        )
                    }

                    // Celebration Title & Optional Subtitle
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("reward_overlay_title")
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Bouncy Earned Stars Medallion Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("reward_overlay_stars_row")
                    ) {
                        val safeMaxStars = maxStars.coerceIn(1, 5)
                        for (i in 1..safeMaxStars) {
                            val earned = i <= earnedStars
                            val individualScale by animateFloatAsState(
                                targetValue = if (earned && bounceTriggered) 1f else 0.86f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "star_medallion_scale_$i"
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (earned) {
                                    WonderQuestDesignSystem.Colors.SunshineSoft
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = BorderStroke(
                                    width = if (earned) 2.5.dp else 1.dp,
                                    color = if (earned) {
                                        WonderQuestDesignSystem.Colors.StarGold
                                    } else {
                                         Color.Transparent
                                    }
                                ),
                                shadowElevation = if (earned) 6.dp else 0.dp,
                                modifier = Modifier
                                    .size(if (i == 2 && safeMaxStars == 3) 64.dp else 56.dp)
                                    .scale(individualScale * if (earned) starPulseScale else 1f)
                                    .testTag("reward_star_medallion_$i")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (earned) {
                                            WonderQuestDesignSystem.Colors.StarGold
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                        },
                                        modifier = Modifier.size(if (i == 2 && safeMaxStars == 3) 42.dp else 36.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quest Score & Stars Earned Summary Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reward_score_pill"),
                            shape = RoundedCornerShape(18.dp),
                            color = accentColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$correctAnswers/$totalQuestions",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentColor
                                )
                                Text(
                                    text = questionProgressLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reward_stars_pill"),
                            shape = RoundedCornerShape(18.dp),
                            color = WonderQuestDesignSystem.Colors.StarGold.copy(alpha = 0.16f),
                            border = BorderStroke(1.5.dp, WonderQuestDesignSystem.Colors.StarGold.copy(alpha = 0.45f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "+$earnedStars",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.testTag("reward_overlay_earned_stars_text")
                                )
                                Text(
                                    text = starsEarnedLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Newly Unlocked Badges / Stickers Showcase
                    if (unlockedBadgeIds.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = WonderQuestDesignSystem.Colors.PurpleSoft.copy(alpha = 0.55f),
                            border = BorderStroke(2.dp, WonderQuestDesignSystem.Colors.PlayfulPurple),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reward_unlocked_badges_row")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = WonderQuestDesignSystem.Colors.PurpleInteractive,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = newBadgesUnlockedLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontFamily = FredokaFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WonderQuestDesignSystem.Colors.PurpleInteractive
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    unlockedBadgeIds.forEach { badgeId ->
                                        val badge = BadgeCatalog.allBadges.find { it.id == badgeId }
                                        if (badge != null) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color(badge.colorHex).copy(alpha = 0.18f),
                                                border = BorderStroke(1.5.dp, Color(badge.colorHex)),
                                                modifier = Modifier.testTag("reward_badge_chip_${badge.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = badge.drawableRes),
                                                        contentDescription = badge.localizedTitle(languageCode),
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .rotate(badgeTiltDeg)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = badge.localizedTitle(languageCode),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontFamily = FredokaFontFamily,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Primary Action Button (Play Next Level / Continue)
                    Button(
                        onClick = {
                            SoundManager.playButtonClick(!isSoundMuted)
                            onPrimaryAction()
                        },
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("play_next_level_button")
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = primaryButtonText,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Secondary Action Button (Back to Island / Close)
                    OutlinedButton(
                        onClick = {
                            SoundManager.playButtonClick(!isSoundMuted)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(2.dp, accentColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("back_to_island_button")
                    ) {
                        Text(
                            text = secondaryButtonText,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}
