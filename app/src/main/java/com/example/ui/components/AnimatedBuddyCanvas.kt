package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.BuddyCharacter
import com.example.data.BuddyReaction
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedBuddyCanvas(
    character: BuddyCharacter,
    reaction: BuddyReaction,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "buddy_idle_transition")

    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breath_phase"
    )

    val sparkleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_rotation"
    )

    val bounceTarget = when (reaction) {
        BuddyReaction.CELEBRATING -> 1.16f
        BuddyReaction.DANCING -> 1.12f
        BuddyReaction.HIGH_FIVE -> 1.14f
        BuddyReaction.SPARKLE -> 1.10f
        BuddyReaction.EATING_TREAT -> 1.13f
        BuddyReaction.MAGIC_WAND -> 1.15f
        BuddyReaction.THINKING -> 0.96f
        BuddyReaction.SLEEPY -> 0.93f
        BuddyReaction.IDLE -> 1.0f
    }

    val reactionScale by animateFloatAsState(
        targetValue = bounceTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "reaction_scale"
    )

    val tiltTarget = when (reaction) {
        BuddyReaction.DANCING -> 14f
        BuddyReaction.THINKING -> -8f
        BuddyReaction.HIGH_FIVE -> 10f
        BuddyReaction.MAGIC_WAND -> -11f
        BuddyReaction.SLEEPY -> 6f
        else -> 0f
    }

    val reactionTilt by animateFloatAsState(
        targetValue = tiltTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "reaction_tilt"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minDim = minOf(w, h)
        val cx = w / 2f
        val cy = h / 2f

        val idleBob = sin(breathPhase) * (minDim * 0.03f)
        val danceWiggle = if (reaction == BuddyReaction.DANCING || reaction == BuddyReaction.CELEBRATING) {
            cos(breathPhase * 3f) * (minDim * 0.045f)
        } else 0f

        val primary = Color(character.primaryColorHex)
        val secondary = Color(character.secondaryColorHex)

        // Glowing Aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primary.copy(alpha = 0.36f),
                    secondary.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = minDim * 0.49f * reactionScale
            ),
            radius = minDim * 0.49f * reactionScale,
            center = Offset(cx, cy)
        )

        // Rainbow arc when MAGIC_WAND or CELEBRATING
        if (reaction == BuddyReaction.MAGIC_WAND || reaction == BuddyReaction.CELEBRATING) {
            drawRainbowHalo(cx, cy, minDim * 0.44f, breathPhase)
        }

        // Ground shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.14f),
            topLeft = Offset(cx - minDim * 0.25f, h * 0.84f),
            size = Size(minDim * 0.50f, minDim * 0.08f)
        )

        val bodyCenterY = cy + idleBob
        val bodyCenterX = cx + danceWiggle
        val bodyRadius = minDim * 0.27f * reactionScale

        rotate(degrees = reactionTilt * sin(breathPhase * 2f), pivot = Offset(bodyCenterX, bodyCenterY)) {
            when (character) {
                BuddyCharacter.SPARKY -> drawFoxEars(bodyCenterX, bodyCenterY, bodyRadius, primary, secondary)
                BuddyCharacter.LUNA -> drawOwlTuftsAndWings(bodyCenterX, bodyCenterY, bodyRadius, primary, secondary, breathPhase)
                BuddyCharacter.ROBOBIT -> drawRobotAntennaAndArms(bodyCenterX, bodyCenterY, bodyRadius, primary, secondary, breathPhase)
                BuddyCharacter.SUNNY -> drawLionMane(bodyCenterX, bodyCenterY, bodyRadius, primary, secondary, sparkleRotation)
                BuddyCharacter.KOMODO -> drawKomodoCrestAndTail(bodyCenterX, bodyCenterY, bodyRadius, primary, secondary, breathPhase)
            }

            // Main torso/head
            if (character == BuddyCharacter.ROBOBIT) {
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(secondary, primary)),
                    topLeft = Offset(bodyCenterX - bodyRadius, bodyCenterY - bodyRadius * 0.9f),
                    size = Size(bodyRadius * 2f, bodyRadius * 1.85f),
                    cornerRadius = CornerRadius(bodyRadius * 0.42f, bodyRadius * 0.42f)
                )
            } else {
                drawCircle(
                    brush = Brush.verticalGradient(listOf(secondary, primary)),
                    radius = bodyRadius,
                    center = Offset(bodyCenterX, bodyCenterY)
                )
            }

            // Belly patch
            drawOval(
                color = Color.White.copy(alpha = 0.88f),
                topLeft = Offset(bodyCenterX - bodyRadius * 0.56f, bodyCenterY - bodyRadius * 0.1f),
                size = Size(bodyRadius * 1.12f, bodyRadius * 0.92f)
            )

            // Eyes
            val eyeOffsetX = bodyRadius * 0.36f
            val eyeY = bodyCenterY - bodyRadius * 0.2f
            val eyeRadius = bodyRadius * 0.16f

            if (reaction == BuddyReaction.SLEEPY) {
                // Closed peaceful eyes
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(bodyCenterX - eyeOffsetX - eyeRadius, eyeY - eyeRadius * 0.5f),
                    size = Size(eyeRadius * 2f, eyeRadius),
                    style = Stroke(width = bodyRadius * 0.06f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(bodyCenterX + eyeOffsetX - eyeRadius, eyeY - eyeRadius * 0.5f),
                    size = Size(eyeRadius * 2f, eyeRadius),
                    style = Stroke(width = bodyRadius * 0.06f, cap = StrokeCap.Round)
                )
            } else {
                drawCircle(color = Color.White, radius = eyeRadius, center = Offset(bodyCenterX - eyeOffsetX, eyeY))
                drawCircle(color = Color.White, radius = eyeRadius, center = Offset(bodyCenterX + eyeOffsetX, eyeY))

                val pupilShiftY = if (reaction == BuddyReaction.THINKING) -eyeRadius * 0.25f else 0f
                val pupilRadius = eyeRadius * 0.58f
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = pupilRadius,
                    center = Offset(bodyCenterX - eyeOffsetX, eyeY + pupilShiftY)
                )
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = pupilRadius,
                    center = Offset(bodyCenterX + eyeOffsetX, eyeY + pupilShiftY)
                )

                // Eye catchlight
                drawCircle(
                    color = Color.White,
                    radius = pupilRadius * 0.35f,
                    center = Offset(bodyCenterX - eyeOffsetX - pupilRadius * 0.28f, eyeY - pupilRadius * 0.28f)
                )
                drawCircle(
                    color = Color.White,
                    radius = pupilRadius * 0.35f,
                    center = Offset(bodyCenterX + eyeOffsetX - pupilRadius * 0.28f, eyeY - pupilRadius * 0.28f)
                )
            }

            // Rosy cheeks
            drawCircle(
                color = Color(0xFFFF6F91).copy(alpha = 0.6f),
                radius = bodyRadius * 0.13f,
                center = Offset(bodyCenterX - eyeOffsetX * 1.35f, eyeY + bodyRadius * 0.22f)
            )
            drawCircle(
                color = Color(0xFFFF6F91).copy(alpha = 0.6f),
                radius = bodyRadius * 0.13f,
                center = Offset(bodyCenterX + eyeOffsetX * 1.35f, eyeY + bodyRadius * 0.22f)
            )

            // Mouth / Beak
            val mouthY = bodyCenterY + bodyRadius * 0.06f
            if (character == BuddyCharacter.LUNA) {
                val beak = Path().apply {
                    moveTo(bodyCenterX, mouthY + bodyRadius * 0.14f)
                    lineTo(bodyCenterX - bodyRadius * 0.12f, mouthY - bodyRadius * 0.04f)
                    lineTo(bodyCenterX + bodyRadius * 0.12f, mouthY - bodyRadius * 0.04f)
                    close()
                }
                drawPath(beak, color = Color(0xFFFFB300))
            } else if (reaction == BuddyReaction.EATING_TREAT) {
                // Happy open munching mouth
                val munchOpen = (0.14f + 0.06f * absSin(breathPhase * 4f)) * bodyRadius
                drawOval(
                    color = Color(0xFFD81B60),
                    topLeft = Offset(bodyCenterX - bodyRadius * 0.18f, mouthY - bodyRadius * 0.02f),
                    size = Size(bodyRadius * 0.36f, munchOpen)
                )
            } else {
                val smileSweep = if (reaction == BuddyReaction.THINKING) 90f else 155f
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 15f,
                    sweepAngle = smileSweep,
                    useCenter = false,
                    topLeft = Offset(bodyCenterX - bodyRadius * 0.22f, mouthY - bodyRadius * 0.08f),
                    size = Size(bodyRadius * 0.44f, bodyRadius * 0.28f),
                    style = Stroke(width = bodyRadius * 0.06f, cap = StrokeCap.Round)
                )
            }

            // Interactive Props based on Reaction
            if (reaction == BuddyReaction.HIGH_FIVE) {
                val pawX = bodyCenterX + bodyRadius * 0.95f
                val pawY = bodyCenterY - bodyRadius * 0.35f
                drawCircle(color = primary, radius = bodyRadius * 0.22f, center = Offset(pawX, pawY))
                drawCircle(color = Color.White, radius = bodyRadius * 0.11f, center = Offset(pawX, pawY))
            }

            if (reaction == BuddyReaction.EATING_TREAT) {
                drawFloatingBerryTreat(
                    cx = bodyCenterX + bodyRadius * 0.72f,
                    cy = bodyCenterY - bodyRadius * 0.1f + sin(breathPhase * 3f) * (bodyRadius * 0.15f),
                    radius = bodyRadius * 0.22f
                )
            }

            if (reaction == BuddyReaction.MAGIC_WAND) {
                drawMagicWandProp(
                    cx = bodyCenterX + bodyRadius * 0.85f,
                    cy = bodyCenterY - bodyRadius * 0.25f,
                    size = bodyRadius * 0.55f,
                    phase = breathPhase
                )
            }
        }

        // Orbiting stars & reaction particles
        val showBurst = reaction in listOf(
            BuddyReaction.CELEBRATING,
            BuddyReaction.SPARKLE,
            BuddyReaction.DANCING,
            BuddyReaction.HIGH_FIVE,
            BuddyReaction.MAGIC_WAND,
            BuddyReaction.EATING_TREAT
        )
        val starCount = if (showBurst) 8 else 4
        val orbitRadius = minDim * (if (showBurst) 0.43f else 0.39f)
        for (i in 0 until starCount) {
            val angleDeg = sparkleRotation + (i * (360f / starCount))
            val angleRad = (angleDeg * PI / 180.0).toFloat()
            val sx = cx + cos(angleRad) * orbitRadius
            val sy = cy + sin(angleRad) * orbitRadius
            val starSize = minDim * (if (showBurst) 0.055f else 0.036f)
            val starColor = when (i % 3) {
                0 -> Color(0xFFFFD54F)
                1 -> secondary
                else -> Color(0xFFFF80AB)
            }
            drawSparkleStar(
                center = Offset(sx, sy),
                radius = starSize,
                color = starColor
            )
        }
    }
}

