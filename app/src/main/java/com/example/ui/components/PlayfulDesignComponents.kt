package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object PlayfulSoundEffects {
    fun playTap(enabled: Boolean = true) {
        com.example.util.SoundManager.playButtonClick(enabled)
    }

    fun playSuccess(enabled: Boolean = true) {
        com.example.util.SoundManager.playCorrectAnswer(enabled)
    }

    fun playGentleRetry(enabled: Boolean = true) {
        com.example.util.SoundManager.playGentleRetry(enabled)
    }

    fun playLevelComplete(enabled: Boolean = true) {
        com.example.util.SoundManager.playLevelComplete(enabled)
    }

    fun playReward(enabled: Boolean = true) {
        com.example.util.SoundManager.playRewardTrigger(enabled)
    }
}

@Composable
fun PlayfulBouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncy_btn_scale"
    )

    val lipColor = if (enabled) {
        Color(
            red = (containerColor.red * 0.76f).coerceIn(0f, 1f),
            green = (containerColor.green * 0.76f).coerceIn(0f, 1f),
            blue = (containerColor.blue * 0.76f).coerceIn(0f, 1f),
            alpha = 1f
        )
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = 48.dp, minHeight = 52.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(lipColor)
            .padding(bottom = if (isPressed) 1.dp else 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (enabled) containerColor else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
fun PlayfulInteractiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncy_card_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 2.dp else 5.dp)
    ) {
        content()
    }
}

