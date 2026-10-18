package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.ALL_REWARD_BADGES
import com.example.data.BuddyCharacter
import com.example.data.ChildProfile
import com.example.data.LocalizedUiStrings
import com.example.data.RewardBadge
import com.example.ui.components.AnimatedBuddyCanvas

@Composable
fun BuddiesPlaygroundScreen(
    profile: ChildProfile,
    strings: LocalizedUiStrings,
    onSelectBuddy: (BuddyCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBuddy = BuddyCharacter.fromId(profile.selectedCharacter)
    var inspectedBuddy by remember(activeBuddy) { mutableStateOf(activeBuddy) }
    var animCounter by remember { mutableIntStateOf(1) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("buddies_playground_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.buddiesScreenTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.buddiesScreenSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Interactive Stage Card
        item {
            val primaryColor = Color(inspectedBuddy.primaryColorHex)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedBuddyCanvas(
                        character = inspectedBuddy,
                        interactionTriggerCount = animCounter,
                        onBuddyTapped = { animCounter++ },
                        canvasSize = 185.dp,
                        testTagName = "playground_stage_buddy"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = inspectedBuddy.displayName(profile.languageCode),
                        style = MaterialTheme.typography.headlineMedium,
                        color = primaryColor
                    )
                    Text(
                        text = inspectedBuddy.role(profile.languageCode),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = primaryColor.copy(alpha = 0.14f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💬 \"${inspectedBuddy.cheer(profile.languageCode)}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Animation Trigger Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { animCounter++ },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("trigger_jump_button")
                        ) {
                            Icon(Icons.Filled.Celebration, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.triggerDanceButton)
                        }
                        FilledTonalButton(
                            onClick = { animCounter += 2 },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("trigger_sparkle_button")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.triggerSparkleButton)
                        }
                        FilledTonalButton(
                            onClick = { animCounter += 3 },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("trigger_wave_button")
                        ) {
                            Icon(Icons.Filled.WavingHand, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.triggerHighFiveButton)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val isCurrentBuddy = inspectedBuddy.id == profile.selectedCharacter
                    Button(
                        onClick = {
                            onSelectBuddy(inspectedBuddy)
                            animCounter++
                        },
                        enabled = !isCurrentBuddy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("select_buddy_button"),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCurrentBuddy) strings.selectedBuddyBadge else strings.selectBuddyButton,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Character Roster List
        items(BuddyCharacter.entries) { character ->
            val isSelected = character.id == profile.selectedCharacter
            val color = Color(character.primaryColorHex)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .testTag("buddy_roster_card_${character.id}")
                    .clickable {
                        inspectedBuddy = character
                        animCounter++
                    },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = if (inspectedBuddy == character) {
                        color.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = character.displayName(profile.languageCode),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = character.role(profile.languageCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Surface(
                            color = color,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "★ ${strings.selectedBuddyBadge}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgesTrophyScreen(
    profile: ChildProfile,
    strings: LocalizedUiStrings,
    modifier: Modifier = Modifier
) {
    var selectedBadge by remember { mutableStateOf<RewardBadge?>(ALL_REWARD_BADGES.first()) }
    var badgePulseCount by remember { mutableIntStateOf(1) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("badges_trophy_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.badgesScreenTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${profile.unlockedBadges.size} / ${ALL_REWARD_BADGES.size} ${strings.badgeUnlockedLabel} • ${strings.badgesScreenSubtitle}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Interactive Spotlight Card for Selected Badge
        item {
            AnimatedVisibility(visible = selectedBadge != null) {
                val badge = selectedBadge ?: return@AnimatedVisibility
                val isUnlocked = profile.unlockedBadges.contains(badge.id)
                val badgeColor = Color(badge.colorHex)
                val pulseScale by animateFloatAsState(
                    targetValue = if (badgePulseCount % 2 == 0) 1.12f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "badge_pulse"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .clickable { badgePulseCount++ },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = badgeColor.copy(alpha = 0.16f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(badgeColor.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badge.iconEmoji,
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = badge.title(profile.languageCode),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = badge.description(profile.languageCode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = if (isUnlocked) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (isUnlocked) "✨ ${strings.badgeUnlockedLabel}" else "🔒 ${strings.badgeLockedLabel}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        items(ALL_REWARD_BADGES) { badge ->
            val unlocked = profile.unlockedBadges.contains(badge.id)
            val color = Color(badge.colorHex)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .testTag("badge_card_${badge.id}")
                    .clickable {
                        selectedBadge = badge
                        badgePulseCount++
                    },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (unlocked) 3.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (unlocked) color.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge.iconEmoji,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = badge.title(profile.languageCode),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = badge.description(profile.languageCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        imageVector = if (unlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                        contentDescription = if (unlocked) strings.badgeUnlockedLabel else strings.badgeLockedLabel,
                        tint = if (unlocked) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
