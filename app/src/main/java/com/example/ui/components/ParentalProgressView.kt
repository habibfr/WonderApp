package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BadgeCatalog
import com.example.data.ChildProfile
import com.example.data.GameSessionRecord
import com.example.data.LocalizedUiStrings
import com.example.data.MiniGameType
import com.example.ui.theme.WonderQuestDesignSystem

/**
 * Kid-friendly Parental Progress View displaying learning achievements, skill mastery
 * progress bars, completed levels breakdown across all 7 mini-games, and earned badges.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentalProgressView(
    profile: ChildProfile,
    recentSessions: List<GameSessionRecord>,
    strings: LocalizedUiStrings,
    modifier: Modifier = Modifier
) {
    val lang = profile.languageCode
    val totalBadges = BadgeCatalog.allBadges.size.coerceAtLeast(1)
    val unlockedCount = profile.unlockedBadges.distinct().size.coerceAtMost(totalBadges)

    // Friendly Explorer Rank title based on completed levels
    val explorerRankTitle = when {
        profile.completedLevelsCount >= 15 -> when (lang) {
            "en" -> "Grand Island Master"
            "es" -> "Gran Maestro de la Isla"
            "fr" -> "Grand Maître de l'Île"
            "hi" -> "ग्रैंड आइलैंड मास्टर"
            else -> "Master Agung Pulau"
        }
        profile.completedLevelsCount >= 5 -> when (lang) {
            "en" -> "Star Quest Champion"
            "es" -> "Campeón de Estrellas"
            "fr" -> "Champion des Étoiles"
            "hi" -> "स्टार चैंपियन"
            else -> "Juara Bintang Petualang"
        }
        else -> when (lang) {
            "en" -> "Rising Island Explorer"
            "es" -> "Explorador de la Isla"
            "fr" -> "Explorateur de l'Île"
            "hi" -> "द्वीप खोजकर्ता"
            else -> "Penjelajah Pulau Ceria"
        }
    }

    val progressSectionTitle = when (lang) {
        "en" -> "Learning Achievements & Progress Bars"
        "es" -> "Logros de Aprendizaje y Progreso"
        "fr" -> "Réussites d'Apprentissage et Progression"
        "hi" -> "सीखने की उपलब्धियाँ और प्रगति"
        else -> "Pencapaian Belajar & Progres Visual"
    }

    val badgesShowcaseTitle = when (lang) {
        "en" -> "Earned Badges & Medal Collection ($unlockedCount/$totalBadges)"
        "es" -> "Insignias Ganadas ($unlockedCount/$totalBadges)"
        "fr" -> "Badges Gagnés ($unlockedCount/$totalBadges)"
        "hi" -> "अर्जित बैज संग्रह ($unlockedCount/$totalBadges)"
        else -> "Koleksi Lencana & Medali ($unlockedCount/$totalBadges)"
    }

    val miniGamesBreakdownTitle = when (lang) {
        "en" -> "Completed Levels by Mini-Game"
        "es" -> "Niveles Completados por Juego"
        "fr" -> "Niveaux Terminés par Mini-Jeu"
        "hi" -> "मिनी-गेम के अनुसार पूर्ण स्तर"
        else -> "Progres Level per Permainan"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("parental_progress_view"),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.dp, WonderQuestDesignSystem.Colors.SkyBlue.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Banner with Explorer Rank Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = progressSectionTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${profile.childName} • $explorerRankTitle",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = WonderQuestDesignSystem.Colors.SkyBlueInteractive
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = WonderQuestDesignSystem.Colors.SunshineSoft,
                    border = BorderStroke(1.5.dp, WonderQuestDesignSystem.Colors.SunshineGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = WonderQuestDesignSystem.Colors.SunshineGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${profile.totalStars} ${strings.starsLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WonderQuestDesignSystem.Colors.OnSunshineSoft
                        )
                    }
                }
            }

            // 1. Core Learning Achievement Visual Progress Bars
            val mathGoal = ((profile.mathScore / 100L) + 1L) * 100L
            val logicGoal = ((profile.logicScore / 100L) + 1L) * 100L
            val levelGoal = ((profile.completedLevelsCount / 10L) + 1L) * 10L

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KidFriendlyAchievementBar(
                    title = strings.mathPointsLabel,
                    currentValueLabel = "${profile.mathScore} / $mathGoal pts",
                    progress = (profile.mathScore.toFloat() / mathGoal.toFloat()).coerceIn(0.08f, 1f),
                    icon = Icons.Default.Calculate,
                    startColor = Color(0xFFFF7F73),
                    endColor = Color(0xFFFFB300),
                    tag = "progress_bar_math_mastery"
                )

                KidFriendlyAchievementBar(
                    title = strings.logicPointsLabel,
                    currentValueLabel = "${profile.logicScore} / $logicGoal pts",
                    progress = (profile.logicScore.toFloat() / logicGoal.toFloat()).coerceIn(0.08f, 1f),
                    icon = Icons.Default.Psychology,
                    startColor = Color(0xFFA98BFA),
                    endColor = Color(0xFF55C7F7),
                    tag = "progress_bar_logic_mastery"
                )

                KidFriendlyAchievementBar(
                    title = strings.levelsLabel,
                    currentValueLabel = "${profile.completedLevelsCount} / $levelGoal",
                    progress = (profile.completedLevelsCount.toFloat() / levelGoal.toFloat()).coerceIn(0.08f, 1f),
                    icon = Icons.Default.EmojiEvents,
                    startColor = Color(0xFF10B981),
                    endColor = Color(0xFF55C7F7),
                    tag = "progress_bar_completed_levels"
                )

                KidFriendlyAchievementBar(
                    title = badgesShowcaseTitle,
                    currentValueLabel = "$unlockedCount / $totalBadges",
                    progress = (unlockedCount.toFloat() / totalBadges.toFloat()).coerceIn(0.08f, 1f),
                    icon = Icons.Default.Star,
                    startColor = Color(0xFFFFB300),
                    endColor = Color(0xFFFF7043),
                    tag = "progress_bar_badges_earned"
                )
            }

            // 2. Earned Badges & Medal Showcase
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("parental_badges_showcase")
            ) {
                Text(
                    text = badgesShowcaseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BadgeCatalog.allBadges.forEach { badge ->
                        val isEarned = badge.id in profile.unlockedBadges
                        val badgeColor = Color(badge.colorHex)

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isEarned) badgeColor.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = if (isEarned) 2.dp else 1.dp,
                                color = if (isEarned) badgeColor else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .alpha(if (isEarned) 1f else 0.65f)
                                .testTag("parental_badge_chip_${badge.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = badge.drawableRes),
                                    contentDescription = badge.localizedTitle(lang),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = badge.localizedTitle(lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = if (isEarned) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isEarned) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Per-Mini-Game Completed Levels Visual Progress Bars
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("parental_minigame_progress_list")
            ) {
                Text(
                    text = miniGamesBreakdownTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                MiniGameType.entries.forEach { gameType ->
                    val sessionsForGame = recentSessions.filter { it.gameType == gameType.id }
                    val completedForGame = sessionsForGame.size
                    val starsForGame = sessionsForGame.sumOf { it.starsEarned }
                    val accent = Color(gameType.accentColorHex)
                    val targetLevels = 5
                    val barFraction = (completedForGame.toFloat() / targetLevels.toFloat()).coerceIn(
                        if (completedForGame > 0) 0.2f else 0.06f,
                        1f
                    )
                    val animatedFraction by animateFloatAsState(
                        targetValue = barFraction,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                        label = "game_progress_${gameType.id}"
                    )

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = accent.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Image(
                                        painter = painterResource(id = gameType.drawableRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Text(
                                        text = gameType.localizedTitle(lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (starsForGame > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "$starsForGame",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${strings.levelPrefix} $completedForGame",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accent
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(accent.copy(alpha = 0.16f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedFraction)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(accent, accent.copy(alpha = 0.75f))
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KidFriendlyAchievementBar(
    title: String,
    currentValueLabel: String,
    progress: Float,
    icon: ImageVector,
    startColor: Color,
    endColor: Color,
    tag: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = tag
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = startColor.copy(alpha = 0.18f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = startColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = startColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = currentValueLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = startColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(startColor, endColor)
                        )
                    )
            )
        }
    }
}