@Composable
fun AnimatedIslandPanoramaCanvas(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "island_panorama")
    val wavePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    val cloudDrift by infinite.animateFloat(
        initialValue = -0.08f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_drift"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Sky backdrop
        val skyColors = if (isDarkMode) {
            listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81))
        } else {
            listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFC084FC))
        }
        drawRect(brush = Brush.verticalGradient(skyColors))

        // Sun or Moon
        val celestialCenter = Offset(w * 0.85f, h * 0.25f)
        drawCircle(
            color = if (isDarkMode) Color(0xFFFEF08A) else Color(0xFFFDE047),
            radius = minOf(w, h) * 0.16f,
            center = celestialCenter
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = minOf(w, h) * 0.23f,
            center = celestialCenter
        )

        // Rainbow Bridge across the island sky
        val rainbowColors = listOf(
            Color(0xFFFF5252),
            Color(0xFFFFB300),
            Color(0xFFFFEE58),
            Color(0xFF66BB6A),
            Color(0xFF42A5F5),
            Color(0xFFAB47BC)
        )
        rainbowColors.forEachIndexed { idx, col ->
            val inset = idx * 4.5f
            drawArc(
                color = col.copy(alpha = 0.65f),
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(w * 0.06f + inset, h * 0.15f + inset),
                size = Size(w * 0.68f - inset * 2f, h * 0.95f),
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )
        }

        // Drifting fluffy clouds
        drawCloud(Offset(w * (0.22f + cloudDrift), h * 0.26f), minOf(w, h) * 0.12f)
        drawCloud(Offset(w * (0.62f - cloudDrift), h * 0.18f), minOf(w, h) * 0.10f)

        // Lush Floating Tropical Hills
        drawOval(
            color = if (isDarkMode) Color(0xFF065F46) else Color(0xFF34D399),
            topLeft = Offset(-w * 0.1f, h * 0.62f),
            size = Size(w * 0.72f, h * 0.65f)
        )
        drawOval(
            color = if (isDarkMode) Color(0xFF047857) else Color(0xFF10B981),
            topLeft = Offset(w * 0.35f, h * 0.56f),
            size = Size(w * 0.78f, h * 0.70f)
        )

        // Sparkling Ocean Waves at bottom
        for (i in 0..5) {
            val wx = (w / 5f) * i + sin(wavePhase + i) * 14f
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.38f),
                radius = h * 0.14f,
                center = Offset(wx, h * 0.96f)
            )
        }
    }
}

