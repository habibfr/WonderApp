package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AgeGroupConfig
import com.example.data.GamePlayMode
import com.example.data.MiniGameType
import com.example.domain.MiniGameGenerator
import com.example.domain.MiniGameQuestion
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.WonderQuestDesignSystem
import com.example.util.SoundManager
import kotlin.random.Random

/**
 * Collectible stickers awarded for completing each of the 3 Daily Challenges.
 */
enum class DailyChallengeSticker(
    val id: String,
    val badgeId: String,
    val drawableRes: Int,
    val colorHex: Long,
    val titleId: String,
    val titleEn: String,
    val titleEs: String,
    val titleFr: String,
    val titleHi: String
) {
    SUNSHINE_STAR(
        id = "sticker_sunshine_star",
        badgeId = "star_collector",
        drawableRes = R.drawable.ic_badge_star_medal,
        colorHex = 0xFFF59E0B,
        titleId = "Stiker Bintang Surya",
        titleEn = "Sunshine Star Sticker",
        titleEs = "Pegatina Estrella Solar",
        titleFr = "Autocollant Étoile Solaire",
        titleHi = "सनशाइन स्टार स्टिकर"
    ),
    ROCKET_EXPLORER(
        id = "sticker_rocket_explorer",
        badgeId = "first_quest",
        drawableRes = R.drawable.ic_badge_rocket_quest,
        colorHex = 0xFF10B981,
        titleId = "Stiker Roket Penjelajah",
        titleEn = "Rocket Explorer Sticker",
        titleEs = "Pegatina Cohete Explorador",
        titleFr = "Autocollant Fusée",
        titleHi = "रॉकेट एक्सप्लोरर स्टिकर"
    ),
    CROWN_CHAMPION(
        id = "sticker_crown_champion",
        badgeId = "super_streak",
        drawableRes = R.drawable.ic_badge_crown_ace,
        colorHex = 0xFF7C3AED,
        titleId = "Stiker Mahkota Logika",
        titleEn = "Logic Crown Sticker",
        titleEs = "Pegatina Corona Lógica",
        titleFr = "Autocollant Couronne",
        titleHi = "लॉजिक क्राउन स्टिकर"
    );

    fun localizedTitle(languageCode: String): String = when (languageCode) {
        "en" -> titleEn
        "es" -> titleEs
        "fr" -> titleFr
        "hi" -> titleHi
        else -> titleId
    }
}

/**
 * Represents one of the three small, randomized daily math & logic tasks.
 */
data class DailyChallengeTask(
    val id: String,
    val slotIndex: Int,
    val gameType: MiniGameType,
    val question: MiniGameQuestion,
    val rewardStars: Int,
    val rewardSticker: DailyChallengeSticker,
    val selectedOptionIndex: Int? = null,
    val isCompleted: Boolean = false,
    val showHint: Boolean = false
)

/**
 * Generates three small, randomized math & logic tasks tailored to the child's age group and language.
 * - Task 1: Number & Counting Math Task (Math Safari, Number Bridge, or Cosmic Balance)
 * - Task 2: Pattern & Sequence Logic Task (Pattern Portal or Logic Matrix)
 * - Task 3: Geometry & Visual Memory Task (Shape Galaxy or Memory Treasure)
 */
fun fetchDailyChallengeTasks(
    ageGroup: AgeGroupConfig = AgeGroupConfig.JUNIOR,
    languageCode: String = "id",
    seed: Int = 1
): List<DailyChallengeTask> {
    val rng = Random(seed * 9973 + ageGroup.code.hashCode())

    val mathTypes = listOf(MiniGameType.MATH_SAFARI, MiniGameType.NUMBER_BRIDGE, MiniGameType.COSMIC_BALANCE)
    val logicTypes = listOf(MiniGameType.PATTERN_PORTAL, MiniGameType.LOGIC_MATRIX)
    val spatialTypes = listOf(MiniGameType.SHAPE_GALAXY, MiniGameType.MEMORY_TREASURE)

    val pickedTypes = listOf(
        mathTypes[rng.nextInt(mathTypes.size)],
        logicTypes[rng.nextInt(logicTypes.size)],
        spatialTypes[rng.nextInt(spatialTypes.size)]
    )

    val stickers = DailyChallengeSticker.entries
    val starRewards = listOf(2, 3, 4)

    return pickedTypes.mapIndexed { index, gameType ->
        val randomLevel = rng.nextInt(1, 10)
        val generatedQuestions = MiniGameGenerator.generateQuestions(
            gameType = gameType,
            ageGroup = ageGroup,
            levelNumber = randomLevel,
            languageCode = languageCode,
            playMode = GamePlayMode.DAILY_QUEST
        )
        val pickedQuestion = generatedQuestions[rng.nextInt(generatedQuestions.size)]
        DailyChallengeTask(
            id = "daily_challenge_${seed}_$index",
            slotIndex = index,
            gameType = gameType,
            question = pickedQuestion,
            rewardStars = starRewards[index % starRewards.size],
            rewardSticker = stickers[index % stickers.size]
        )
    }
}

