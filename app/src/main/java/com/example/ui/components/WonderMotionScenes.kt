package com.example.ui.components

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.VisualSceneKind
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rich multi-layered illustrated banner combining vector scene art (`img_wonder_island_scene`)
 * with real-time Compose motion graphics (drifting clouds, orbiting crystal sparkles, and aurora waves).
 */
@Composable
fun WonderIslandIllustratedBanner(
    modifier: Modifier = Modifier,
    overlayContent: @Composable () -> Unit = {}
) {
    val infinite = rememberInfiniteTransition(label = "island_scene_motion")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "island_phase"
    )
    val cloudDrift by infinite.animateFloat(
        initialValue = -24f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_drift"
    )

    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.img_wonder_island_scene),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Animated soft clouds
            drawRoundRect(
                color = Color.White.copy(alpha = 0.24f),
                topLeft = Offset(w * 0.12f + cloudDrift, h * 0.16f),
                size = Size(w * 0.22f, h * 0.13f),
                cornerRadius = CornerRadius(30f, 30f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.20f),
                topLeft = Offset(w * 0.62f - cloudDrift, h * 0.22f),
                size = Size(w * 0.24f, h * 0.12f),
                cornerRadius = CornerRadius(30f, 30f)
            )

            // Floating crystal fireflies & sparkles
            val sparkles = listOf(
                Triple(0.18f, 0.35f, Color(0xFFFFF176)),
                Triple(0.35f, 0.22f, Color(0xFF80DEEA)),
                Triple(0.52f, 0.30f, Color(0xFFFF80AB)),
                Triple(0.75f, 0.28f, Color(0xFFB9F6CA)),
                Triple(0.86f, 0.45f, Color(0xFFFFD54F))
            )
            sparkles.forEachIndexed { idx, (rx, ry, col) ->
                val dy = sin(phase + idx * 1.1f) * 10f
                val pulse = 4f + (cos(phase * 1.5f + idx) + 1f) * 2.5f
                val cx = w * rx
                val cy = h * ry + dy
                drawCircle(color = col.copy(alpha = 0.35f), radius = pulse * 2.1f, center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = pulse * 0.7f, center = Offset(cx, cy))
            }
        }

        overlayContent()
    }
}

/**
 * Illustrated Trophy Vault header combining `img_trophy_hall_scene` with animated golden sparkles.
 */
@Composable
fun TrophyVaultIllustratedBanner(
    modifier: Modifier = Modifier,
    overlayContent: @Composable () -> Unit = {}
) {
    val infinite = rememberInfiniteTransition(label = "trophy_vault_motion")
    val shimmer by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "trophy_shimmer"
    )

    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.img_trophy_hall_scene),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (i in 0..5) {
                val x = w * (0.12f + i * 0.15f)
                val y = h * (0.25f + (i % 2) * 0.22f) + sin(shimmer + i) * 8f
                val r = 4f + (cos(shimmer + i * 0.8f) + 1f) * 2f
                drawCircle(color = Color(0xFFFFF59D).copy(alpha = 0.45f), radius = r * 2f, center = Offset(x, y))
                drawCircle(color = Color.White, radius = r * 0.75f, center = Offset(x, y))
            }
        }
        overlayContent()
    }
}

/**
 * Stepping-stone milestone rail showing each question in the current mini-game level as an
 * island stone with stars for completed puzzles and a bouncy glow on the active puzzle.
 */
@Composable
fun QuestionStepMilestoneRail(
    totalQuestions: Int,
    currentQuestionIndex: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val safeTotal = totalQuestions.coerceIn(1, 10)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for (idx in 0 until safeTotal) {
            val isCompleted = idx < currentQuestionIndex
            val isCurrent = idx == currentQuestionIndex
            val scale by animateFloatAsState(
                targetValue = if (isCurrent) 1.16f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "milestone_stone_$idx"
            )

            val stoneBg = when {
                isCompleted -> Color(0xFFFFB300)
                isCurrent -> accent
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val stoneBorder = when {
                isCompleted -> Color(0xFFF59E0B)
                isCurrent -> Color.White.copy(alpha = 0.85f)
                else -> accent.copy(alpha = 0.25f)
            }

            Surface(
                shape = CircleShape,
                color = stoneBg,
                border = BorderStroke(2.dp, stoneBorder),
                shadowElevation = if (isCurrent || isCompleted) 4.dp else 0.dp,
                modifier = Modifier
                    .scale(scale)
                    .size(if (isCurrent) 34.dp else 28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        isCompleted -> {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        else -> {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (idx < safeTotal - 1) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isCompleted) Color(0xFFFFB300)
                            else accent.copy(alpha = 0.22f)
                        )
                )
            }
        }
    }
}

