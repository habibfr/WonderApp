package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BuddyCharacter as BuddyCharacterModel
import com.example.data.BuddyReaction
import com.example.ui.theme.WonderQuestDesignSystem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Core gameplay feedback states for the animated [BuddyCharacter] composable.
 */
enum class BuddyCharacterState {
    IDLE,
    HAPPY,
    THINKING;

    fun toBuddyReaction(): BuddyReaction = when (this) {
        IDLE -> BuddyReaction.IDLE
        HAPPY -> BuddyReaction.CELEBRATING
        THINKING -> BuddyReaction.THINKING
    }

    fun localizedMoodLabel(languageCode: String): String = when (this) {
        IDLE -> when (languageCode) {
            "en" -> "Ready!"
            "es" -> "¡Listo!"
            "fr" -> "Prêt !"
            "hi" -> "तैयार!"
            else -> "Siap!"
        }
        HAPPY -> when (languageCode) {
            "en" -> "Awesome!"
            "es" -> "¡Genial!"
            "fr" -> "Super !"
            "hi" -> "शानदार!"
            else -> "Hebat!"
        }
        THINKING -> when (languageCode) {
            "en" -> "Thinking..."
            "es" -> "Pensando..."
            "fr" -> "Voyons..."
            "hi" -> "सोच रहे हैं..."
            else -> "Hmm, coba pikir..."
        }
    }
}

/**
 * Maps any [BuddyReaction] to the 3 primary gameplay feedback states ([BuddyCharacterState.IDLE],
 * [BuddyCharacterState.HAPPY], or [BuddyCharacterState.THINKING]).
 */
fun BuddyReaction.toBuddyCharacterState(): BuddyCharacterState = when (this) {
    BuddyReaction.CELEBRATING,
    BuddyReaction.DANCING,
    BuddyReaction.HIGH_FIVE,
    BuddyReaction.SPARKLE,
    BuddyReaction.EATING_TREAT,
    BuddyReaction.MAGIC_WAND -> BuddyCharacterState.HAPPY
    BuddyReaction.THINKING -> BuddyCharacterState.THINKING
    BuddyReaction.IDLE,
    BuddyReaction.SLEEPY -> BuddyCharacterState.IDLE
}

/**
 * Composable component that displays an animated companion character with distinct
 * [BuddyCharacterState.IDLE], [BuddyCharacterState.HAPPY], and [BuddyCharacterState.THINKING]
 * animations, thought bubbles, celebratory sparkles, and mood badge to provide real-time
 * visual feedback to the child during gameplay.
 */
