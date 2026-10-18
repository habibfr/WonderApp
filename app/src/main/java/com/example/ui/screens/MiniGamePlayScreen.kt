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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BadgeCatalog
import com.example.data.BuddyCharacter
import com.example.data.BuddyReaction
import com.example.data.LocalizedUiStrings
import com.example.domain.MiniGameQuestion
import com.example.domain.VisualSceneKind
import com.example.ui.ActiveGameState
import com.example.ui.components.AnimatedBuddyCanvas
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
            onPlayNextLevel = onPlayNextLevel,
            onBackToIsland = onBackToIsland,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("minigame_play_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header with Back Button, Game Title, Mode Badge, and Hint Button
        Row(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackToIsland,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("minigame_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = strings.backToIslandButton
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${activeGame.gameType.emoji} ${activeGame.gameType.localizedTitle(languageCode)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${activeGame.playMode.emoji} ${strings.levelPrefix} ${activeGame.levelNumber} • ${strings.questionProgress} ${question.index}/${question.total}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onToggleHint,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .testTag("minigame_hint_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = strings.hintButton,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        // Progress Bar + Optional Star Blitz Countdown
        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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

            activeGame.remainingBlitzSeconds?.let { remSec ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFF7043).copy(alpha = 0.16f)
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
                            text = "⏱️ ${remSec}s",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE64A19)
                        )
                    }
                }
            }
        }

        // Animated Buddy Coach & Prompt Card
        Card(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedBuddyCanvas(
                    character = buddy,
                    reaction = buddyReaction,
                    modifier = Modifier
                        .size(84.dp)
                        .testTag("minigame_buddy_coach")
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = question.prompt,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag("question_prompt_text")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Interactive Visual Puzzle Stage (Canvas + Tappable Counting Objects)
        Card(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Custom Visual Canvas for Shape Galaxy & Cosmic Balance
                when (question.sceneKind) {
                    VisualSceneKind.POLYGON_GALAXY -> {
                        InteractivePolygonGalaxyCanvas(
                            sides = question.polygonSides,
                            accent = accent,
                            modifier = Modifier
                                .size(140.dp)
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
                                .height(120.dp)
                                .testTag("cosmic_balance_canvas")
                        )
                    }
                    else -> Unit
                }

                // Visual Equation / Pattern Banner
                Text(
                    text = question.visualEquationOrPattern,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("question_visual_equation")
                )

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
                            targetValue = if (isTapped) 1.16f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "token_scale_$idx"
                        )

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isTapped) accent.copy(alpha = 0.28f) else accent.copy(alpha = 0.12f),
                            tonalElevation = if (isTapped) 6.dp else 1.dp,
                            modifier = Modifier
                                .scale(tokenScale)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onToggleVisualToken(idx) }
                                .testTag("visual_token_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = token,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (tapOrder != null) {
                                    Surface(
                                        shape = CircleShape,
                                        color = accent
                                    ) {
                                        Text(
                                            text = "$tapOrder",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hint Drawer
        AnimatedVisibility(visible = activeGame.showHint) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxWidth()
                    .testTag("hint_banner"),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = strings.hintButton,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = question.hintText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // 2x2 Visual Answer Options Grid
        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
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

                        val containerColor by animateColorAsState(
                            targetValue = when {
                                activeGame.isAnswerSubmitted && isCorrectOption && (isSelected || activeGame.isCurrentCorrect) ->
                                    Color(0xFF2E7D32)
                                activeGame.isAnswerSubmitted && isSelected && !isCorrectOption ->
                                    MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            label = "option_bg_$optionIndex"
                        )

                        val contentColor = when {
                            activeGame.isAnswerSubmitted && (isSelected || (isCorrectOption && activeGame.isCurrentCorrect)) ->
                                Color.White
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }

                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.04f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "option_scale_$optionIndex"
                        )

                        ElevatedButton(
                            onClick = { onSelectOption(optionIndex) },
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = containerColor,
                                contentColor = contentColor
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp)
                                .scale(scale)
                                .testTag("answer_option_$optionIndex")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (!optionIcon.isNullOrBlank() && optionIcon != optionText) {
                                    Text(
                                        text = optionIcon,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cheer & Next Stage Button
        AnimatedVisibility(visible = activeGame.isAnswerSubmitted) {
            Column(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (activeGame.isCurrentCorrect) strings.correctCheer else strings.tryAgainCheer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (activeGame.isCurrentCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("answer_feedback_text")
                )

                if (activeGame.isCurrentCorrect) {
                    val isLast = activeGame.currentQuestionIndex >= activeGame.questions.lastIndex
                    Button(
                        onClick = onNextOrFinish,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
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

        drawPath(path = path, color = accent.copy(alpha = 0.25f))
        drawPath(
            path = path,
            color = accent,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Glowing corner vertices so kids can visually count the corners!
        vertices.forEach { pt ->
            drawCircle(color = Color(0xFFFFD54F), radius = 11f, center = pt)
            drawCircle(color = Color.White, radius = 5f, center = pt)
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

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val pivotY = h * 0.55f

        // Fulcrum triangle
        val fulcrum = Path().apply {
            moveTo(cx, pivotY)
            lineTo(cx - 26f, h * 0.90f)
            lineTo(cx + 26f, h * 0.90f)
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
            color = if (isBalanced) Color(0xFF2E7D32) else Color(0xFF5C6BC0),
            start = Offset(leftX, leftY),
            end = Offset(rightX, rightY),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )

        // Left & Right Pans
        drawCircle(
            color = Color(0xFFFFB300),
            radius = 22f,
            center = Offset(leftX, leftY - 16f)
        )
        drawCircle(
            color = if (isBalanced) Color(0xFF66BB6A) else Color(0xFFAB47BC),
            radius = 22f,
            center = Offset(rightX, rightY - 16f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LevelCompletedCelebration(
    activeGame: ActiveGameState,
    buddy: BuddyCharacter,
    languageCode: String,
    strings: LocalizedUiStrings,
    accent: Color,
    onPlayNextLevel: () -> Unit,
    onBackToIsland: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("level_complete_card"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AnimatedBuddyCanvas(
                    character = buddy,
                    reaction = BuddyReaction.CELEBRATING,
                    modifier = Modifier.size(150.dp)
                )

                Text(
                    text = strings.levelCompleteTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent,
                    textAlign = TextAlign.Center
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in 1..3) {
                        val earned = i <= activeGame.earnedStars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (earned) Color(0xFFFFB300) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                if (activeGame.newlyUnlockedBadges.isNotEmpty()) {
                    Text(
                        text = strings.newBadgesUnlocked,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeGame.newlyUnlockedBadges.forEach { badgeId ->
                            val badge = BadgeCatalog.allBadges.find { it.id == badgeId }
                            if (badge != null) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(badge.colorHex).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${badge.emoji} ${badge.localizedTitle(languageCode)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onPlayNextLevel,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("play_next_level_button")
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.playNextLevelButton,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onBackToIsland,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("back_to_island_button")
                ) {
                    Text(
                        text = strings.backToIslandButton,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