/**
 * Interactive 'DailyChallenges' Composable that fetches and displays three small, randomized
 * math and logic tasks for the child to complete, rewarding them with bonus stars and collectible
 * stickers upon completion.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyChallenges(
    modifier: Modifier = Modifier,
    ageGroup: AgeGroupConfig = AgeGroupConfig.JUNIOR,
    languageCode: String = "id",
    isSoundMuted: Boolean = false,
    onRewardEarned: (bonusStars: Int, unlockedStickerBadgeId: String, isMathTask: Boolean) -> Unit = { _, _, _ -> },
    onAllThreeCompletedBonus: (grandBonusStars: Int, grandBadgeId: String) -> Unit = { _, _ -> }
) {
    var seedCounter by remember { mutableIntStateOf(1) }
    var tasks by remember(ageGroup, languageCode, seedCounter) {
        mutableStateOf(
            fetchDailyChallengeTasks(
                ageGroup = ageGroup,
                languageCode = languageCode,
                seed = seedCounter
            )
        )
    }
    var grandBonusClaimed by remember(seedCounter) { mutableStateOf(false) }

    val completedCount = tasks.count { it.isCompleted }
    val earnedStarsToday = tasks.filter { it.isCompleted }.sumOf { it.rewardStars } +
        if (grandBonusClaimed) 5 else 0

    // Automatically trigger grand bonus celebration when all 3 tasks are solved
    LaunchedEffect(completedCount) {
        if (completedCount == 3 && !grandBonusClaimed) {
            grandBonusClaimed = true
            SoundManager.playLevelComplete(!isSoundMuted)
            onAllThreeCompletedBonus(5, "island_champion")
        }
    }

    val headerTitle = when (languageCode) {
        "en" -> "3 Daily Math & Logic Challenges"
        "es" -> "3 Retos Diarios de Mate y Lógica"
        "fr" -> "3 Défis Quotidiens Maths & Logique"
        "hi" -> "3 दैनिक गणित और तर्क चुनौतियाँ"
        else -> "3 Tantangan Harian Matematika & Logika"
    }
    val headerSubtitle = when (languageCode) {
        "en" -> "Complete 3 quick puzzles to earn bonus stars and collectible stickers!"
        "es" -> "¡Completa 3 acertijos rápidos para ganar estrellas extra y pegatinas!"
        "fr" -> "Complète 3 petits défis pour gagner des étoiles bonus et des autocollants !"
        "hi" -> "बोनस सितारे और स्टिकर जीतने के लिए 3 छोटी पहेलियाँ पूरी करें!"
        else -> "Selesaikan 3 teka-teki singkat untuk meraih bintang bonus & stiker koleksi!"
    }
    val shuffleLabel = when (languageCode) {
        "en" -> "New Tasks"
        "es" -> "Nuevos Retos"
        "fr" -> "Nouveaux Défis"
        "hi" -> "नई पहेलियाँ"
        else -> "Soal Baru"
    }
    val stickerTrayLabel = when (languageCode) {
        "en" -> "Today's Sticker & Star Rewards"
        "es" -> "Premios de Pegatinas y Estrellas"
        "fr" -> "Autocollants et Étoiles du Jour"
        "hi" -> "आज के स्टिकर और स्टार पुरस्कार"
        else -> "Hadiah Stiker & Bintang Hari Ini"
    }
    val grandBonusTitle = when (languageCode) {
        "en" -> "All 3 Daily Challenges Complete! +5 Grand Bonus Stars & Trophy Sticker Unlocked!"
        "es" -> "¡3 Retos Completados! +5 Estrellas Extra y Trofeo Desbloqueado!"
        "fr" -> "Les 3 Défis Terminés ! +5 Étoiles Bonus et Trophée Débloqué !"
        "hi" -> "तीनों दैनिक चुनौतियाँ पूरी हुईं! +5 बोनस सितारे और ट्रॉफी अनलॉक!"
        else -> "Ketiga Tantangan Harian Selesai! +5 Bintang Bonus Utama & Stiker Piala Terbuka!"
    }

    val pillShape = RoundedCornerShape(50)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(2.5.dp, WonderQuestDesignSystem.Colors.PlayfulPurple.copy(alpha = 0.45f)),
                WonderQuestDesignSystem.ShapeTokens.HeroBannerShape
            )
            .testTag("daily_challenges_section"),
        shape = WonderQuestDesignSystem.ShapeTokens.HeroBannerShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            WonderQuestDesignSystem.Colors.PurpleSoft.copy(alpha = 0.55f),
                            WonderQuestDesignSystem.Colors.CreamBackground.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Row with Title, Progress Counter, and Shuffle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = WonderQuestDesignSystem.Colors.PurpleInteractive,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = headerTitle,
                                tint = WonderQuestDesignSystem.Colors.SunshineYellow,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = headerSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Shuffle / Refresh 3 Daily Tasks Button
                Surface(
                    shape = pillShape,
                    color = WonderQuestDesignSystem.Colors.SkyBlueInteractive.copy(alpha = 0.14f),
                    border = BorderStroke(1.5.dp, WonderQuestDesignSystem.Colors.SkyBlueInteractive.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(pillShape)
                        .clickable {
                            SoundManager.playButtonClick(!isSoundMuted)
                            seedCounter += 1
                        }
                        .testTag("daily_challenges_refresh_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = shuffleLabel,
                            tint = WonderQuestDesignSystem.Colors.SkyBlueInteractive,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = shuffleLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = WonderQuestDesignSystem.Colors.SkyBlueInteractive
                        )
                    }
                }
            }

            // 2. Sticker Collection & Star Progress Bar
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, WonderQuestDesignSystem.Colors.StarGold.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_challenges_sticker_tray")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$stickerTrayLabel ($completedCount/3)",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = pillShape,
                            color = WonderQuestDesignSystem.Colors.SunshineYellow.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = WonderQuestDesignSystem.Colors.StarGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "+$earnedStarsToday",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WonderQuestDesignSystem.Colors.PrimaryText,
                                    modifier = Modifier.testTag("daily_challenges_earned_stars")
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (completedCount / 3f).coerceIn(0.08f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = WonderQuestDesignSystem.Colors.MintInteractive,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // 3 Collectible Sticker Medallions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tasks.forEach { task ->
                            val stickerColor = Color(task.rewardSticker.colorHex)
                            val scale by animateFloatAsState(
                                targetValue = if (task.isCompleted) 1.04f else 0.96f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "sticker_slot_scale_${task.slotIndex}"
                            )
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (task.isCompleted) {
                                    stickerColor.copy(alpha = 0.16f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                },
                                border = BorderStroke(
                                    width = if (task.isCompleted) 2.dp else 1.dp,
                                    color = if (task.isCompleted) stickerColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(scale)
                                    .testTag("daily_sticker_slot_${task.slotIndex}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = task.rewardSticker.drawableRes),
                                        contentDescription = task.rewardSticker.localizedTitle(languageCode),
                                        modifier = Modifier.size(28.dp),
                                        alpha = if (task.isCompleted) 1f else 0.38f
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.rewardSticker.localizedTitle(languageCode),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FredokaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isCompleted) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "+${task.rewardStars} Bintang",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (task.isCompleted) stickerColor else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Grand Celebration Banner when all 3 tasks are completed
            AnimatedVisibility(
                visible = completedCount == 3,
                enter = fadeIn() + expandVertically() + scaleIn(initialScale = 0.92f)
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = WonderQuestDesignSystem.Colors.MintSoft,
                    border = BorderStroke(2.dp, WonderQuestDesignSystem.Colors.MintInteractive),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_challenges_all_complete_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_badge_trophy_cup),
                            contentDescription = grandBonusTitle,
                            modifier = Modifier.size(44.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = grandBonusTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = WonderQuestDesignSystem.Colors.PrimaryText
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = WonderQuestDesignSystem.Colors.StarGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // 4. Three Small Randomized Math & Logic Task Cards
            tasks.forEachIndexed { index, task ->
                DailyChallengeTaskCard(
                    task = task,
                    languageCode = languageCode,
                    onSelectOption = { chosenOptionIndex ->
                        if (task.isCompleted) return@DailyChallengeTaskCard
                        val isCorrect = chosenOptionIndex == task.question.correctIndex
                        if (isCorrect) {
                            SoundManager.playCorrectAnswer(!isSoundMuted)
                            SoundManager.playRewardTrigger(!isSoundMuted)
                            tasks = tasks.toMutableList().also { list ->
                                list[index] = task.copy(
                                    selectedOptionIndex = chosenOptionIndex,
                                    isCompleted = true,
                                    showHint = false
                                )
                            }
                            onRewardEarned(
                                task.rewardStars,
                                task.rewardSticker.badgeId,
                                task.gameType.isMath
                            )
                        } else {
                            SoundManager.playGentleRetry(!isSoundMuted)
                            tasks = tasks.toMutableList().also { list ->
                                list[index] = task.copy(
                                    selectedOptionIndex = chosenOptionIndex,
                                    isCompleted = false,
                                    showHint = true
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyChallengeTaskCard(
    task: DailyChallengeTask,
    languageCode: String,
    onSelectOption: (Int) -> Unit
) {
    val accentColor = Color(task.gameType.accentColorHex)
    val stickerColor = Color(task.rewardSticker.colorHex)
    val pillShape = RoundedCornerShape(50)
    val cardBorderColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            WonderQuestDesignSystem.Colors.MintInteractive
        } else {
            accentColor.copy(alpha = 0.42f)
        },
        label = "daily_task_border_${task.slotIndex}"
    )

    val solvedBadgeLabel = when (languageCode) {
        "en" -> "Completed! +${task.rewardStars} Stars & Sticker Unlocked"
        "es" -> "¡Completado! +${task.rewardStars} Estrellas y Pegatina"
        "fr" -> "Terminé ! +${task.rewardStars} Étoiles & Autocollant"
        "hi" -> "पूरा हुआ! +${task.rewardStars} सितारे और स्टिकर"
        else -> "Selesai! +${task.rewardStars} Bintang & Stiker Terbuka"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (task.isCompleted) {
            WonderQuestDesignSystem.Colors.MintSoft.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(2.dp, cardBorderColor),
        shadowElevation = if (task.isCompleted) 2.dp else 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_challenge_card_${task.slotIndex}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Task Header Row: Task Number + Mini-Game Category + Star & Sticker Reward Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#${task.slotIndex + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Image(
                        painter = painterResource(id = task.gameType.drawableRes),
                        contentDescription = task.gameType.localizedTitle(languageCode),
                        modifier = Modifier.size(28.dp)
                    )

                    Text(
                        text = task.gameType.localizedTitle(languageCode),
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Reward Pill
                Surface(
                    shape = pillShape,
                    color = if (task.isCompleted) {
                        WonderQuestDesignSystem.Colors.MintInteractive
                    } else {
                        stickerColor.copy(alpha = 0.16f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Star,
                            contentDescription = null,
                            tint = if (task.isCompleted) Color.White else WonderQuestDesignSystem.Colors.StarGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "+${task.rewardStars}",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (task.isCompleted) Color.White else stickerColor
                        )
                    }
                }
            }

            // Visual Tokens Preview + Visual Equation Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sampleTokens = task.question.visualTokens.take(4)
                    sampleTokens.forEach { token ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                TokenIllustrationCanvas(
                                    tokenCode = token,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (task.question.visualEquationOrPattern.isNotBlank()) {
                    Text(
                        text = task.question.visualEquationOrPattern,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Question Prompt
            Text(
                text = task.question.prompt,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 4 Interactive Answer Option Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                task.question.options.forEachIndexed { optionIndex, optionText ->
                    val isSelected = task.selectedOptionIndex == optionIndex
                    val isRightOption = optionIndex == task.question.correctIndex

                    val containerColor = when {
                        task.isCompleted && isRightOption -> WonderQuestDesignSystem.Colors.MintInteractive
                        isSelected && !task.isCompleted -> WonderQuestDesignSystem.Colors.CoralInteractive
                        else -> accentColor.copy(alpha = 0.14f)
                    }
                    val contentColor = when {
                        (task.isCompleted && isRightOption) || (isSelected && !task.isCompleted) -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    PlayfulBouncyButton(
                        onClick = { onSelectOption(optionIndex) },
                        enabled = !task.isCompleted,
                        containerColor = containerColor,
                        contentColor = contentColor,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("daily_task_${task.slotIndex}_option_$optionIndex")
                    ) {
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Gentle Hint if user picked a wrong option
            AnimatedVisibility(visible = task.showHint && !task.isCompleted) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WonderQuestDesignSystem.Colors.SunshineSoft,
                    border = BorderStroke(1.dp, WonderQuestDesignSystem.Colors.StarGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_task_${task.slotIndex}_hint")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = WonderQuestDesignSystem.Colors.StarGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = task.question.hintText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = WonderQuestDesignSystem.Colors.PrimaryText
                        )
                    }
                }
            }

            // Completed Sticker Reward Row
            AnimatedVisibility(visible = task.isCompleted) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WonderQuestDesignSystem.Colors.MintInteractive.copy(alpha = 0.14f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_task_${task.slotIndex}_completed_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = task.rewardSticker.drawableRes),
                            contentDescription = task.rewardSticker.localizedTitle(languageCode),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "$solvedBadgeLabel (${task.rewardSticker.localizedTitle(languageCode)})",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = WonderQuestDesignSystem.Colors.MintInteractive
                        )
                    }
                }
            }
        }
    }
}