/**
 * Animated sparkle & confetti burst overlay rendered when a child answers correctly or finishes a level.
 */
@Composable
fun StarSparkleCelebrationOverlay(
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "celebration_sparkles")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val palette = listOf(
            Color(0xFFFFD95A),
            Color(0xFF55C7F7),
            Color(0xFFFF7F73),
            Color(0xFF72DDA8),
            Color(0xFFA98BFA)
        )
        val anchors = listOf(
            Offset(0.10f, 0.18f),
            Offset(0.24f, 0.78f),
            Offset(0.48f, 0.12f),
            Offset(0.76f, 0.20f),
            Offset(0.88f, 0.68f),
            Offset(0.15f, 0.52f),
            Offset(0.84f, 0.40f)
        )
        anchors.forEachIndexed { idx, pt ->
            val col = palette[idx % palette.size]
            val cx = w * pt.x + cos(phase + idx) * 8f
            val cy = h * pt.y + sin(phase * 1.3f + idx) * 8f
            val r = 5f + (sin(phase + idx * 0.9f) + 1f) * 3f
            drawCircle(color = col.copy(alpha = 0.35f), radius = r * 1.9f, center = Offset(cx, cy))
            drawCircle(color = col, radius = r, center = Offset(cx, cy))
            drawCircle(color = Color.White, radius = r * 0.45f, center = Offset(cx, cy))
        }
    }
}

/**
 * Renders an interactive, animated visual scene banner tailored to each of the 7 mini-games
 * so every puzzle feels like a lively game stage.
 */
