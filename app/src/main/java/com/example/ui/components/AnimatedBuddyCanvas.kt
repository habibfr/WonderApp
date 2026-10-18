package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.BuddyCharacter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedBuddyCanvas(
    character: BuddyCharacter,
    interactionTriggerCount: Int,
    onBuddyTapped: () -> Unit,
    modifier: Modifier = Modifier,
    canvasSize: Dp = 170.dp,
    testTagName: String = "animated_buddy_canvas"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "buddy_idle")
    val idleBob by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_bob"
    )
    val idleWave by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_wave"
    )

    val jumpOffset = remember { Animatable(0f) }
    val burstProgress = remember { Animatable(0f) }

    LaunchedEffect(interactionTriggerCount) {
        if (interactionTriggerCount > 0) {
            burstProgress.snapTo(0f)
            jumpOffset.animateTo(
                targetValue = -28f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            burstProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(650, easing = FastOutSlowInEasing)
            )
            jumpOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val scaleBounce by animateFloatAsState(
        targetValue = if (jumpOffset.value < -8f) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale_bounce"
    )

    val primaryColor = Color(character.primaryColorHex)
    val accentColor = Color(character.accentColorHex)

    Box(
        modifier = modifier
            .size(canvasSize)
            .testTag(testTagName)
            .semantics { contentDescription = "Interactive character ${character.nameEn}" }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = canvasSize / 2),
                onClick = onBuddyTapped
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = (size.height / 2f) + idleBob + jumpOffset.value
            val r = size.minDimension * 0.28f

            // Glowing aura background circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.32f),
                        accentColor.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(cx, size.height / 2f),
                    radius = size.minDimension * 0.48f
                ),
                radius = size.minDimension * 0.48f,
                center = Offset(cx, size.height / 2f)
            )

            // Ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.14f),
                topLeft = Offset(cx - r * 0.85f, size.height * 0.84f),
                size = Size(r * 1.7f, r * 0.35f)
            )

            scale(scale = scaleBounce, pivot = Offset(cx, cy)) {
                when (character) {
                    BuddyCharacter.SPARKY -> drawSparkyFox(cx, cy, r, primaryColor, accentColor, idleWave)
                    BuddyCharacter.LUNA -> drawLunaOwl(cx, cy, r, primaryColor, accentColor, idleWave)
                    BuddyCharacter.ROBOBIT -> drawRoboBit(cx, cy, r, primaryColor, accentColor, idleWave)
                    BuddyCharacter.SUNNY -> drawSunnyStar(cx, cy, r, primaryColor, accentColor, idleWave)
                }
            }

            // Celebration particle burst when tapped or triggered
            if (burstProgress.value > 0f && burstProgress.value < 0.99f) {
                val p = burstProgress.value
                val burstColors = listOf(
                    Color(0xFFFBBF24),
                    Color(0xFFEC4899),
                    Color(0xFF10B981),
                    Color(0xFF0EA5E9),
                    Color(0xFF8B5CF6)
                )
                for (i in 0 until 10) {
                    val angle = (i * 36.0) * (PI / 180.0)
                    val dist = r * (1.0f + p * 0.95f)
                    val px = cx + (cos(angle) * dist).toFloat()
                    val py = cy + (sin(angle) * dist).toFloat()
                    val dotRadius = (1f - p) * (size.minDimension * 0.045f)
                    drawCircle(
                        color = burstColors[i % burstColors.size].copy(alpha = (1f - p)),
                        radius = dotRadius.coerceAtLeast(2f),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSparkyFox(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    accent: Color,
    waveAngle: Float
) {
    // Pointed Fox Ears
    val leftEar = Path().apply {
        moveTo(cx - r * 0.75f, cy - r * 0.3f)
        lineTo(cx - r * 0.95f, cy - r * 1.25f)
        lineTo(cx - r * 0.2f, cy - r * 0.75f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(cx + r * 0.75f, cy - r * 0.3f)
        lineTo(cx + r * 0.95f, cy - r * 1.25f)
        lineTo(cx + r * 0.2f, cy - r * 0.75f)
        close()
    }
    drawPath(leftEar, color = primary)
    drawPath(rightEar, color = primary)

    // Waving Paw
    rotate(degrees = waveAngle, pivot = Offset(cx + r * 0.8f, cy + r * 0.2f)) {
        drawRoundRect(
            color = primary,
            topLeft = Offset(cx + r * 0.7f, cy - r * 0.1f),
            size = Size(r * 0.55f, r * 0.28f),
            cornerRadius = CornerRadius(r * 0.14f)
        )
    }

    // Head/Body
    drawCircle(color = primary, radius = r, center = Offset(cx, cy))
    // White Cheeks
    drawOval(
        color = Color(0xFFFFF7ED),
        topLeft = Offset(cx - r * 0.78f, cy + r * 0.02f),
        size = Size(r * 1.56f, r * 0.82f)
    )
    // Star Goggles
    drawCircle(color = accent, radius = r * 0.32f, center = Offset(cx - r * 0.36f, cy - r * 0.18f), style = Stroke(width = r * 0.09f))
    drawCircle(color = accent, radius = r * 0.32f, center = Offset(cx + r * 0.36f, cy - r * 0.18f), style = Stroke(width = r * 0.09f))
    // Eyes
    drawCircle(color = Color(0xFF1E1B4B), radius = r * 0.13f, center = Offset(cx - r * 0.36f, cy - r * 0.18f))
    drawCircle(color = Color(0xFF1E1B4B), radius = r * 0.13f, center = Offset(cx + r * 0.36f, cy - r * 0.18f))
    drawCircle(color = Color.White, radius = r * 0.05f, center = Offset(cx - r * 0.4f, cy - r * 0.22f))
    drawCircle(color = Color.White, radius = r * 0.05f, center = Offset(cx + r * 0.32f, cy - r * 0.22f))
    // Cute Nose & Smile
    drawCircle(color = Color(0xFF1E1B4B), radius = r * 0.1f, center = Offset(cx, cy + r * 0.12f))
    val smile = Path().apply {
        moveTo(cx - r * 0.25f, cy + r * 0.3f)
        quadraticTo(cx, cy + r * 0.52f, cx + r * 0.25f, cy + r * 0.3f)
    }
    drawPath(smile, color = Color(0xFF1E1B4B), style = Stroke(width = r * 0.08f, cap = StrokeCap.Round))
}

private fun DrawScope.drawLunaOwl(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    accent: Color,
    waveAngle: Float
) {
    // Flapping Wings
    rotate(degrees = -waveAngle, pivot = Offset(cx - r * 0.85f, cy)) {
        drawOval(
            color = primary.copy(alpha = 0.9f),
            topLeft = Offset(cx - r * 1.3f, cy - r * 0.3f),
            size = Size(r * 0.55f, r * 0.9f)
        )
    }
    rotate(degrees = waveAngle, pivot = Offset(cx + r * 0.85f, cy)) {
        drawOval(
            color = primary.copy(alpha = 0.9f),
            topLeft = Offset(cx + r * 0.75f, cy - r * 0.3f),
            size = Size(r * 0.55f, r * 0.9f)
        )
    }
    // Owl Body
    drawCircle(color = primary, radius = r, center = Offset(cx, cy))
    // Soft Belly
    drawOval(
        color = Color(0xFFEDE9FE),
        topLeft = Offset(cx - r * 0.62f, cy + r * 0.1f),
        size = Size(r * 1.24f, r * 0.78f)
    )
    // Big Wise Owl Eyes
    drawCircle(color = Color.White, radius = r * 0.34f, center = Offset(cx - r * 0.36f, cy - r * 0.18f))
    drawCircle(color = Color.White, radius = r * 0.34f, center = Offset(cx + r * 0.36f, cy - r * 0.18f))
    drawCircle(color = Color(0xFF1E1B4B), radius = r * 0.17f, center = Offset(cx - r * 0.36f, cy - r * 0.18f))
    drawCircle(color = Color(0xFF1E1B4B), radius = r * 0.17f, center = Offset(cx + r * 0.36f, cy - r * 0.18f))
    drawCircle(color = accent, radius = r * 0.06f, center = Offset(cx - r * 0.41f, cy - r * 0.23f))
    drawCircle(color = accent, radius = r * 0.06f, center = Offset(cx + r * 0.31f, cy - r * 0.23f))
    // Golden Beak
    val beak = Path().apply {
        moveTo(cx - r * 0.14f, cy - r * 0.05f)
        lineTo(cx + r * 0.14f, cy - r * 0.05f)
        lineTo(cx, cy + r * 0.22f)
        close()
    }
    drawPath(beak, color = Color(0xFFF59E0B))
}

private fun DrawScope.drawRoboBit(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    accent: Color,
    waveAngle: Float
) {
    // Antenna
    drawLine(
        color = accent,
        start = Offset(cx, cy - r),
        end = Offset(cx, cy - r * 1.38f),
        strokeWidth = r * 0.1f,
        cap = StrokeCap.Round
    )
    drawCircle(color = accent, radius = r * 0.16f, center = Offset(cx, cy - r * 1.42f))

    // Robot Head/Body
    drawRoundRect(
        color = primary,
        topLeft = Offset(cx - r * 0.95f, cy - r * 0.85f),
        size = Size(r * 1.9f, r * 1.75f),
        cornerRadius = CornerRadius(r * 0.38f)
    )
    // Glowing Visor Screen
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - r * 0.72f, cy - r * 0.55f),
        size = Size(r * 1.44f, r * 0.75f),
        cornerRadius = CornerRadius(r * 0.22f)
    )
    // Digital Happy Eyes
    drawCircle(color = accent, radius = r * 0.16f, center = Offset(cx - r * 0.35f, cy - r * 0.18f))
    drawCircle(color = accent, radius = r * 0.16f, center = Offset(cx + r * 0.35f, cy - r * 0.18f))
    // Smile bar
    drawRoundRect(
        color = Color(0xFF34D399),
        topLeft = Offset(cx - r * 0.42f, cy + r * 0.38f),
        size = Size(r * 0.84f, r * 0.2f),
        cornerRadius = CornerRadius(r * 0.1f)
    )
    // Waving Robot Arm
    rotate(degrees = waveAngle, pivot = Offset(cx + r * 0.95f, cy)) {
        drawRoundRect(
            color = accent,
            topLeft = Offset(cx + r * 0.9f, cy - r * 0.15f),
            size = Size(r * 0.45f, r * 0.25f),
            cornerRadius = CornerRadius(r * 0.1f)
        )
    }
}

private fun DrawScope.drawSunnyStar(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    accent: Color,
    waveAngle: Float
) {
    rotate(degrees = waveAngle * 0.4f, pivot = Offset(cx, cy)) {
        val starPath = Path()
        val points = 5
        val outerR = r * 1.2f
        val innerR = r * 0.58f
        for (i in 0 until points * 2) {
            val rad = if (i % 2 == 0) outerR else innerR
            val angle = (i * PI / points) - (PI / 2.0)
            val x = cx + (cos(angle) * rad).toFloat()
            val y = cy + (sin(angle) * rad).toFloat()
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()
        drawPath(starPath, color = primary)
    }
    // Rosy cheeks
    drawCircle(color = accent.copy(alpha = 0.55f), radius = r * 0.16f, center = Offset(cx - r * 0.42f, cy + r * 0.1f))
    drawCircle(color = accent.copy(alpha = 0.55f), radius = r * 0.16f, center = Offset(cx + r * 0.42f, cy + r * 0.1f))
    // Eyes
    drawCircle(color = Color(0xFF1E1B4B), radius = r * 0.13f, center = Offset(cx - r * 0.28f, cy - r * 0.12f))
    drawCircle(color = Color(0xFF1E1B4B), radius = r * 0.13f, center = Offset(cx + r * 0.28f, cy - r * 0.12f))
    // Smile
    val smile = Path().apply {
        moveTo(cx - r * 0.22f, cy + r * 0.15f)
        quadraticTo(cx, cy + r * 0.38f, cx + r * 0.22f, cy + r * 0.15f)
    }
    drawPath(smile, color = Color(0xFF1E1B4B), style = Stroke(width = r * 0.08f, cap = StrokeCap.Round))
}
