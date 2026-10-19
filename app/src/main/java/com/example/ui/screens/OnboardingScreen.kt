package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AgeGroupConfig
import com.example.data.BuddyCharacter
import com.example.data.BuddyReaction
import com.example.ui.components.AnimatedBuddyCanvas
import com.example.ui.components.BuddyCharacterState
import com.example.ui.components.StarSparkleCelebrationOverlay
import com.example.ui.components.WonderIslandIllustratedBanner
import com.example.ui.theme.WonderQuestDesignSystem
import com.example.util.SoundManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * Friendly, visual-first Onboarding Screen that uses an animated character mascot (`BuddyCharacter`)
 * to guide new children through choosing their companion, completing their first hands-on counting
 * & number interaction, and launching into Wonder Island with starter bonus stars.
 *
 * Strictly zero emojis — all visuals are rendered with vector graphics and Compose Canvas.
 */
@Composable
fun OnboardingScreen(
    selectedCharacter: BuddyCharacter = BuddyCharacter.KOMODO,
    selectedAgeGroup: AgeGroupConfig = AgeGroupConfig.JUNIOR,
    languageCode: String = "id",
    isSoundMuted: Boolean = false,
    onSelectCharacter: (BuddyCharacter) -> Unit = {},
    onSelectAgeGroup: (AgeGroupConfig) -> Unit = {},
    onCompleteOnboarding: (earnedStarterStars: Int) -> Unit = {},
    onSkipOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var mascotHighFives by rememberSaveable { mutableIntStateOf(0) }
    var tappedStarTokens by remember { mutableStateOf(setOf<Int>()) }
    var selectedAnswerNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    var isPracticeCompleted by rememberSaveable { mutableStateOf(false) }

    val totalSteps = 3

    BackHandler(enabled = true) {
        if (currentStep > 0) {
            SoundManager.playButtonClick(!isSoundMuted)
            currentStep -= 1
        } else {
            onSkipOnboarding()
        }
    }

    val mascotState = when (currentStep) {
        0 -> if (mascotHighFives > 0) BuddyCharacterState.HAPPY else BuddyCharacterState.IDLE
        1 -> if (isPracticeCompleted) BuddyCharacterState.HAPPY else BuddyCharacterState.THINKING
        else -> BuddyCharacterState.HAPPY
    }

    val mascotSpeechText = remember(currentStep, selectedCharacter, languageCode, mascotHighFives, tappedStarTokens.size, isPracticeCompleted) {
        onboardingMascotSpeech(
            step = currentStep,
            character = selectedCharacter,
            languageCode = languageCode,
            highFives = mascotHighFives,
            tappedStarsCount = tappedStarTokens.size,
            practiceDone = isPracticeCompleted
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        WonderQuestDesignSystem.Colors.SkyBlueSoft.copy(alpha = 0.65f),
                        MaterialTheme.colorScheme.background,
                        WonderQuestDesignSystem.Colors.SunshineSoft.copy(alpha = 0.45f)
                    )
                )
            )
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Visual Step Progress Bar & Skip Button
                OnboardingTopHeader(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    languageCode = languageCode,
                    onSkip = {
                        SoundManager.playButtonClick(!isSoundMuted)
                        onSkipOnboarding()
                    }
                )

                // Hero Character Mascot Guide Card (Persistent across steps with animated state)
                OnboardingMascotStageCard(
                    character = selectedCharacter,
                    mascotState = mascotState,
                    speechText = mascotSpeechText,
                    highFivesCount = mascotHighFives,
                    showSparkles = isPracticeCompleted || currentStep == 2 || mascotHighFives > 0,
                    languageCode = languageCode,
                    onTapMascot = {
                        mascotHighFives += 1
                        SoundManager.playCorrectAnswer(!isSoundMuted)
                    }
                )

                // Step Content with Smooth Horizontal Animated Transition
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(tween(260)) { it / 3 } + fadeIn(tween(220)) + scaleIn(initialScale = 0.95f))
                                .togetherWith(slideOutHorizontally(tween(220)) { -it / 3 } + fadeOut(tween(180)))
                        } else {
                            (slideInHorizontally(tween(260)) { -it / 3 } + fadeIn(tween(220)) + scaleIn(initialScale = 0.95f))
                                .togetherWith(slideOutHorizontally(tween(220)) { it / 3 } + fadeOut(tween(180)))
                        }
                    },
                    label = "onboardingStepTransition"
                ) { step ->
                    when (step) {
                        0 -> OnboardingStepMeetBuddyAndAge(
                            selectedCharacter = selectedCharacter,
                            selectedAgeGroup = selectedAgeGroup,
                            languageCode = languageCode,
                            onSelectCharacter = { buddy ->
                                SoundManager.playButtonClick(!isSoundMuted)
                                onSelectCharacter(buddy)
                            },
                            onSelectAgeGroup = { age ->
                                SoundManager.playButtonClick(!isSoundMuted)
                                onSelectAgeGroup(age)
                            }
                        )

                        1 -> OnboardingStepInteractiveFirstTry(
                            tappedStarTokens = tappedStarTokens,
                            selectedAnswerNumber = selectedAnswerNumber,
                            isPracticeCompleted = isPracticeCompleted,
                            languageCode = languageCode,
                            onTapStarToken = { tokenIndex ->
                                val updated = if (tokenIndex in tappedStarTokens) {
                                    tappedStarTokens
                                } else {
                                    tappedStarTokens + tokenIndex
                                }
                                tappedStarTokens = updated
                                SoundManager.playTokenCount(updated.size, !isSoundMuted)
                                if (updated.size == 3 && selectedAnswerNumber == 3) {
                                    isPracticeCompleted = true
                                }
                            },
                            onSelectAnswerNumber = { number ->
                                selectedAnswerNumber = number
                                if (number == 3) {
                                    // Auto-light all 3 stars if child directly taps 3
                                    tappedStarTokens = setOf(0, 1, 2)
                                    if (!isPracticeCompleted) {
                                        isPracticeCompleted = true
                                        SoundManager.playLevelComplete(!isSoundMuted)
                                    } else {
                                        SoundManager.playCorrectAnswer(!isSoundMuted)
                                    }
                                } else {
                                    SoundManager.playGentleRetry(!isSoundMuted)
                                }
                            }
                        )

                        else -> OnboardingStepReadyToExplore(
                            selectedCharacter = selectedCharacter,
                            selectedAgeGroup = selectedAgeGroup,
                            isPracticeCompleted = isPracticeCompleted,
                            languageCode = languageCode
                        )
                    }
                }

                // Bottom Big Kid-Friendly Step Navigation Buttons
                OnboardingBottomControls(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    isPracticeCompleted = isPracticeCompleted,
                    languageCode = languageCode,
                    onPrevious = {
                        if (currentStep > 0) {
                            SoundManager.playButtonClick(!isSoundMuted)
                            currentStep -= 1
                        }
                    },
                    onNext = {
                        if (currentStep < totalSteps - 1) {
                            SoundManager.playButtonClick(!isSoundMuted)
                            currentStep += 1
                        }
                    },
                    onFinish = {
                        SoundManager.playLevelComplete(!isSoundMuted)
                        val starterStars = if (isPracticeCompleted) 3 else 1
                        onCompleteOnboarding(starterStars)
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingTopHeader(
    currentStep: Int,
    totalSteps: Int,
    languageCode: String,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_progress_indicator"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (idx in 0 until totalSteps) {
                val isCurrent = idx == currentStep
                val isDone = idx < currentStep
                val pillWidth by animateFloatAsState(
                    targetValue = if (isCurrent) 42f else 18f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "stepPillWidth_$idx"
                )
                val pillColor by animateColorAsState(
                    targetValue = when {
                        isCurrent -> WonderQuestDesignSystem.Colors.SkyBlueInteractive
                        isDone -> WonderQuestDesignSystem.Colors.MintInteractive
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                    label = "stepPillColor_$idx"
                )
                Box(
                    modifier = Modifier
                        .width(pillWidth.dp)
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(pillColor)
                        .testTag("onboarding_step_dot_$idx")
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${currentStep + 1} / $totalSteps",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Friendly Skip Pill Button (minimum 48.dp touch target height)
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .height(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onSkip)
                .testTag("onboarding_skip_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = onboardingSkipLabel(languageCode),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = onboardingSkipLabel(languageCode),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingMascotStageCard(
    character: BuddyCharacter,
    mascotState: BuddyCharacterState,
    speechText: String,
    highFivesCount: Int,
    showSparkles: Boolean,
    languageCode: String,
    onTapMascot: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.5.dp, Color(character.primaryColorHex).copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_mascot_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Illustrated Island Stage with Interactive Mascot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_mascot_guide")
            ) {
                WonderIslandIllustratedBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(178.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(onClick = onTapMascot)
                        .testTag("onboarding_mascot_tap_area")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showSparkles) {
                            StarSparkleCelebrationOverlay(
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        com.example.ui.components.BuddyCharacter(
                            state = mascotState,
                            character = character,
                            languageCode = languageCode,
                            showMoodPill = true,
                            onTap = onTapMascot,
                            modifier = Modifier.size(152.dp)
                        )

                    // Top-right High-Five Interactive Counter Badge
                    Surface(
                        shape = CircleShape,
                        color = WonderQuestDesignSystem.Colors.SunshineYellow,
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .testTag("onboarding_highfive_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = Color(0xFF30334A),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (highFivesCount > 0) {
                                    "${onboardingHighFiveLabel(languageCode)} x$highFivesCount"
                                } else {
                                    onboardingTapMascotHint(languageCode)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF30334A)
                            )
                        }
                    }
                }
                }
            }

            // Friendly Mascot Speech Bubble
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = WonderQuestDesignSystem.Colors.SkyBlueSoft.copy(alpha = 0.7f),
                border = BorderStroke(
                    width = 2.dp,
                    color = Color(character.primaryColorHex).copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_speech_bubble")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(character.primaryColorHex),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = speechText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepMeetBuddyAndAge(
    selectedCharacter: BuddyCharacter,
    selectedAgeGroup: AgeGroupConfig,
    languageCode: String,
    onSelectCharacter: (BuddyCharacter) -> Unit,
    onSelectAgeGroup: (AgeGroupConfig) -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, WonderQuestDesignSystem.Colors.SkyBlueInteractive.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_step_0")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Pick Your Mascot Companion
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = WonderQuestDesignSystem.Colors.CoralInteractive,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = onboardingPickBuddyTitle(languageCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BuddyCharacter.entries.forEach { buddy ->
                    val isSelected = buddy == selectedCharacter
                    val accent = Color(buddy.primaryColorHex)
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.04f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "buddyCardScale_${buddy.id}"
                    )

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) accent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .scale(scale)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onSelectCharacter(buddy) }
                            .testTag("onboarding_buddy_${buddy.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AnimatedBuddyCanvas(
                                character = buddy,
                                reaction = if (isSelected) BuddyReaction.CELEBRATING else BuddyReaction.IDLE,
                                modifier = Modifier.size(52.dp)
                            )
                            Text(
                                text = buddy.localizedName(languageCode).substringBefore(" "),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Section 2: Pick Your Explorer Age Group
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = WonderQuestDesignSystem.Colors.SkyBlueInteractive,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = onboardingPickAgeTitle(languageCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AgeGroupConfig.entries.forEach { ageGroup ->
                    val isSelected = ageGroup == selectedAgeGroup
                    val accent = when (ageGroup) {
                        AgeGroupConfig.EARLY -> WonderQuestDesignSystem.Colors.MintInteractive
                        AgeGroupConfig.JUNIOR -> WonderQuestDesignSystem.Colors.SkyBlueInteractive
                        AgeGroupConfig.SENIOR -> WonderQuestDesignSystem.Colors.PurpleInteractive
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) accent else accent.copy(alpha = 0.14f),
                        border = BorderStroke(
                            width = if (isSelected) 2.5.dp else 1.5.dp,
                            color = accent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectAgeGroup(ageGroup) }
                            .testTag("onboarding_age_${ageGroup.code}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = ageGroup.code,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else accent
                            )
                            Text(
                                text = onboardingAgeUnitLabel(languageCode),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White.copy(alpha = 0.92f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepInteractiveFirstTry(
    tappedStarTokens: Set<Int>,
    selectedAnswerNumber: Int?,
    isPracticeCompleted: Boolean,
    languageCode: String,
    onTapStarToken: (Int) -> Unit,
    onSelectAnswerNumber: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 2.5.dp,
            color = if (isPracticeCompleted) {
                WonderQuestDesignSystem.Colors.MintInteractive
            } else {
                WonderQuestDesignSystem.Colors.SunshineGold
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_step_1")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Visual Instruction Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = WonderQuestDesignSystem.Colors.SunshineGold,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = onboardingPracticeTitle(languageCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            // 3 Interactive Star Medallions to Tap & Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0..2) {
                    val isLit = index in tappedStarTokens
                    val tokenScale by animateFloatAsState(
                        targetValue = if (isLit) 1.12f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "practiceStarScale_$index"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isLit) {
                                WonderQuestDesignSystem.Colors.SunshineYellow.copy(alpha = 0.28f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            },
                            border = BorderStroke(
                                width = if (isLit) 3.dp else 2.dp,
                                color = if (isLit) {
                                    WonderQuestDesignSystem.Colors.SunshineGold
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            modifier = Modifier
                                .size(82.dp)
                                .scale(tokenScale)
                                .clip(CircleShape)
                                .clickable { onTapStarToken(index) }
                                .testTag("onboarding_star_token_$index")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                OnboardingStarTokenCanvas(
                                    isLit = isLit,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }

                        // Count number badge under each star
                        Surface(
                            shape = CircleShape,
                            color = if (isLit) {
                                WonderQuestDesignSystem.Colors.MintInteractive
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = if (isLit) "${index + 1}" else "?",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLit) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = onboardingPickNumberPrompt(languageCode),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // 3 Big Number Answer Buttons (1, 2, 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(1, 2, 3).forEachIndexed { idx, number ->
                    val isSelected = selectedAnswerNumber == number
                    val isCorrectChoice = number == 3
                    val buttonColor = when {
                        isSelected && isCorrectChoice -> WonderQuestDesignSystem.Colors.MintInteractive
                        isSelected && !isCorrectChoice -> WonderQuestDesignSystem.Colors.CoralAccent
                        else -> WonderQuestDesignSystem.Colors.SkyBlueInteractive
                    }
                    Button(
                        onClick = { onSelectAnswerNumber(number) },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .testTag("onboarding_answer_option_$idx")
                    ) {
                        Text(
                            text = number.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Celebratory Success Banner when the child solves their first interaction!
            AnimatedVisibility(visible = isPracticeCompleted) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = WonderQuestDesignSystem.Colors.MintSoft,
                    border = BorderStroke(2.dp, WonderQuestDesignSystem.Colors.MintInteractive),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_practice_success_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WonderQuestDesignSystem.Colors.MintInteractive,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = onboardingPracticeSuccessTitle(languageCode),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = WonderQuestDesignSystem.Colors.MintInteractive
                            )
                            Text(
                                text = onboardingPracticeSuccessSubtitle(languageCode),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = WonderQuestDesignSystem.Colors.SunshineYellow
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF30334A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "+3",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF30334A)
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
private fun OnboardingStepReadyToExplore(
    selectedCharacter: BuddyCharacter,
    selectedAgeGroup: AgeGroupConfig,
    isPracticeCompleted: Boolean,
    languageCode: String
) {
    val starterStars = if (isPracticeCompleted) 3 else 1
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.5.dp, WonderQuestDesignSystem.Colors.PurpleInteractive.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_step_2")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = onboardingReadyTitle(languageCode),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Custom Illustrated Map & Trophy Preview Canvas
            OnboardingIslandMapPreviewCanvas(
                accentColor = Color(selectedCharacter.primaryColorHex),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(WonderQuestDesignSystem.Colors.SkyBlueSoft.copy(alpha = 0.5f))
                    .testTag("onboarding_island_preview_card")
            )

            // Summary Pills Row (Selected Mascot + Age Group + Starter Bonus Stars)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(selectedCharacter.primaryColorHex).copy(alpha = 0.16f),
                    border = BorderStroke(1.5.dp, Color(selectedCharacter.primaryColorHex)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = Color(selectedCharacter.primaryColorHex),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedCharacter.localizedName(languageCode).substringBefore(" "),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WonderQuestDesignSystem.Colors.SkyBlueSoft,
                    border = BorderStroke(1.5.dp, WonderQuestDesignSystem.Colors.SkyBlueInteractive),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = WonderQuestDesignSystem.Colors.SkyBlueInteractive,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${selectedAgeGroup.code} ${onboardingAgeUnitLabel(languageCode)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WonderQuestDesignSystem.Colors.SunshineSoft,
                    border = BorderStroke(1.5.dp, WonderQuestDesignSystem.Colors.SunshineGold),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("onboarding_starter_reward_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = WonderQuestDesignSystem.Colors.SunshineGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+$starterStars",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF30334A)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingBottomControls(
    currentStep: Int,
    totalSteps: Int,
    isPracticeCompleted: Boolean,
    languageCode: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onPrevious,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .weight(0.42f)
                    .height(58.dp)
                    .testTag("onboarding_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = onboardingBackLabel(languageCode),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (currentStep < totalSteps - 1) {
            val highlightNext = currentStep == 0 || isPracticeCompleted
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (highlightNext) {
                        WonderQuestDesignSystem.Colors.MintInteractive
                    } else {
                        WonderQuestDesignSystem.Colors.SkyBlueInteractive
                    },
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(if (currentStep > 0) 0.58f else 1f)
                    .height(58.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = onboardingNextLabel(languageCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WonderQuestDesignSystem.Colors.MintInteractive,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(0.65f)
                    .height(60.dp)
                    .testTag("onboarding_finish_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = onboardingStartAdventureLabel(languageCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun OnboardingStarTokenCanvas(
    isLit: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "onboardingStarPulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starPulseScale"
    )
    Canvas(modifier = modifier.scale(if (isLit) pulse else 1f)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension * 0.46f
        val innerR = outerR * 0.45f

        val starPath = Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = Math.toRadians((i * 36.0) - 90.0)
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()

        if (isLit) {
            drawCircle(
                color = Color(0xFFFFD95A).copy(alpha = 0.35f),
                radius = outerR * 1.12f,
                center = Offset(cx, cy)
            )
            drawPath(
                path = starPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFE57F), Color(0xFFFFB300))
                )
            )
            drawPath(
                path = starPath,
                color = Color(0xFFF57F17),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
            // Happy star eyes
            drawCircle(
                color = Color(0xFF30334A),
                radius = outerR * 0.08f,
                center = Offset(cx - outerR * 0.16f, cy - outerR * 0.05f)
            )
            drawCircle(
                color = Color(0xFF30334A),
                radius = outerR * 0.08f,
                center = Offset(cx + outerR * 0.16f, cy - outerR * 0.05f)
            )
        } else {
            drawPath(
                path = starPath,
                color = Color(0xFFB0BEC5).copy(alpha = 0.55f)
            )
            drawPath(
                path = starPath,
                color = Color(0xFF78909C),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun OnboardingIslandMapPreviewCanvas(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Rolling green island hills
        drawOval(
            color = Color(0xFF81C784).copy(alpha = 0.65f),
            topLeft = Offset(-w * 0.1f, h * 0.55f),
            size = Size(w * 0.7f, h * 0.7f)
        )
        drawOval(
            color = Color(0xFF66BB6A).copy(alpha = 0.75f),
            topLeft = Offset(w * 0.35f, h * 0.5f),
            size = Size(w * 0.75f, h * 0.75f)
        )

        // Dotted adventure trail connecting 3 island stops
        val stop1 = Offset(w * 0.2f, h * 0.56f)
        val stop2 = Offset(w * 0.5f, h * 0.38f)
        val stop3 = Offset(w * 0.8f, h * 0.52f)

        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = stop1,
            end = stop2,
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = stop2,
            end = stop3,
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Stop 1: Math Safari node
        drawCircle(color = Color.White, radius = 24f, center = stop1)
        drawCircle(color = Color(0xFF2ECC71), radius = 18f, center = stop1)

        // Stop 2: Daily Challenge Star node
        drawCircle(color = Color.White, radius = 28f, center = stop2)
        drawCircle(color = accentColor, radius = 22f, center = stop2)

        // Stop 3: Golden Trophy node
        drawRoundRect(
            color = Color(0xFFFFB300),
            topLeft = Offset(stop3.x - 22f, stop3.y - 22f),
            size = Size(44f, 44f),
            cornerRadius = CornerRadius(12f, 12f)
        )
    }
}

private fun onboardingMascotSpeech(
    step: Int,
    character: BuddyCharacter,
    languageCode: String,
    highFives: Int,
    tappedStarsCount: Int,
    practiceDone: Boolean
): String {
    val name = character.localizedName(languageCode)
    return when (step) {
        0 -> when (languageCode) {
            "id" -> if (highFives > 0) {
                "Tos yang hebat! Aku $name. Pilih teman dan kelompok usiamu, lalu ketuk Lanjut!"
            } else {
                "Halo Penjelajah! Aku $name. Ketuk gambarku untuk tos, lalu pilih teman favoritmu!"
            }
            "es" -> if (highFives > 0) {
                "Choca esos cinco! Soy $name. Elige tu amigo y tu edad para continuar."
            } else {
                "Hola Explorador! Soy $name. Tocame para saludar y elige a tu companero favorito!"
            }
            else -> if (highFives > 0) {
                "Awesome high-five! I am $name. Pick your buddy and age group, then tap Next!"
            } else {
                "Hello Explorer! I am $name. Tap me for a high-five and pick your favorite buddy!"
            }
        }
        1 -> when (languageCode) {
            "id" -> when {
                practiceDone -> "Luar biasa! Kamu berhasil menghitung 3 Bintang Ajaib dan mendapat hadiah bintang pertamamu!"
                tappedStarsCount in 1..2 -> "Bagus! Kamu sudah mengetuk $tappedStarsCount bintang. Ketuk semua 3 bintang lalu pilih angka 3!"
                else -> "Ayo coba bermain! Ketuk 3 Bintang Ajaib di bawah satu per satu, lalu pilih angka 3!"
            }
            "es" -> when {
                practiceDone -> "Fantastico! Contaste las 3 Estrellas Magicas y ganaste tus primeras estrellas!"
                tappedStarsCount in 1..2 -> "Genial! Tocaste $tappedStarsCount estrellas. Toca las 3 y elige el numero 3!"
                else -> "Vamos a probar! Toca las 3 Estrellas Magicas una por una y elige el numero 3!"
            }
            else -> when {
                practiceDone -> "Amazing job! You counted all 3 Magic Stars and earned your first bonus stars!"
                tappedStarsCount in 1..2 -> "Great! You tapped $tappedStarsCount stars. Tap all 3 stars and pick number 3!"
                else -> "Let's try playing! Tap the 3 Magic Stars below one by one, then choose number 3!"
            }
        }
        else -> when (languageCode) {
            "id" -> "Kamu sudah siap menjelajahi Pulau Keajaiban bersama $name! Ketuk tombol hijau untuk mulai bermain!"
            "es" -> "Estas listo para explorar la Isla Maravilla con $name! Toca el boton verde para empezar!"
            else -> "You are ready to explore Wonder Island with $name! Tap the green button to start playing!"
        }
    }
}

private fun onboardingSkipLabel(languageCode: String): String = when (languageCode) {
    "id" -> "Lewati"
    "es" -> "Saltar"
    "fr" -> "Passer"
    else -> "Skip"
}

private fun onboardingBackLabel(languageCode: String): String = when (languageCode) {
    "id" -> "Kembali"
    "es" -> "Atras"
    "fr" -> "Retour"
    else -> "Back"
}

private fun onboardingNextLabel(languageCode: String): String = when (languageCode) {
    "id" -> "Lanjut"
    "es" -> "Siguiente"
    "fr" -> "Suivant"
    else -> "Next"
}

private fun onboardingStartAdventureLabel(languageCode: String): String = when (languageCode) {
    "id" -> "Mulai Petualangan Pulau!"
    "es" -> "Empezar Aventura!"
    "fr" -> "Commencer l'Aventure!"
    else -> "Start Island Adventure!"
}

private fun onboardingHighFiveLabel(languageCode: String): String = when (languageCode) {
    "id" -> "Tos"
    "es" -> "Choca"
    else -> "High-Five"
}

private fun onboardingTapMascotHint(languageCode: String): String = when (languageCode) {
    "id" -> "Ketuk Aku!"
    "es" -> "Tocame!"
    else -> "Tap Me!"
}

private fun onboardingPickBuddyTitle(languageCode: String): String = when (languageCode) {
    "id" -> "1. Pilih Teman Petualanganmu"
    "es" -> "1. Elige tu Companero"
    else -> "1. Pick Your Adventure Buddy"
}

private fun onboardingPickAgeTitle(languageCode: String): String = when (languageCode) {
    "id" -> "2. Pilih Kelompok Usiamu"
    "es" -> "2. Elige tu Grupo de Edad"
    else -> "2. Pick Your Explorer Age"
}

private fun onboardingAgeUnitLabel(languageCode: String): String = when (languageCode) {
    "id" -> "Tahun"
    "es" -> "Anos"
    else -> "Years"
}

private fun onboardingPracticeTitle(languageCode: String): String = when (languageCode) {
    "id" -> "Ketuk & Hitung 3 Bintang Ajaib!"
    "es" -> "Toca y Cuenta 3 Estrellas Magicas!"
    else -> "Tap & Count 3 Magic Stars!"
}

private fun onboardingPickNumberPrompt(languageCode: String): String = when (languageCode) {
    "id" -> "Berapa jumlah bintang di atas? Ketuk angkanya:"
    "es" -> "Cuantas estrellas hay arriba? Toca el numero:"
    else -> "How many stars are above? Tap the number:"
}

private fun onboardingPracticeSuccessTitle(languageCode: String): String = when (languageCode) {
    "id" -> "Hebat! Kamu Berhasil!"
    "es" -> "Genial! Lo Lograste!"
    else -> "Awesome! You Did It!"
}

private fun onboardingPracticeSuccessSubtitle(languageCode: String): String = when (languageCode) {
    "id" -> "Kamu mendapatkan +3 Bintang Pemula untuk petualanganmu!"
    "es" -> "Ganaste +3 Estrellas Iniciales para tu aventura!"
    else -> "You earned +3 Starter Stars for your adventure!"
}

private fun onboardingReadyTitle(languageCode: String): String = when (languageCode) {
    "id" -> "Siap Menjelajahi Pulau Keajaiban!"
    "es" -> "Listo para Explorar la Isla Maravilla!"
    else -> "Ready to Explore Wonder Island!"
}
