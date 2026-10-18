package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
 * Renders colorful glossy 3D-styled vector crystals/shapes instead of plain text symbols
 * when a mini-game token is a geometric token (`●`, `■`, `▲`, `★`, `◆`, `✦`, `⬠`, `⬡`, `▼`, `✚`).
 */
@Composable
fun ColorfulVisualTokenSymbol(
    token: String,
    accent: Color,
    symbolSize: Dp = 28.dp
) {
    val isShapeToken = token in setOf("●", "■", "▲", "★", "◆", "✦", "⬠", "⬡", "▼", "✚")
    if (!isShapeToken) {
        Text(
            text = token,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        return
    }

    Box(modifier = Modifier.size(symbolSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = minOf(size.width, size.height) * 0.44f
            val cx = size.width / 2f
            val cy = size.height / 2f
            when (token) {
                "●" -> {
                    drawCircle(color = Color(0xFFFFB300), radius = r, center = Offset(cx, cy))
                    drawCircle(color = Color.White.copy(alpha = 0.55f), radius = r * 0.32f, center = Offset(cx - r * 0.28f, cy - r * 0.28f))
                }
                "■" -> {
                    drawRoundRect(
                        color = Color(0xFF26A69A),
                        topLeft = Offset(cx - r, cy - r),
                        size = Size(r * 2f, r * 2f),
                        cornerRadius = CornerRadius(r * 0.28f, r * 0.28f)
                    )
                    drawCircle(color = Color.White.copy(alpha = 0.5f), radius = r * 0.24f, center = Offset(cx - r * 0.35f, cy - r * 0.35f))
                }
                "▲", "▼" -> {
                    val flip = if (token == "▼") -1f else 1f
                    val p = Path().apply {
                        moveTo(cx, cy - r * flip)
                        lineTo(cx + r, cy + r * 0.85f * flip)
                        lineTo(cx - r, cy + r * 0.85f * flip)
                        close()
                    }
                    drawPath(p, color = Color(0xFFFF7043))
                    drawCircle(color = Color.White.copy(alpha = 0.45f), radius = r * 0.22f, center = Offset(cx, cy))
                }
                "◆", "✦", "✚" -> {
                    val p = Path().apply {
                        moveTo(cx, cy - r)
                        lineTo(cx + r * 0.82f, cy)
                        lineTo(cx, cy + r)
                        lineTo(cx - r * 0.82f, cy)
                        close()
                    }
                    drawPath(p, color = Color(0xFFEC407A))
                    drawCircle(color = Color.White.copy(alpha = 0.55f), radius = r * 0.24f, center = Offset(cx, cy - r * 0.22f))
                }
                else -> {
                    // 5/6/8-pointed star or polygon gem
                    val points = if (token == "⬠") 5 else if (token == "⬡") 6 else 5
                    val p = Path()
                    for (i in 0 until points * 2) {
                        val rad = if (i % 2 == 0) r else if (token == "★") r * 0.45f else r * 0.85f
                        val a = (i * PI / points - PI / 2).toFloat()
                        val px = cx + cos(a) * rad
                        val py = cy + sin(a) * rad
                        if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
                    }
                    p.close()
                    drawPath(p, color = if (token == "★") Color(0xFFAB47BC) else accent)
                    drawCircle(color = Color(0xFFFFF59D), radius = r * 0.24f, center = Offset(cx, cy))
                }
            }
        }
    }
}
