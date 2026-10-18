package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.ALL_REWARD_BADGES
import com.example.data.AgeBracket
import com.example.data.BuddyCharacter
import com.example.data.ChildProfile
import com.example.data.LocalizedUiStrings
import com.example.data.MiniGameType
import com.example.domain.MiniGameGenerator
import com.example.ui.components.AnimatedBuddyCanvas
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MiniGamePlayScreen(
    gameType: MiniGameType,
    levelNumber: Int,
    profile: ChildProfile,
    strings: LocalizedUiStrings,
    newlyUnlockedBadges: List<String>,
    onLevelCompleted: (stars: Int, correct: Int, total: Int, durationSec: Int) -> Unit,
    onPlayNextLevel: (Int) -> Unit,
    onBackToIsland: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToIsland() }

    val ageBracket = AgeBracket.fromCode(profile.ageGroup)
    val buddy = BuddyCharacter.fromId(profile.selectedCharacter)
    val questions = remember(gameType, ageBracket, levelNumber, profile.languageCode) {
        MiniGameGenerator.generateLevelQuestions(
            gameType = gameType,
            ageBracket = ageBracket,
            levelNumber = levelNumber,
            lang = profile.languageCode,
            questionCount = 5
        )
    }

    var currentIndex by remember(gameType, levelNumber) { mutableIntStateOf(0) }
    var correctCount by remember(gameType, levelNumber) { mutableIntStateOf(0) }
    var selectedOption by remember(currentIndex, levelNumber) { mutableStateOf<String?>(null) }
    var showHint by remember(currentIndex, levelNumber) { mutableStateOf(false) }
    var buddyAnimTrigger by remember { mutableIntStateOf(1) }
    var isLevelComplete by remember(gameType, levelNumber) { mutableStateOf(false) }
    var elapsedSeconds by remember(gameType, levelNumber) { mutableLongStateOf(1L) }

    LaunchedEffect(isLevelComplete, levelNumber) {
        while (!isLevelComplete) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val currentQuestion = questions.getOrElse(currentIndex) { questions.first() }
    val gameAccent = Color(gameType.colorHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("minigame_play_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation & Progress Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackToIsland,
                modifier = Modifier.testTag("back_to_island_icon_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = strings.backToIslandButton,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${gameType.title(profile.languageCode)} • ${strings.levelPrefix} $levelNumber",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = ageBracket.label(profile.languageCode),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = gameAccent.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "⏱ ${elapsedSeconds}s",
                    style = MaterialTheme.typography.labelLarge,
                    color = gameAccent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .height(10.dp),
            color = gameAccent,
            trackColor = gameAccent.copy(alpha = 0.18f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLevelComplete) {
            val earnedStars = when {
                correctCount >= 5 -> 3
                correctCount >= 3 -> 2
                else -> 1
            }
            LevelCompleteCelebrationCard(
                starsEarned = earnedStars,
                correctCount = correctCount,
                totalCount = questions.size,
                durationSeconds = elapsedSeconds.toInt(),
                buddy = buddy,
                languageCode = profile.languageCode,
                strings = strings,
                newlyUnlockedBadges = newlyUnlockedBadges,
                onPlayNextLevel = { onPlayNextLevel(levelNumber + 1) },
                onBackToIsland = onBackToIsland
            )
        } else {
            // Interactive Buddy + Question Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${strings.questionProgress} ${currentIndex + 1} / ${questions.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                showHint = !showHint
                                buddyAnimTrigger++
                            },
                            modifier = Modifier.testTag("hint_button"),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = strings.hintButton)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedBuddyCanvas(
                        character = buddy,
                        interactionTriggerCount = buddyAnimTrigger,
                        onBuddyTapped = { buddyAnimTrigger++ },
                        canvasSize = 115.dp,
                        testTagName = "gameplay_buddy_canvas"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentQuestion.prompt,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Visual Tokens Board
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            currentQuestion.visualEquationOrTokens.forEach { token ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.medium,
                                    tonalElevation = 2.dp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = token,
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = showHint) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(
                                text = "💡 ${currentQuestion.hint}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF78350F),
                                modifier = Modifier.padding(14.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 Large Accessible Answer Option Buttons
                    val optionColors = listOf(
                        Color(0xFF4F46E5),
                        Color(0xFF10B981),
                        Color(0xFFFF6B35),
                        Color(0xFFEC4899)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        currentQuestion.options.chunked(2).forEachIndexed { rowIdx, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEachIndexed { colIdx, option ->
                                    val flatIdx = rowIdx * 2 + colIdx
                                    val isChosen = selectedOption == option
                                    val isCorrect = option == currentQuestion.correctOption
                                    val containerColor = when {
                                        selectedOption == null -> optionColors[flatIdx % optionColors.size]
                                        isCorrect -> Color(0xFF10B981)
                                        isChosen -> Color(0xFFEF4444)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }

                                    Button(
                                        onClick = {
                                            if (selectedOption == null) {
                                                selectedOption = option
                                                buddyAnimTrigger++
                                                if (option == currentQuestion.correctOption) {
                                                    correctCount++
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(64.dp)
                                            .testTag("option_button_$flatIdx"),
                                        shape = MaterialTheme.shapes.large,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = containerColor
                                        )
                                    ) {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Feedback Banner & Next Button
                    if (selectedOption != null) {
                        val wasRight = selectedOption == currentQuestion.correctOption
                        Spacer(modifier = Modifier.height(18.dp))
                        Surface(
                            color = if (wasRight) {
                                Color(0xFFD1FAE5)
                            } else {
                                Color(0xFFFEE2E2)
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (wasRight) "🎉 ${strings.correctCheer}" else "🌟 ${strings.tryAgainCheer}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (wasRight) Color(0xFF065F46) else Color(0xFF991B1B),
                                    textAlign = TextAlign.Center
                                )
                                if (!wasRight) {
                                    Text(
                                        text = "💡 ${currentQuestion.hint}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF991B1B),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (currentIndex + 1 < questions.size) {
                                    currentIndex++
                                } else {
                                    val earnedStars = when {
                                        correctCount >= 5 -> 3
                                        correctCount >= 3 -> 2
                                        else -> 1
                                    }
                                    isLevelComplete = true
                                    onLevelCompleted(
                                        earnedStars,
                                        correctCount,
                                        questions.size,
                                        elapsedSeconds.toInt().coerceAtLeast(1)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("next_or_finish_button"),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentIndex + 1 < questions.size) {
                                    strings.nextQuestionButton
                                } else {
                                    strings.finishLevelButton
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCompleteCelebrationCard(
    starsEarned: Int,
    correctCount: Int,
    totalCount: Int,
    durationSeconds: Int,
    buddy: BuddyCharacter,
    languageCode: String,
    strings: LocalizedUiStrings,
    newlyUnlockedBadges: List<String>,
    onPlayNextLevel: () -> Unit,
    onBackToIsland: () -> Unit
) {
    var celebrateTap by remember { mutableIntStateOf(2) }
    val starScale by animateFloatAsState(
        targetValue = 1.1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "star_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("level_complete_card"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎊 ${strings.levelCompleteTitle} 🎊",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedBuddyCanvas(
                character = buddy,
                interactionTriggerCount = celebrateTap,
                onBuddyTapped = { celebrateTap++ },
                canvasSize = 150.dp,
                testTagName = "celebration_buddy_canvas"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Golden Stars Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val earned = i <= starsEarned
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(if (earned) starScale else 0.9f)
                            .background(
                                color = if (earned) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (earned) Color(0xFFF59E0B) else Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "✅ $correctCount / $totalCount • ⏱ ${durationSeconds}s",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (newlyUnlockedBadges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🏅 ${strings.newBadgesUnlocked}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        newlyUnlockedBadges.forEach { badgeId ->
                            val badge = ALL_REWARD_BADGES.find { it.id == badgeId }
                            if (badge != null) {
                                Text(
                                    text = "${badge.iconEmoji} ${badge.title(languageCode)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackToIsland,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("celebration_back_button"),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(text = strings.backToIslandButton)
                }

                Button(
                    onClick = onPlayNextLevel,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("celebration_next_level_button"),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(text = strings.playNextLevelButton)
                }
            }
        }
    }
}
