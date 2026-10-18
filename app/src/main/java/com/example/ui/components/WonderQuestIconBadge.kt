package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BuddyCharacter

@Composable
fun WonderQuestIcon(
    iconKey: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier.size(24.dp),
    contentDescription: String? = null
) {
    val vector: ImageVector = when (iconKey.lowercase()) {
        "explore" -> Icons.Default.Explore
        "bolt", "flash" -> Icons.Default.Bolt
        "spa", "relax" -> Icons.Default.Spa
        "trophy", "champion", "award" -> Icons.Default.EmojiEvents
        "early" -> Icons.Default.TouchApp
        "junior" -> Icons.Default.School
        "senior" -> Icons.Default.AutoAwesome
        "calculate", "math" -> Icons.Default.Calculate
        "extension", "puzzle" -> Icons.Default.Extension
        "alt_route", "route", "bridge" -> Icons.AutoMirrored.Filled.AltRoute
        "psychology", "logic", "brain" -> Icons.Default.Psychology
        "category", "shapes", "geometry" -> Icons.Default.Category
        "diamond", "gem", "treasure" -> Icons.Default.Diamond
        "balance", "scale" -> Icons.Default.Scale
        "star" -> Icons.Default.Star
        "military_tech" -> Icons.Default.MilitaryTech
        "workspace_premium" -> Icons.Default.WorkspacePremium
        "device_storage", "storage", "save" -> Icons.Default.Save
        "cloud_sync", "cloud" -> Icons.Default.CloudDone
        "heart", "favorite" -> Icons.Default.Favorite
        "timer", "clock" -> Icons.Default.HourglassTop
        "lock" -> Icons.Default.Lock
        "settings" -> Icons.Default.Settings
        "visible", "eye" -> Icons.Default.Visibility
        else -> Icons.AutoMirrored.Filled.Help
    }

    Icon(
        imageVector = vector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun BuddyMiniAvatar(
    character: BuddyCharacter,
    modifier: Modifier = Modifier.size(36.dp),
    isSelected: Boolean = false
) {
    val primaryColor = Color(character.primaryColorHex)
    val secondaryColor = Color(character.secondaryColorHex)

    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val center = Offset(sizePx / 2f, sizePx / 2f)
        val radius = sizePx / 2f

        // Outer glow/ring if selected
        if (isSelected) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.35f),
                radius = radius,
                center = center
            )
        }

        // Inner circle
        drawCircle(
            color = primaryColor,
            radius = radius * 0.82f,
            center = center
        )

        // Character stylized features
        when (character) {
            BuddyCharacter.KOMODO -> {
                // Crown ridges
                drawCircle(
                    color = secondaryColor,
                    radius = radius * 0.28f,
                    center = Offset(center.x, center.y - radius * 0.22f)
                )
                // Eyes
                drawCircle(Color.White, radius * 0.14f, Offset(center.x - radius * 0.26f, center.y))
                drawCircle(Color.Black, radius * 0.07f, Offset(center.x - radius * 0.26f, center.y))
                drawCircle(Color.White, radius * 0.14f, Offset(center.x + radius * 0.26f, center.y))
                drawCircle(Color.Black, radius * 0.07f, Offset(center.x + radius * 0.26f, center.y))
            }
            BuddyCharacter.SPARKY -> {
                // Pointed ears
                val leftEar = Path().apply {
                    moveTo(center.x - radius * 0.5f, center.y - radius * 0.2f)
                    lineTo(center.x - radius * 0.65f, center.y - radius * 0.75f)
                    lineTo(center.x - radius * 0.15f, center.y - radius * 0.45f)
                    close()
                }
                drawPath(leftEar, color = primaryColor)
                val rightEar = Path().apply {
                    moveTo(center.x + radius * 0.5f, center.y - radius * 0.2f)
                    lineTo(center.x + radius * 0.65f, center.y - radius * 0.75f)
                    lineTo(center.x + radius * 0.15f, center.y - radius * 0.45f)
                    close()
                }
                drawPath(rightEar, color = primaryColor)
                // Cheeks / muzzle
                drawCircle(secondaryColor, radius * 0.26f, Offset(center.x, center.y + radius * 0.18f))
                // Eyes
                drawCircle(Color.Black, radius * 0.08f, Offset(center.x - radius * 0.25f, center.y - radius * 0.08f))
                drawCircle(Color.Black, radius * 0.08f, Offset(center.x + radius * 0.25f, center.y - radius * 0.08f))
            }
            BuddyCharacter.LUNA -> {
                // Large owl spectacles/eyes
                drawCircle(secondaryColor, radius * 0.32f, Offset(center.x - radius * 0.25f, center.y))
                drawCircle(secondaryColor, radius * 0.32f, Offset(center.x + radius * 0.25f, center.y))
                drawCircle(Color.White, radius * 0.20f, Offset(center.x - radius * 0.25f, center.y))
                drawCircle(Color.White, radius * 0.20f, Offset(center.x + radius * 0.25f, center.y))
                drawCircle(Color.Black, radius * 0.10f, Offset(center.x - radius * 0.25f, center.y))
                drawCircle(Color.Black, radius * 0.10f, Offset(center.x + radius * 0.25f, center.y))
            }
            BuddyCharacter.ROBOBIT -> {
                // Antenna
                drawLine(
                    color = secondaryColor,
                    start = Offset(center.x, center.y - radius * 0.5f),
                    end = Offset(center.x, center.y - radius * 0.8f),
                    strokeWidth = radius * 0.10f
                )
                drawCircle(Color(0xFFFF5252), radius * 0.12f, Offset(center.x, center.y - radius * 0.8f))
                // Visor
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(center.x - radius * 0.45f, center.y - radius * 0.2f),
                    size = androidx.compose.ui.geometry.Size(radius * 0.9f, radius * 0.45f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.15f)
                )
                // Visor glow eyes
                drawCircle(Color(0xFF00E676), radius * 0.08f, Offset(center.x - radius * 0.22f, center.y))
                drawCircle(Color(0xFF00E676), radius * 0.08f, Offset(center.x + radius * 0.22f, center.y))
            }
            BuddyCharacter.SUNNY -> {
                // Lion mane rays
                for (i in 0 until 8) {
                    val angle = (i * 45) * (PI / 180.0)
                    val rx = center.x + (radius * 0.65f * cos(angle)).toFloat()
                    val ry = center.y + (radius * 0.65f * sin(angle)).toFloat()
                    drawCircle(secondaryColor, radius * 0.18f, Offset(rx, ry))
                }
                drawCircle(primaryColor, radius * 0.52f, center)
                drawCircle(Color.Black, radius * 0.07f, Offset(center.x - radius * 0.18f, center.y - radius * 0.05f))
                drawCircle(Color.Black, radius * 0.07f, Offset(center.x + radius * 0.18f, center.y - radius * 0.05f))
            }
        }
    }
}

@Composable
fun StarCountBadge(
    starsCount: Long,
    modifier: Modifier = Modifier,
    starSize: Dp = 18.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_badge_pulse")
    val starGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_glow"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Stars",
            tint = Color(0xFFFFB300),
            modifier = Modifier.size(starSize)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = starsCount.toString(),
            fontWeight = FontWeight.Black,
            fontSize = (starSize.value * 0.85f).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