@Composable
fun BuddyCharacter(
    state: BuddyCharacterState = BuddyCharacterState.IDLE,
    character: BuddyCharacterModel = BuddyCharacterModel.SPARKY,
    reactionOverride: BuddyReaction? = null,
    languageCode: String = "id",
    showMoodPill: Boolean = true,
    onTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val effectiveReaction = reactionOverride ?: state.toBuddyReaction()
    val infinite = rememberInfiniteTransition(label = "buddy_character_state_motion")

    val floatPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "buddy_float_phase"
    )

    val containerScale by animateFloatAsState(
        targetValue = when (state) {
            BuddyCharacterState.HAPPY -> 1.08f
            BuddyCharacterState.THINKING -> 0.98f
            BuddyCharacterState.IDLE -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "buddy_state_scale"
    )

    val ringColor by animateColorAsState(
        targetValue = when (state) {
            BuddyCharacterState.HAPPY -> WonderQuestDesignSystem.Colors.MintInteractive
            BuddyCharacterState.THINKING -> WonderQuestDesignSystem.Colors.SunshineGold
            BuddyCharacterState.IDLE -> Color(character.primaryColorHex)
        },
        label = "buddy_ring_color"
    )

    val auraBackground by animateColorAsState(
        targetValue = when (state) {
            BuddyCharacterState.HAPPY -> WonderQuestDesignSystem.Colors.MintSoft.copy(alpha = 0.78f)
            BuddyCharacterState.THINKING -> WonderQuestDesignSystem.Colors.SunshineSoft.copy(alpha = 0.82f)
            BuddyCharacterState.IDLE -> MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        },
        label = "buddy_aura_bg"
    )

    val clickableModifier = if (onTap != null) {
        Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onTap() }
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .scale(containerScale)
            .then(clickableModifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .size(92.dp),
            contentAlignment = Alignment.Center
        ) {
            // Contextual State Backdrop Frame
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = auraBackground,
                border = BorderStroke(2.5.dp, ringColor.copy(alpha = 0.65f)),
                shadowElevation = if (state == BuddyCharacterState.HAPPY) 6.dp else 3.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // State-specific ambient particles (Thought bubbles for THINKING, Confetti bursts for HAPPY)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        when (state) {
                            BuddyCharacterState.THINKING -> {
                                // Rising animated thought bubbles on upper-right
                                val bubbleOffset = sin(floatPhase * 1.5f) * 3.5f
                                drawCircle(
                                    color = Color(0xFFF59E0B).copy(alpha = 0.45f),
                                    radius = w * 0.045f,
                                    center = Offset(w * 0.74f, h * 0.30f + bubbleOffset)
                                )
                                drawCircle(
                                    color = Color(0xFFF59E0B).copy(alpha = 0.65f),
                                    radius = w * 0.068f,
                                    center = Offset(w * 0.82f, h * 0.20f + bubbleOffset * 0.8f)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = w * 0.095f,
                                    center = Offset(w * 0.86f, h * 0.11f + bubbleOffset * 0.5f)
                                )
                                drawCircle(
                                    color = Color(0xFFF59E0B),
                                    radius = w * 0.032f,
                                    center = Offset(w * 0.86f, h * 0.11f + bubbleOffset * 0.5f)
                                )
                            }

                            BuddyCharacterState.HAPPY -> {
                                // Joyful confetti & radial glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFD95A).copy(alpha = 0.38f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = w * 0.48f,
                                    center = Offset(w / 2f, h / 2f)
                                )
                                val confettiColors = listOf(
                                    Color(0xFF10B981),
                                    Color(0xFFFFB300),
                                    Color(0xFF55C7F7),
                                    Color(0xFFFF7F73)
                                )
                                for (i in 0 until 4) {
                                    val angle = floatPhase + i * (PI / 2.0).toFloat()
                                    val px = w / 2f + cos(angle) * (w * 0.38f)
                                    val py = h / 2f + sin(angle) * (h * 0.38f)
                                    drawCircle(
                                        color = confettiColors[i],
                                        radius = w * 0.042f,
                                        center = Offset(px, py)
                                    )
                                }
                            }

                            BuddyCharacterState.IDLE -> {
                                // Calm breathing halo
                                val pulse = 0.36f + sin(floatPhase) * 0.03f
                                drawCircle(
                                    color = Color(character.primaryColorHex).copy(alpha = 0.14f),
                                    radius = w * pulse,
                                    center = Offset(w / 2f, h / 2f)
                                )
                            }
                        }
                    }

                    // Core Animated Character Illustration Canvas
                    AnimatedBuddyCanvas(
                        character = character,
                        reaction = effectiveReaction,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    )
                }
            }
        }

        if (showMoodPill) {
            AnimatedContent(
                targetState = state,
                label = "buddy_mood_pill_transition"
            ) { currentState ->
                Surface(
                    shape = CircleShape,
                    color = ringColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, ringColor.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("buddy_mood_pill_${currentState.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (currentState) {
                                BuddyCharacterState.HAPPY -> Icons.Default.Star
                                BuddyCharacterState.THINKING -> Icons.Default.Lightbulb
                                BuddyCharacterState.IDLE -> Icons.Default.AutoAwesome
                            },
                            contentDescription = null,
                            tint = ringColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = currentState.localizedMoodLabel(languageCode),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
