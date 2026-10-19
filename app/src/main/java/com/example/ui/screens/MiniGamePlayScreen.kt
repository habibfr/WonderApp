package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BadgeCatalog
import com.example.data.BuddyCharacter
import com.example.data.BuddyReaction
import com.example.data.LocalizedUiStrings
import com.example.domain.VisualSceneKind
import com.example.ui.ActiveGameState
import com.example.ui.components.AnimatedBuddyCanvas
import com.example.ui.components.BuddyCharacter as BuddyCharacterComponent
import com.example.ui.components.BuddyCharacterState
import com.example.ui.components.ColorfulVisualTokenSymbol
import com.example.ui.components.MiniGameStageSceneBanner
import com.example.ui.components.PlayfulSoundEffects
import com.example.ui.components.QuestionStepMilestoneRail
import com.example.ui.components.RewardOverlay
import com.example.ui.components.StarSparkleCelebrationOverlay
import com.example.ui.components.WonderQuestIcon
import com.example.ui.components.toBuddyCharacterState
import com.example.ui.theme.WonderQuestDesignSystem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MiniGamePlayScreen(
    activeGame: ActiveGameState,
    buddy: BuddyCharacter,
    buddyReaction: BuddyReaction,
    languageCode: String,
    strings: LocalizedUiStrings,
    onSelectOption: (Int) -> Unit,
    onToggleHint: () -> Unit,
    onToggleVisualToken: (Int) -> Unit = {},
    onNextOrFinish: () -> Unit,
    onPlayNextLevel: () -> Unit,
    onBackToIsland: () -> Unit,
    isSoundMuted: Boolean = false,
    onToggleSoundMute: () -> Unit = {},
    onBuddyTap: (BuddyReaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToIsland)

    val accent = Color(activeGame.gameType.accentColorHex)

    if (activeGame.isCompleted) {
        LevelCompletedCelebration(
            activeGame = activeGame,
            buddy = buddy,
            languageCode = languageCode,
            strings = strings,
            accent = accent,
            onPlayNextLevel = {
                PlayfulSoundEffects.playTap(!isSoundMuted)
                onPlayNextLevel()
            },
            onBackToIsland = {
                PlayfulSoundEffects.playTap(!isSoundMuted)
                onBackToIsland()
            },
            modifier = modifier
        )
        return
    }

    val question = activeGame.currentQuestion
    val progress = (activeGame.currentQuestionIndex + 1).toFloat() / activeGame.questions.size.coerceAtLeast(1).toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 450),
        label = "question_progress"
    )

    val infiniteHint = rememberInfiniteTransition(label = "hint_pulse")
    val hintPulseScale by infiniteHint.animateFloat(
        initialValue = 1f,
        targetValue = if (activeGame.isAnswerSubmitted && !activeGame.isCurrentCorrect && !activeGame.showHint) 1.14f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background,
                        accent.copy(alpha = 0.06f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("minigame_play_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Tactile Game HUD Header Bar
            Surface(
                modifier = Modifier
                    .widthIn(max = WonderQuestDesignSystem.SpacingTokens.MaxReadableContentWidth)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                border = BorderStroke(2.dp, accent.copy(alpha = 0.28f)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            PlayfulSoundEffects.playTap(!isSoundMuted)
                            onBackToIsland()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("minigame_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.backToIslandButton,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = accent.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = activeGame.gameType.drawableRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = activeGame.gameType.localizedTitle(languageCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${activeGame.playMode.localizedTitle(languageCode)} • ${strings.levelPrefix} ${activeGame.levelNumber} • ${strings.questionProgress} ${question.index}/${question.total}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onToggleSoundMute,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .testTag("minigame_sound_toggle")
                        ) {
                            Icon(
                                imageVector = if (isSoundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = strings.soundEffectsLabel,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                PlayfulSoundEffects.playTap(!isSoundMuted)
                                onToggleHint()
                            },
                            modifier = Modifier
                                .scale(hintPulseScale)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(WonderQuestDesignSystem.Colors.SunshineYellow)
                                .border(2.dp, WonderQuestDesignSystem.Colors.SunshineGold, CircleShape)
                                .testTag("minigame_hint_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = strings.hintButton,
                                tint = Color(0xFF78350F)
                            )
                        }
                    }
                }
            }

            // 2. Stepping-Stone Milestone Rail + Progress Bar + Streak & Blitz Pills
            Column(
                modifier = Modifier
                    .widthIn(max = WonderQuestDesignSystem.SpacingTokens.MaxReadableContentWidth)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestionStepMilestoneRail(
                    totalQuestions = activeGame.questions.size,
                    currentQuestionIndex = activeGame.currentQuestionIndex,
                    accent = accent
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .testTag("minigame_progress_bar"),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeGame.currentStreak > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = WonderQuestDesignSystem.Colors.StreakFlame.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, WonderQuestDesignSystem.Colors.StreakFlame.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = WonderQuestDesignSystem.Colors.StreakFlame,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activeGame.currentStreak}x Streak!",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WonderQuestDesignSystem.Colors.StreakFlame
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    activeGame.remainingBlitzSeconds?.let { remSec ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFFF7043).copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Color(0xFFFF7043).copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFFF7043),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${remSec}s",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE64A19)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Interactive Buddy Coach & Comic Speech Bubble
            Card(
                modifier = Modifier
                    .widthIn(max = WonderQuestDesignSystem.SpacingTokens.MaxReadableContentWidth)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(2.dp, accent.copy(alpha = 0.32f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val buddyMoodState = when {
                        activeGame.isAnswerSubmitted && activeGame.isCurrentCorrect -> BuddyCharacterState.HAPPY
                        activeGame.isAnswerSubmitted && !activeGame.isCurrentCorrect -> BuddyCharacterState.THINKING
                        activeGame.showHint -> BuddyCharacterState.THINKING
                        activeGame.tappedTokenIndices.isNotEmpty() -> BuddyCharacterState.HAPPY
                        else -> buddyReaction.toBuddyCharacterState()
                    }

                    BuddyCharacterComponent(
                        state = buddyMoodState,
                        character = buddy,
                        reactionOverride = buddyReaction,
                        languageCode = languageCode,
                        showMoodPill = true,
                        onTap = {
                            PlayfulSoundEffects.playTap(!isSoundMuted)
                            onBuddyTap(BuddyReaction.HIGH_FIVE)
                        },
                        modifier = Modifier.testTag("minigame_buddy_coach")
                    )

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = question.prompt,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("question_prompt_text")
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = accent.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.tapVisualCounterHint,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Interactive Visual Puzzle Stage (Custom Animated Scene + Equation Board + Tappable Counting Tokens)
            Card(
                modifier = Modifier
                    .widthIn(max = WonderQuestDesignSystem.SpacingTokens.MaxReadableContentWidth)
                    .fillMaxWidth(),
                shape = WonderQuestDesignSystem.ShapeTokens.QuestCardShape,
                border = BorderStroke(2.5.dp, accent.copy(alpha = 0.36f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (activeGame.isAnswerSubmitted && activeGame.isCurrentCorrect) {
                        StarSparkleCelebrationOverlay(
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        accent.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Animated Scene Canvas for ALL 7 Mini-Games
                        when (question.sceneKind) {
                            VisualSceneKind.POLYGON_GALAXY -> {
                                InteractivePolygonGalaxyCanvas(
                                    sides = question.polygonSides,
                                    accent = accent,
                                    modifier = Modifier
                                        .size(144.dp)
                                        .testTag("polygon_galaxy_canvas")
                                )
                            }
                            VisualSceneKind.BALANCE_BEAM -> {
                                InteractiveBalanceBeamCanvas(
                                    leftWeight = question.balanceLeftWeight,
                                    rightKnownWeight = question.balanceRightKnownWeight,
                                    isBalanced = activeGame.isAnswerSubmitted && activeGame.isCurrentCorrect,
                                    accent = accent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(124.dp)
                                        .testTag("cosmic_balance_canvas")
                                )
                            }
                            else -> {
                                MiniGameStageSceneBanner(
                                    sceneKind = question.sceneKind,
                                    accent = accent,
                                    isSolved = activeGame.isAnswerSubmitted && activeGame.isCurrentCorrect,
                                    tappedCount = activeGame.tappedTokenIndices.size,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(82.dp)
                                        .testTag("minigame_stage_scene_canvas")
                                )
                            }
                        }

                        // Chalkboard / Hologram Visual Equation & Pattern Board
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = accent.copy(alpha = 0.10f),
                            border = BorderStroke(2.dp, accent.copy(alpha = 0.30f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = question.visualEquationOrPattern,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .testTag("question_visual_equation")
                            )
                        }

                        // Live Tap Counter Badge when child taps tokens to count
                        if (activeGame.tappedTokenIndices.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = WonderQuestDesignSystem.Colors.SunshineYellow.copy(alpha = 0.28f),
                                border = BorderStroke(1.5.dp, WonderQuestDesignSystem.Colors.SunshineGold),
                                modifier = Modifier.testTag("counted_tokens_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = WonderQuestDesignSystem.Colors.SunshineGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = when (languageCode) {
                                            "en" -> "Counted: ${activeGame.tappedTokenIndices.size}"
                                            "es" -> "Contados: ${activeGame.tappedTokenIndices.size}"
                                            "fr" -> "Comptés : ${activeGame.tappedTokenIndices.size}"
                                            "hi" -> "गिने गए: ${activeGame.tappedTokenIndices.size}"
                                            else -> "Terhitung: ${activeGame.tappedTokenIndices.size}"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Interactive Tappable Visual Tokens (Children tap icons to count them with spring bounce!)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            question.visualTokens.forEachIndexed { idx, token ->
                                val isTapped = idx in activeGame.tappedTokenIndices
                                val tapOrder = if (isTapped) {
                                    activeGame.tappedTokenIndices.indexOf(idx) + 1
                                } else null
                                val tokenScale by animateFloatAsState(
                                    targetValue = if (isTapped) 1.15f else 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "token_scale_$idx"
                                )

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isTapped) accent.copy(alpha = 0.26f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    border = BorderStroke(
                                        width = if (isTapped) 2.5.dp else 1.5.dp,
                                        color = if (isTapped) accent else accent.copy(alpha = 0.30f)
                                    ),
                                    shadowElevation = if (isTapped) 5.dp else 2.dp,
                                    modifier = Modifier
                                        .scale(tokenScale)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            PlayfulSoundEffects.playTap(!isSoundMuted)
                                            onToggleVisualToken(idx)
                                        }
                                        .testTag("visual_token_$idx")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ColorfulVisualTokenSymbol(
                                            token = token,
                                            accent = accent,
                                            symbolSize = 36.dp
                                        )
                                        if (tapOrder != null) {
                                            Surface(
                                                shape = CircleShape,
                                                color = accent
                                            ) {
                                                Text(
                                                    text = "$tapOrder",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Animated Hint Drawer
            AnimatedVisibility(visible = activeGame.showHint) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = WonderQuestDesignSystem.SpacingTokens.MaxReadableContentWidth)
                        .fillMaxWidth()
                        .testTag("hint_banner"),
                    shape = RoundedCornerShape(20.dp),
                    color = WonderQuestDesignSystem.Colors.SunshineSoft,
                    border = BorderStroke(2.dp, WonderQuestDesignSystem.Colors.SunshineGold)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = WonderQuestDesignSystem.Colors.SunshineYellow,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = strings.hintButton,
                                    tint = Color(0xFF78350F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = question.hintText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }

            // 6. Tactile 3D 2x2 Visual Answer Options Grid
            val optionBadges = listOf("A", "B", "C", "D")
            val optionMedallionColors = listOf(
                WonderQuestDesignSystem.Colors.SkyBlueInteractive,
                WonderQuestDesignSystem.Colors.PurpleInteractive,
                WonderQuestDesignSystem.Colors.CoralInteractive,
                WonderQuestDesignSystem.Colors.MintInteractive
            )

            Column(
                modifier = Modifier
                    .widthIn(max = WonderQuestDesignSystem.SpacingTokens.MaxReadableContentWidth)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                question.options.chunked(2).forEachIndexed { rowIdx, rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowOptions.forEachIndexed { colIdx, optionText ->
                            val optionIndex = rowIdx * 2 + colIdx
                            val optionIcon = question.optionIcons.getOrNull(optionIndex)
                            val isSelected = activeGame.selectedOptionIndex == optionIndex
                            val isCorrectOption = optionIndex == question.correctIndex
                            val badgeLetter = optionBadges.getOrElse(optionIndex) { "${optionIndex + 1}" }
                            val medallionColor = optionMedallionColors[optionIndex % optionMedallionColors.size]

                            val containerColor by animateColorAsState(
                                targetValue = when {
                                    activeGame.isAnswerSubmitted && isCorrectOption && (isSelected || activeGame.isCurrentCorrect) ->
                                        Color(0xFF10B981)
                                    activeGame.isAnswerSubmitted && isSelected && !isCorrectOption ->
                                        Color(0xFFEF4444)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                label = "option_bg_$optionIndex"
                            )

                            val borderColor = when {
                                activeGame.isAnswerSubmitted && isCorrectOption && (isSelected || activeGame.isCurrentCorrect) ->
                                    Color(0xFF059669)
                                activeGame.isAnswerSubmitted && isSelected && !isCorrectOption ->
                                    Color(0xFFB91C1C)
                                isSelected -> accent
                                else -> medallionColor.copy(alpha = 0.45f)
                            }

                            val contentColor = when {
                                activeGame.isAnswerSubmitted && (isSelected || (isCorrectOption && activeGame.isCurrentCorrect)) ->
                                    Color.White
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.04f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "option_scale_$optionIndex"
                            )

                            ElevatedButton(
                                onClick = {
                                    if (optionIndex == question.correctIndex) {
                                        PlayfulSoundEffects.playSuccess(!isSoundMuted)
                                    } else {
                                        PlayfulSoundEffects.playGentleRetry(!isSoundMuted)
                                    }
                                    onSelectOption(optionIndex)
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = containerColor,
                                    contentColor = contentColor
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                elevation = ButtonDefaults.elevatedButtonElevation(
                                    defaultElevation = if (isSelected) 2.dp else 6.dp,
                                    pressedElevation = 1.dp
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .scale(scale)
                                    .border(2.5.dp, borderColor, RoundedCornerShape(24.dp))
                                    .testTag("answer_option_$optionIndex")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (activeGame.isAnswerSubmitted && (isSelected || (isCorrectOption && activeGame.isCurrentCorrect))) {
                                            Color.White.copy(alpha = 0.25f)
                                        } else {
                                            medallionColor.copy(alpha = 0.18f)
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = badgeLetter,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (activeGame.isAnswerSubmitted && (isSelected || (isCorrectOption && activeGame.isCurrentCorrect))) {
                                                    Color.White
                                                } else {
                                                    medallionColor
                                                }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (!optionIcon.isNullOrBlank() && optionIcon != optionText) {
                                            ColorfulVisualTokenSymbol(
                                                token = optionIcon,
                                                accent = accent,
                                                symbolSize = 30.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = optionText,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (activeGame.isAnswerSubmitted && isCorrectOption && (isSelected || activeGame.isCurrentCorrect)) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Celebratory Feedback Banner & Next Stage Button
            AnimatedVisibility(visible = activeGame.isAnswerSubmitted) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = WonderQuestDesignSystem.SpacingTokens.MaxReadableContentWidth)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = if (activeGame.isCurrentCorrect) {
                        Color(0xFF10B981).copy(alpha = 0.14f)
                    } else {
                        Color(0xFFFF7043).copy(alpha = 0.14f)
                    },
                    border = BorderStroke(
                        2.dp,
                        if (activeGame.isCurrentCorrect) Color(0xFF10B981) else Color(0xFFFF7043)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (activeGame.isCurrentCorrect) strings.correctCheer else strings.tryAgainCheer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (activeGame.isCurrentCorrect) Color(0xFF047857) else Color(0xFFD84315),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("answer_feedback_text")
                        )

                        if (!activeGame.isCurrentCorrect && !activeGame.showHint) {
                            FilledTonalButton(
                                onClick = {
                                    PlayfulSoundEffects.playTap(!isSoundMuted)
                                    onToggleHint()
                                },
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.hintButton,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (activeGame.isCurrentCorrect) {
                            val isLast = activeGame.currentQuestionIndex >= activeGame.questions.lastIndex
                            Button(
                                onClick = {
                                    PlayfulSoundEffects.playTap(!isSoundMuted)
                                    onNextOrFinish()
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("next_or_finish_button")
                            ) {
                                Text(
                                    text = if (isLast) strings.finishLevelButton else strings.nextQuestionButton,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (isLast) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null
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
private fun InteractivePolygonGalaxyCanvas(
    sides: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "polygon_spin")
    val spinDeg by infinite.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "polygon_spin_deg"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(size.width, size.height) * 0.38f
        val safeSides = sides.coerceIn(3, 10)
        val path = Path()
        val vertices = mutableListOf<Offset>()

        for (i in 0 until safeSides) {
            val angleRad = ((i * 360f / safeSides - 90f + spinDeg) * PI / 180.0).toFloat()
            val vx = cx + cos(angleRad) * radius
            val vy = cy + sin(angleRad) * radius
            vertices.add(Offset(vx, vy))
            if (i == 0) path.moveTo(vx, vy) else path.lineTo(vx, vy)
        }
        path.close()

        drawCircle(
            color = accent.copy(alpha = 0.08f),
            radius = radius * 1.18f,
            center = Offset(cx, cy)
        )
        drawPath(path = path, color = accent.copy(alpha = 0.25f))
        drawPath(
            path = path,
            color = accent,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Glowing corner vertices so kids can visually count the corners!
        vertices.forEach { pt ->
            drawCircle(color = Color(0xFFFFD54F), radius = 12f, center = pt)
            drawCircle(color = Color.White, radius = 5.5f, center = pt)
        }
    }
}

@Composable
private fun InteractiveBalanceBeamCanvas(
    leftWeight: Int,
    rightKnownWeight: Int,
    isBalanced: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val tiltDeg by animateFloatAsState(
        targetValue = if (isBalanced) 0f else -10f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "beam_tilt"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val pivotY = h * 0.56f

            // Fulcrum triangle
            val fulcrum = Path().apply {
                moveTo(cx, pivotY)
                lineTo(cx - 28f, h * 0.90f)
                lineTo(cx + 28f, h * 0.90f)
                close()
            }
            drawPath(fulcrum, color = accent)

            val beamHalf = w * 0.34f
            val rad = (tiltDeg * PI / 180.0).toFloat()
            val leftX = cx - cos(rad) * beamHalf
            val leftY = pivotY - sin(rad) * beamHalf
            val rightX = cx + cos(rad) * beamHalf
            val rightY = pivotY + sin(rad) * beamHalf

            // Balance beam bar
            drawLine(
                color = if (isBalanced) Color(0xFF10B981) else Color(0xFF5C6BC0),
                start = Offset(leftX, leftY),
                end = Offset(rightX, rightY),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )

            // Left & Right Pans
            drawCircle(
                color = Color(0xFFFFB300),
                radius = 24f,
                center = Offset(leftX, leftY - 18f)
            )
            drawCircle(
                color = if (isBalanced) Color(0xFF10B981) else Color(0xFFAB47BC),
                radius = 24f,
                center = Offset(rightX, rightY - 18f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFB300).copy(alpha = 0.22f),
                border = BorderStroke(1.5.dp, Color(0xFFFFB300))
            ) {
                Text(
                    text = "$leftWeight",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = (if (isBalanced) Color(0xFF10B981) else Color(0xFFAB47BC)).copy(alpha = 0.22f),
                border = BorderStroke(1.5.dp, if (isBalanced) Color(0xFF10B981) else Color(0xFFAB47BC))
            ) {
                Text(
                    text = if (isBalanced) "$leftWeight" else "$rightKnownWeight + ?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LevelCompletedCelebration(
    activeGame: ActiveGameState,
    buddy: BuddyCharacter,
    languageCode: String,
    strings: LocalizedUiStrings,
    accent: Color,
    isSoundMuted: Boolean = false,
    onPlayNextLevel: () -> Unit,
    onBackToIsland: () -> Unit,
    modifier: Modifier = Modifier
) {
    RewardOverlay(
        visible = true,
        title = strings.levelCompleteTitle,
        subtitle = "${activeGame.gameType.localizedTitle(languageCode)} • ${strings.levelPrefix} ${activeGame.levelNumber}",
        earnedStars = activeGame.earnedStars,
        maxStars = 3,
        correctAnswers = activeGame.correctCount,
        totalQuestions = activeGame.questions.size,
        unlockedBadgeIds = activeGame.newlyUnlockedBadges,
        buddy = buddy,
        languageCode = languageCode,
        accentColor = accent,
        isSoundMuted = isSoundMuted,
        primaryButtonText = strings.playNextLevelButton,
        secondaryButtonText = strings.backToIslandButton,
        questionProgressLabel = strings.questionProgress,
        starsEarnedLabel = strings.starsLabel,
        newBadgesUnlockedLabel = strings.newBadgesUnlocked,
        onPrimaryAction = onPlayNextLevel,
        onDismiss = onBackToIsland,
        modifier = modifier
    )
}