@Composable
fun VisualTokenTile(
    token: String,
    modifier: Modifier = Modifier,
    tileSize: Dp = 54.dp
) {
    val isPureIllustration = token.startsWith("ITEM_") || token.startsWith("SHAPE_")
    val isOperator = token in setOf("+", "-", "×", "=", "?")

    val bgColor = when {
        token == "?" -> Color(0xFFFFD95A).copy(alpha = 0.32f)
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isPureIllustration -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        token == "?" -> Color(0xFFF59E0B)
        isOperator -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else -> Color(0xFF55C7F7).copy(alpha = 0.35f)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        tonalElevation = 2.dp
    ) {
        if (isPureIllustration) {
            Box(
                modifier = Modifier
                    .size(tileSize)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                TokenIllustrationCanvas(tokenCode = token, modifier = Modifier.fillMaxSize())
            }
        } else {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = tileSize, minHeight = tileSize)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = token,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (token == "?") Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun TokenIllustrationCanvas(
    tokenCode: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.42f

        when (tokenCode) {
            "SHAPE_RED_CIRCLE", "ITEM_APPLE" -> {
                drawCircle(color = Color(0xFFFF7F73), radius = r, center = Offset(cx, cy + r * 0.08f))
                // Little green leaf on top for apple/berry feel
                drawOval(
                    color = Color(0xFF72DDA8),
                    topLeft = Offset(cx, cy - r * 1.05f),
                    size = Size(r * 0.55f, r * 0.35f)
                )
            }
            "SHAPE_BLUE_SQUARE" -> {
                drawRoundRect(
                    color = Color(0xFF55C7F7),
                    topLeft = Offset(cx - r * 0.85f, cy - r * 0.85f),
                    size = Size(r * 1.7f, r * 1.7f),
                    cornerRadius = CornerRadius(r * 0.3f)
                )
            }
            "SHAPE_GOLD_STAR", "ITEM_STAR" -> {
                drawFivePointStar(cx, cy, r * 1.05f, Color(0xFFFFD95A))
            }
            "SHAPE_GREEN_TRIANGLE", "ITEM_TREE" -> {
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r, cy + r * 0.85f)
                    lineTo(cx - r, cy + r * 0.85f)
                    close()
                }
                drawPath(path, color = Color(0xFF72DDA8))
            }
            "SHAPE_PURPLE_DIAMOND", "ITEM_CRYSTAL" -> {
                val path = Path().apply {
                    moveTo(cx, cy - r * 1.05f)
                    lineTo(cx + r * 0.85f, cy)
                    lineTo(cx, cy + r * 1.05f)
                    lineTo(cx - r * 0.85f, cy)
                    close()
                }
                drawPath(path, color = Color(0xFFA98BFA))
            }
            "ITEM_BUTTERFLY" -> {
                drawCircle(color = Color(0xFFA98BFA), radius = r * 0.55f, center = Offset(cx - r * 0.45f, cy - r * 0.25f))
                drawCircle(color = Color(0xFFA98BFA), radius = r * 0.55f, center = Offset(cx + r * 0.45f, cy - r * 0.25f))
                drawCircle(color = Color(0xFF55C7F7), radius = r * 0.42f, center = Offset(cx - r * 0.4f, cy + r * 0.35f))
                drawCircle(color = Color(0xFF55C7F7), radius = r * 0.42f, center = Offset(cx + r * 0.4f, cy + r * 0.35f))
                drawRoundRect(
                    color = Color(0xFF30334A),
                    topLeft = Offset(cx - r * 0.12f, cy - r * 0.7f),
                    size = Size(r * 0.24f, r * 1.4f),
                    cornerRadius = CornerRadius(r * 0.12f)
                )
            }
            "ITEM_CAR" -> {
                drawRoundRect(
                    color = Color(0xFF55C7F7),
                    topLeft = Offset(cx - r * 0.95f, cy - r * 0.15f),
                    size = Size(r * 1.9f, r * 0.75f),
                    cornerRadius = CornerRadius(r * 0.25f)
                )
                drawRoundRect(
                    color = Color(0xFFFFD95A),
                    topLeft = Offset(cx - r * 0.55f, cy - r * 0.65f),
                    size = Size(r * 1.1f, r * 0.6f),
                    cornerRadius = CornerRadius(r * 0.2f)
                )
                drawCircle(color = Color(0xFF30334A), radius = r * 0.28f, center = Offset(cx - r * 0.5f, cy + r * 0.6f))
                drawCircle(color = Color(0xFF30334A), radius = r * 0.28f, center = Offset(cx + r * 0.5f, cy + r * 0.6f))
            }
            "ITEM_SUN" -> {
                drawCircle(color = Color(0xFFFFD95A), radius = r * 0.72f, center = Offset(cx, cy))
                for (i in 0 until 8) {
                    val angle = i * (PI / 4.0)
                    val sx = cx + (cos(angle) * r * 0.82f).toFloat()
                    val sy = cy + (sin(angle) * r * 0.82f).toFloat()
                    val ex = cx + (cos(angle) * r * 1.1f).toFloat()
                    val ey = cy + (sin(angle) * r * 1.1f).toFloat()
                    drawLine(
                        color = Color(0xFFF59E0B),
                        start = Offset(sx, sy),
                        end = Offset(ex, ey),
                        strokeWidth = r * 0.16f,
                        cap = StrokeCap.Round
                    )
                }
            }
            "ITEM_MOON" -> {
                drawCircle(color = Color(0xFFA98BFA), radius = r * 0.85f, center = Offset(cx, cy))
                drawCircle(color = Color(0xFFFFF9EE), radius = r * 0.65f, center = Offset(cx + r * 0.32f, cy - r * 0.2f))
            }
            else -> {
                drawCircle(color = Color(0xFF55C7F7), radius = r * 0.8f, center = Offset(cx, cy))
            }
        }
    }
}

private fun DrawScope.drawFivePointStar(cx: Float, cy: Float, outerR: Float, color: Color) {
    val path = Path()
    val innerR = outerR * 0.45f
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) outerR else innerR
        val angle = (i * PI / 5.0) - (PI / 2.0)
        val x = cx + (cos(angle) * rad).toFloat()
        val y = cy + (sin(angle) * rad).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
    drawPath(path, color = Color(0xFFF59E0B), style = Stroke(width = outerR * 0.08f))
}
