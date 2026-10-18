package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AgeBracket
import com.example.data.BuddyCharacter
import com.example.data.ChildProfile
import com.example.data.LocalizedUiStrings
import com.example.data.MiniGameType
import com.example.data.WonderQuestStrings
import com.example.ui.components.AnimatedBuddyCanvas

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IslandHomeScreen(
    profile: ChildProfile,
    strings: LocalizedUiStrings,
    isOnline: Boolean,
    onSelectAgeBracket: (AgeBracket) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onToggleDarkMode: () -> Unit,
    onStartMiniGame: (MiniGameType, Int) -> Unit,
    onNavigateToBuddies: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBuddy = BuddyCharacter.fromId(profile.selectedCharacter)
    val activeAgeBracket = AgeBracket.fromCode(profile.ageGroup)
    var buddyTapCount by remember { mutableIntStateOf(1) }
    val nextSuggestedLevel = ((profile.completedLevelsCount % 10L) + 1L).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("island_home_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Header Bar with Cloud/Offline Status, Language Pills, and Dark Mode Toggle
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WonderQuest",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "👋 ${profile.childName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (isOnline) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("sync_status_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isOnline) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOnline) strings.cloudSyncedStatus else strings.offlineReadyStatus,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isOnline) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleDarkMode,
                            modifier = Modifier.testTag("home_dark_mode_button")
                        ) {
                            Icon(
                                imageVector = if (profile.darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = strings.darkModeLabel,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Classroom Multilingual Bar
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WonderQuestStrings.supportedLanguages.forEach { (code, name) ->
                        FilterChip(
                            selected = profile.languageCode == code,
                            onClick = { onSelectLanguage(code) },
                            label = { Text(name) },
                            modifier = Modifier.testTag("lang_chip_$code")
                        )
                    }
                }
            }
        }

        // 2. Interactive Buddy Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .testTag("hero_buddy_card"),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(activeBuddy.primaryColorHex).copy(alpha = 0.20f),
                                    MaterialTheme.colorScheme.surface,
                                    Color(activeBuddy.accentColorHex).copy(alpha = 0.18f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = Color(activeBuddy.primaryColorHex).copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable { onNavigateToBuddies() }
                                ) {
                                    Text(
                                        text = "✨ ${activeBuddy.displayName(profile.languageCode)} • ${activeBuddy.role(profile.languageCode)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                AnimatedContent(
                                    targetState = buddyTapCount,
                                    label = "buddy_speech"
                                ) { count ->
                                    val cheerText = if (count % 2 == 1) {
                                        activeBuddy.cheer(profile.languageCode)
                                    } else {
                                        strings.tapBuddyHint
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = "💬 \"$cheerText\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            AnimatedBuddyCanvas(
                                character = activeBuddy,
                                interactionTriggerCount = buddyTapCount,
                                onBuddyTapped = { buddyTapCount++ },
                                canvasSize = 135.dp,
                                testTagName = "home_buddy_canvas"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gamified Stats Strip (Stars, Completed Levels, Streak, Unlocked Badges)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatPillCard(
                                icon = {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                value = "${profile.totalStars}",
                                label = strings.starsLabel,
                                modifier = Modifier.weight(1f)
                            )
                            StatPillCard(
                                icon = {
                                    Icon(
                                        Icons.Filled.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                value = "${profile.completedLevelsCount}",
                                label = strings.levelsLabel,
                                modifier = Modifier.weight(1f)
                            )
                            StatPillCard(
                                icon = {
                                    Icon(
                                        Icons.Filled.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B35),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                value = "${profile.streakDays}d",
                                label = strings.streakLabel,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 3. Age Bracket Difficulty Selector (4-6, 7-9, 10-13)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = strings.chooseAgeGroup,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AgeBracket.entries.forEach { bracket ->
                        val isSelected = bracket == activeAgeBracket
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectAgeBracket(bracket) },
                            label = {
                                Text(
                                    text = bracket.label(profile.languageCode),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("age_chip_${bracket.code}")
                        )
                    }
                }
            }
        }

        // 4. Educational Mini-Games Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.chooseMiniGame,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${strings.mathPointsLabel}: ${profile.mathScore} • ${strings.logicPointsLabel}: ${profile.logicScore}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 5. Mini-Game Cards
        items(MiniGameType.entries) { gameType ->
            MiniGameQuestCard(
                gameType = gameType,
                languageCode = profile.languageCode,
                strings = strings,
                suggestedLevel = nextSuggestedLevel,
                onPlayClicked = { onStartMiniGame(gameType, nextSuggestedLevel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            )
        }
    }
}

@Composable
private fun StatPillCard(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                icon()
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MiniGameQuestCard(
    gameType: MiniGameType,
    languageCode: String,
    strings: LocalizedUiStrings,
    suggestedLevel: Int,
    onPlayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(gameType.colorHex)
    val iconEmoji = when (gameType) {
        MiniGameType.MATH_SAFARI -> "🦁"
        MiniGameType.PATTERN_PORTAL -> "🔮"
        MiniGameType.NUMBER_BRIDGE -> "🌉"
        MiniGameType.LOGIC_MATRIX -> "🧩"
    }

    Card(
        modifier = modifier
            .testTag("minigame_card_${gameType.id}")
            .clickable { onPlayClicked() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconEmoji,
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = gameType.title(languageCode),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = accent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${strings.levelPrefix} $suggestedLevel",
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = gameType.description(languageCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onPlayClicked,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("play_button_${gameType.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.playNowButton,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}