@Composable
fun MiniGameStageSceneBanner(
    sceneKind: VisualSceneKind,
    accent: Color,
    isSolved: Boolean,
    tappedCount: Int,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "stage_scene_anim")
    val wave by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scene_wave"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (sceneKind) {
            VisualSceneKind.SAFARI_COUNT -> {
                // Rolling sunny safari hills + glowing counting fireflies
                val hillPath = Path().apply {
                    moveTo(0f, h)
                    quadraticTo(w * 0.28f, h * 0.38f, w * 0.58f, h * 0.78f)
                    quadraticTo(w * 0.82f, h * 0.45f, w, h * 0.72f)
                    lineTo(w, h)
                    close()
                }
                drawPath(hillPath, color = Color(0xFF72DDA8).copy(alpha = 0.28f))
                // Sun in corner
                drawCircle(
                    color = Color(0xFFFFD95A).copy(alpha = 0.65f),
                    radius = h * 0.26f,
                    center = Offset(w * 0.14f, h * 0.32f + sin(wave) * 4f)
                )
                // Bouncing safari orbs representing counted items
                val countOrbs = (tappedCount.coerceAtLeast(3)).coerceAtMost(8)
                for (i in 0 until countOrbs) {
                    val ox = w * (0.24f + i * (0.62f / countOrbs))
                    val oy = h * 0.52f + sin(wave + i * 0.8f) * 8f
                    drawCircle(
                        color = if (i < tappedCount) Color(0xFFFFB300) else accent.copy(alpha = 0.45f),
                        radius = if (i < tappedCount) 12f else 8f,
                        center = Offset(ox, oy)
                    )
                    drawCircle(color = Color.White, radius = 4f, center = Offset(ox - 2f, oy - 2f))
                }
            }

            VisualSceneKind.PATTERN_PORTAL -> {
                // Swirling magical portal rings
                for (ring in 3 downTo 1) {
                    val rad = minOf(w, h) * (0.14f * ring) + sin(wave + ring) * 3f
                    drawCircle(
                        color = accent.copy(alpha = 0.14f * (4 - ring)),
                        radius = rad,
                        center = Offset(cx, cy),
                        style = Stroke(width = 5f)
                    )
                }
                // Orbiting portal nodes
                for (i in 0 until 6) {
                    val angle = wave + (i * PI / 3.0).toFloat()
                    val px = cx + cos(angle) * (minOf(w, h) * 0.34f)
                    val py = cy + sin(angle) * (minOf(w, h) * 0.32f)
                    drawCircle(
                        color = if (i % 2 == 0) Color(0xFFFFD95A) else accent,
                        radius = 7f,
                        center = Offset(px, py)
                    )
                }
            }

            VisualSceneKind.NUMBER_BRIDGE -> {
                // River waves at bottom
                drawRoundRect(
                    color = Color(0xFF55C7F7).copy(alpha = 0.25f),
                    topLeft = Offset(w * 0.08f, h * 0.68f),
                    size = Size(w * 0.84f, h * 0.22f),
                    cornerRadius = CornerRadius(20f, 20f)
                )
                // Bridge arch cables
                drawLine(
                    color = accent.copy(alpha = 0.55f),
                    start = Offset(w * 0.12f, h * 0.48f),
                    end = Offset(w * 0.88f, h * 0.48f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                // 5 Bridge stepping planks (middle plank glows gold or green when solved)
                for (plank in 0 until 5) {
                    val px = w * (0.18f + plank * 0.15f)
                    val isMiddleMissing = plank == 2
                    val plankY = if (isMiddleMissing && !isSolved) h * 0.42f + sin(wave) * 6f else h * 0.46f
                    val plankColor = when {
                        isMiddleMissing && isSolved -> Color(0xFF10B981)
                        isMiddleMissing -> Color(0xFFFFB300)
                        else -> accent
                    }
                    drawRoundRect(
                        color = plankColor,
                        topLeft = Offset(px - 18f, plankY),
                        size = Size(36f, 16f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }

            VisualSceneKind.LOGIC_MATRIX -> {
                // Holographic detective matrix cells
                val cellW = minOf(w * 0.15f, 44f)
                val cellH = minOf(h * 0.36f, 32f)
                val startX = cx - cellW * 2.2f
                for (i in 0 until 4) {
                    val x = startX + i * (cellW * 1.15f)
                    val y = cy - cellH / 2f + sin(wave + i) * 3f
                    val isHighlight = (i == 1 && !isSolved) || isSolved
                    drawRoundRect(
                        color = if (isHighlight) Color(0xFFFFD95A).copy(alpha = 0.35f) else accent.copy(alpha = 0.16f),
                        topLeft = Offset(x, y),
                        size = Size(cellW, cellH),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = if (isHighlight) Color(0xFFF59E0B) else accent,
                        topLeft = Offset(x, y),
                        size = Size(cellW, cellH),
                        cornerRadius = CornerRadius(10f, 10f),
                        style = Stroke(width = 3f)
                    )
                }
            }

            VisualSceneKind.MEMORY_DECK -> {
                // Treasure chest lid + floating gem bubbles
                val chestW = minOf(w * 0.28f, 96f)
                val chestH = h * 0.48f
                drawRoundRect(
                    color = Color(0xFFD97706),
                    topLeft = Offset(cx - chestW / 2f, cy - chestH * 0.2f),
                    size = Size(chestW, chestH),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawRoundRect(
                    color = Color(0xFFFFD95A),
                    topLeft = Offset(cx - chestW / 2f, cy + chestH * 0.02f),
                    size = Size(chestW, 6f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                val gemColors = listOf(Color(0xFFFF7F73), Color(0xFF55C7F7), Color(0xFFFFD95A), Color(0xFFA98BFA))
                gemColors.forEachIndexed { idx, col ->
                    val gx = cx + (idx - 1.5f) * 36f
                    val gy = cy - chestH * 0.35f + sin(wave + idx) * 6f
                    drawCircle(color = col, radius = 9f, center = Offset(gx, gy))
                    drawCircle(color = Color.White, radius = 3.5f, center = Offset(gx - 2f, gy - 2f))
                }
            }

            else -> Unit
        }
    }
}

/**
 * Renders colorful glossy 3D-styled vector crystals/shapes (`SHAPE_*` and `ITEM_*`)
 * using custom Canvas illustration art instead of plain text or emojis.
 */
@Composable
fun ColorfulVisualTokenSymbol(
    token: String,
    accent: Color,
    symbolSize: Dp = 32.dp
) {
    val isIllustratedToken = token.startsWith("SHAPE_") || token.startsWith("ITEM_")
    if (!isIllustratedToken) {
        Text(
            text = token,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (token == "?") accent else MaterialTheme.colorScheme.onSurface
        )
        return
    }

    Box(modifier = Modifier.size(symbolSize), contentAlignment = Alignment.Center) {
        TokenIllustrationCanvas(
            tokenCode = token,
            modifier = Modifier.fillMaxSize()
        )
    }
}