private fun DrawScope.drawCloud(center: Offset, r: Float) {
    val c = Color.White.copy(alpha = 0.82f)
    drawCircle(c, radius = r, center = center)
    drawCircle(c, radius = r * 0.8f, center = Offset(center.x - r * 0.8f, center.y + r * 0.15f))
    drawCircle(c, radius = r * 0.85f, center = Offset(center.x + r * 0.8f, center.y + r * 0.12f))
}

private fun DrawScope.drawRainbowHalo(cx: Float, cy: Float, radius: Float, phase: Float) {
    val colors = listOf(Color(0xFFFF5252), Color(0xFFFFD54F), Color(0xFF69F0AE), Color(0xFF40C4FF))
    colors.forEachIndexed { i, c ->
        drawArc(
            color = c.copy(alpha = 0.6f),
            startAngle = 195f + sin(phase) * 8f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(cx - radius + i * 5f, cy - radius + i * 5f),
            size = Size((radius - i * 5f) * 2f, (radius - i * 5f) * 2f),
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawFloatingBerryTreat(cx: Float, cy: Float, radius: Float) {
    drawCircle(color = Color(0xFFE91E63), radius = radius, center = Offset(cx, cy))
    drawCircle(color = Color(0xFF4CAF50), radius = radius * 0.45f, center = Offset(cx, cy - radius * 0.85f))
    drawCircle(color = Color.White.copy(alpha = 0.7f), radius = radius * 0.2f, center = Offset(cx - radius * 0.3f, cy - radius * 0.2f))
}

private fun DrawScope.drawMagicWandProp(cx: Float, cy: Float, size: Float, phase: Float) {
    drawLine(
        color = Color(0xFF8D6E63),
        start = Offset(cx - size * 0.3f, cy + size * 0.5f),
        end = Offset(cx + size * 0.3f, cy - size * 0.3f),
        strokeWidth = size * 0.14f,
        cap = StrokeCap.Round
    )
    drawSparkleStar(
        center = Offset(cx + size * 0.35f, cy - size * 0.35f),
        radius = size * (0.35f + 0.08f * absSin(phase * 3f)),
        color = Color(0xFFFFD54F)
    )
}

private fun absSin(v: Float): Float = kotlin.math.abs(sin(v))

private fun DrawScope.drawFoxEars(cx: Float, cy: Float, r: Float, primary: Color, secondary: Color) {
    val leftEar = Path().apply {
        moveTo(cx - r * 0.75f, cy - r * 0.35f)
        lineTo(cx - r * 0.92f, cy - r * 1.28f)
        lineTo(cx - r * 0.18f, cy - r * 0.85f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(cx + r * 0.75f, cy - r * 0.35f)
        lineTo(cx + r * 0.92f, cy - r * 1.28f)
        lineTo(cx + r * 0.18f, cy - r * 0.85f)
        close()
    }
    drawPath(leftEar, color = primary)
    drawPath(rightEar, color = primary)
    drawCircle(color = secondary, radius = r * 0.16f, center = Offset(cx - r * 0.58f, cy - r * 0.82f))
    drawCircle(color = secondary, radius = r * 0.16f, center = Offset(cx + r * 0.58f, cy - r * 0.82f))
}

private fun DrawScope.drawOwlTuftsAndWings(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    phase: Float
) {
    val flap = sin(phase * 2f) * (r * 0.12f)
    drawOval(
        color = primary,
        topLeft = Offset(cx - r * 1.28f, cy - r * 0.25f + flap),
        size = Size(r * 0.5f, r * 0.85f)
    )
    drawOval(
        color = primary,
        topLeft = Offset(cx + r * 0.78f, cy - r * 0.25f - flap),
        size = Size(r * 0.5f, r * 0.85f)
    )
    drawCircle(color = secondary, radius = r * 0.22f, center = Offset(cx - r * 0.55f, cy - r * 0.92f))
    drawCircle(color = secondary, radius = r * 0.22f, center = Offset(cx + r * 0.55f, cy - r * 0.92f))
}

private fun DrawScope.drawRobotAntennaAndArms(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    phase: Float
) {
    drawLine(
        color = primary,
        start = Offset(cx, cy - r * 0.9f),
        end = Offset(cx, cy - r * 1.32f),
        strokeWidth = r * 0.1f,
        cap = StrokeCap.Round
    )
    val pulse = (1f + 0.2f * sin(phase * 3f))
    drawCircle(
        color = Color(0xFFFFD54F),
        radius = r * 0.16f * pulse,
        center = Offset(cx, cy - r * 1.36f)
    )
    drawRoundRect(
        color = secondary,
        topLeft = Offset(cx - r * 1.22f, cy - r * 0.2f),
        size = Size(r * 0.22f, r * 0.45f),
        cornerRadius = CornerRadius(r * 0.08f, r * 0.08f)
    )
    drawRoundRect(
        color = secondary,
        topLeft = Offset(cx + r * 1.0f, cy - r * 0.2f),
        size = Size(r * 0.22f, r * 0.45f),
        cornerRadius = CornerRadius(r * 0.08f, r * 0.08f)
    )
}

private fun DrawScope.drawLionMane(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    rotationDeg: Float
) {
    val petalCount = 12
    for (i in 0 until petalCount) {
        val angle = ((i * (360f / petalCount) + rotationDeg * 0.2f) * PI / 180.0).toFloat()
        val px = cx + cos(angle) * (r * 0.92f)
        val py = cy + sin(angle) * (r * 0.92f)
        drawCircle(
            color = if (i % 2 == 0) primary else secondary,
            radius = r * 0.28f,
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawKomodoCrestAndTail(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    phase: Float
) {
    // Swishing friendly dragon tail
    val tailWag = sin(phase * 2.5f) * (r * 0.18f)
    val tailPath = Path().apply {
        moveTo(cx + r * 0.65f, cy + r * 0.45f)
        quadraticTo(
            cx + r * 1.35f,
            cy + r * 0.35f + tailWag,
            cx + r * 1.25f,
            cy - r * 0.15f + tailWag
        )
        lineTo(cx + r * 0.85f, cy + r * 0.15f)
        close()
    }
    drawPath(tailPath, color = primary)

    // Golden Nusantara crown/scales crest on top of head
    for (i in -1..1) {
        val sx = cx + i * (r * 0.32f)
        val sy = cy - r * 0.98f + kotlin.math.abs(i) * (r * 0.08f)
        val scaleSpike = Path().apply {
            moveTo(sx, sy - r * 0.28f)
            lineTo(sx - r * 0.16f, sy + r * 0.12f)
            lineTo(sx + r * 0.16f, sy + r * 0.12f)
            close()
        }
        drawPath(scaleSpike, color = Color(0xFFFFCA28))
    }
    // Cute rounded dragon ears/horns
    drawCircle(color = secondary, radius = r * 0.18f, center = Offset(cx - r * 0.68f, cy - r * 0.72f))
    drawCircle(color = secondary, radius = r * 0.18f, center = Offset(cx + r * 0.68f, cy - r * 0.72f))
}

private fun DrawScope.drawSparkleStar(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path = path, color = color)
}
