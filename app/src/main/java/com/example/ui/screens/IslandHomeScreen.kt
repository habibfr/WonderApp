package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import com.example.ui.components.PlayfulSoundEffects
import com.example.ui.components.TokenIllustrationCanvas
import com.example.ui.components.WonderIslandIllustratedBanner
import com.example.ui.components.WonderQuestIcon
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgeGroupConfig
import com.example.data.BuddyCharacter
import com.example.data.BuddyReaction
import com.example.data.GamePlayMode
import com.example.data.MiniGameType
import com.example.data.WonderQuestStrings
import com.example.ui.WonderQuestUiState
import com.example.ui.components.AnimatedBuddyCanvas
import com.example.ui.components.AnimatedIslandPanoramaCanvas
import com.example.ui.components.DailyChallenges

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IslandHomeScreen(
    uiState: WonderQuestUiState,
    onSelectAgeGroup: (AgeGroupConfig) -> Unit,
    onSelectPlayMode: (GamePlayMode) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onBuddyTap: (BuddyReaction) -> Unit,
    onStartMiniGame: (MiniGameType) -> Unit,
    onToggleSoundMute: () -> Unit = {},
    onDailyChallengeReward: (bonusStars: Int, stickerBadgeId: String, isMathTask: Boolean) -> Unit = { _, _, _ -> },
    onStartOnboarding: () -> Unit = {},
    onRequestCloudSignIn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = uiState.strings
    val profile = uiState.profile
    val lang = profile.languageCode
    val buddy = BuddyCharacter.fromId(profile.selectedCharacter)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 270.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("island_home_grid"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Cloud Sync / Guest Device Storage Banner + Language & Theme Controls
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StorageAndSyncTopBanner(
                    isCloudConnected = uiState.isCloudConnected,
                    guestModeBanner = strings.guestModeBanner,
                    syncedBanner = strings.syncedWithCloudBanner,
                    syncNowLabel = strings.syncNowButton,
                    onRequestCloudSignIn = onRequestCloudSignIn
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        WonderQuestStrings.supportedLanguages.forEach { (code, label) ->
                            FilterChip(
                                selected = lang == code,
                                onClick = { onSelectLanguage(code) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (lang == code) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.testTag("lang_chip_$code")
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onToggleSoundMute,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .testTag("toggle_sound_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isSoundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = strings.soundEffectsLabel,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        IconButton(
                            onClick = {
                                PlayfulSoundEffects.playTap(!uiState.isSoundMuted)
                                onToggleDarkMode(!profile.darkMode)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .testTag("toggle_dark_mode_button")
                        ) {
                            Icon(
                                imageVector = if (profile.darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = strings.darkModeLabel,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }

        // 2. Living Animated Panorama Hero Card with Interactive Buddy & Quick Props
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_buddy_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Animated Sky, Rainbow Bridge, Clouds, Tropical Hills & Waves
                    AnimatedIslandPanoramaCanvas(
                        isDarkMode = profile.darkMode,
                        modifier = Modifier
                            .matchParentSize()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                                    )
                                )
                            )
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WonderIslandIllustratedBanner(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(104.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(132.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable {
                                        val nextReaction = when (uiState.buddyReaction) {
                                            BuddyReaction.IDLE -> BuddyReaction.DANCING
                                            BuddyReaction.DANCING -> BuddyReaction.SPARKLE
                                            BuddyReaction.SPARKLE -> BuddyReaction.EATING_TREAT
                                            else -> BuddyReaction.HIGH_FIVE
                                        }
                                        onBuddyTap(nextReaction)
                                    }
                                    .testTag("island_buddy_canvas_box"),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedBuddyCanvas(
                                    character = buddy,
                                    reaction = uiState.buddyReaction,
                                    modifier = Modifier.size(126.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${buddy.localizedName(lang)} & ${profile.childName}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.tapBuddyHint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Buddy Happiness & Energy meter
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = { (uiState.buddyHappiness / 100f).coerceIn(0.1f, 1f) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = Color(0xFFE91E63),
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Text(
                                        text = "${uiState.buddyHappiness}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Interactive Buddy Action Buttons right on the Island!
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InteractiveReactionChip(
                                icon = Icons.Default.Restaurant,
                                label = strings.feedTreatButton,
                                onClick = { onBuddyTap(BuddyReaction.EATING_TREAT) },
                                tag = "home_feed_treat_chip"
                            )
                            InteractiveReactionChip(
                                icon = Icons.Default.AutoAwesome,
                                label = strings.magicWandButton,
                                onClick = { onBuddyTap(BuddyReaction.MAGIC_WAND) },
                                tag = "home_magic_wand_chip"
                            )
                            InteractiveReactionChip(
                                icon = Icons.Default.Celebration,
                                label = strings.triggerDanceButton,
                                onClick = { onBuddyTap(BuddyReaction.DANCING) },
                                tag = "home_dance_chip"
                            )
                            InteractiveReactionChip(
                                icon = Icons.Default.PanTool,
                                label = strings.triggerHighFiveButton,
                                onClick = { onBuddyTap(BuddyReaction.HIGH_FIVE) },
                                tag = "home_high_five_chip"
                            )
                            InteractiveReactionChip(
                                icon = Icons.Default.AutoAwesome,
                                label = if (lang == "id") "Panduan Bermain" else "How to Play",
                                onClick = onStartOnboarding,
                                tag = "island_onboarding_guide_button"
                            )
                        }

                        // Stats Row with Icons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatPill(
                                icon = Icons.Default.Star,
                                value = "${profile.totalStars}",
                                label = strings.starsLabel,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.weight(1f).testTag("stat_stars_pill")
                            )
                            StatPill(
                                icon = Icons.Default.EmojiEvents,
                                value = "${profile.completedLevelsCount}",
                                label = strings.levelsLabel,
                                tint = Color(0xFF26A69A),
                                modifier = Modifier.weight(1f).testTag("stat_levels_pill")
                            )
                            StatPill(
                                icon = Icons.Default.LocalFireDepartment,
                                value = "${profile.streakDays}d",
                                label = strings.streakLabel,
                                tint = Color(0xFFFF7043),
                                modifier = Modifier.weight(1f).testTag("stat_streak_pill")
                            )
                        }
                    }
                }
            }
        }

        // 3. Step 1: Age Group Selector (4-6, 7-9, 10-13)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.chooseAgeGroup,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AgeGroupConfig.entries.forEach { group ->
                        val selected = profile.ageGroup == group.code
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectAgeGroup(group) },
                            leadingIcon = {
                                WonderQuestIcon(
                                    iconKey = group.iconKey,
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = group.localizedTitle(lang),
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("age_group_chip_${group.code}")
                        )
                    }
                }
            }
        }

        // 4. Step 2: Interactive Game Mode Selector (Adventure, Star Blitz, Zen Practice, Daily Quest)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.choosePlayMode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GamePlayMode.entries.forEach { mode ->
                        val isSelected = uiState.selectedPlayMode == mode
                        val accent = Color(mode.colorHex)
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.03f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "mode_scale_${mode.id}"
                        )
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) accent else accent.copy(alpha = 0.14f),
                            tonalElevation = if (isSelected) 6.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .scale(scale)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onSelectPlayMode(mode) }
                                .testTag("play_mode_${mode.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                WonderQuestIcon(
                                    iconKey = mode.iconKey,
                                    tint = if (isSelected) Color.White else accent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mode.localizedTitle(lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (mode.bonusStars > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "+${mode.bonusStars}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White.copy(alpha = 0.92f) else accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Color(0xFFFFB300),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4b. DailyChallenges: 3 Small Randomized Math & Logic Tasks with Bonus Stars & Stickers
        item(span = { GridItemSpan(maxLineSpan) }) {
            DailyChallenges(
                ageGroup = uiState.selectedAgeGroup,
                languageCode = lang,
                isSoundMuted = uiState.isSoundMuted,
                onRewardEarned = { bonusStars, stickerBadgeId, isMathTask ->
                    onDailyChallengeReward(bonusStars, stickerBadgeId, isMathTask)
                },
                onAllThreeCompletedBonus = { grandBonusStars, grandBadgeId ->
                    onDailyChallengeReward(grandBonusStars, grandBadgeId, true)
                }
            )
        }

        // 5. Step 3: Section Title for 7 Interactive Mini-Games
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.chooseMiniGame,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(uiState.selectedPlayMode.colorHex).copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WonderQuestIcon(
                            iconKey = uiState.selectedPlayMode.iconKey,
                            tint = Color(uiState.selectedPlayMode.colorHex),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.selectedPlayMode.localizedTitle(lang),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(uiState.selectedPlayMode.colorHex)
                        )
                    }
                }
            }
        }

        // 6. All 7 Mini-Game Quest Cards
        items(MiniGameType.entries, key = { it.id }) { gameType ->
            MiniGameQuestCard(
                gameType = gameType,
                languageCode = lang,
                playButtonLabel = strings.playNowButton,
                levelPrefix = strings.levelPrefix,
                currentLevel = (profile.completedLevelsCount.toInt() % 12) + 1,
                playMode = uiState.selectedPlayMode,
                onStart = {
                    PlayfulSoundEffects.playTap(!uiState.isSoundMuted)
                    onStartMiniGame(gameType)
                }
            )
        }
    }
}

@Composable
private fun StorageAndSyncTopBanner(
    isCloudConnected: Boolean,
    guestModeBanner: String,
    syncedBanner: String,
    syncNowLabel: String,
    onRequestCloudSignIn: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isCloudConnected) Color(0xFF00897B).copy(alpha = 0.15f)
        else Color(0xFFFF8F00).copy(alpha = 0.16f),
        label = "sync_banner_bg"
    )
    val accentColor = if (isCloudConnected) Color(0xFF00796B) else Color(0xFFE65100)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("storage_sync_status_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCloudConnected) syncedBanner else guestModeBanner,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!isCloudConnected) {
                FilledTonalButton(
                    onClick = onRequestCloudSignIn,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("sync_cloud_banner_button")
                ) {
                    Text(
                        text = syncNowLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveReactionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    AssistChip(
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(50),
        modifier = Modifier.testTag(tag)
    )
}

@Composable
private fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MiniGameQuestCard(
    gameType: MiniGameType,
    languageCode: String,
    playButtonLabel: String,
    levelPrefix: String,
    currentLevel: Int,
    playMode: GamePlayMode,
    onStart: () -> Unit
) {
    val accent = Color(gameType.accentColorHex)

    val previewTokens = when (gameType) {
        MiniGameType.MATH_SAFARI -> listOf("ITEM_APPLE", "ITEM_STAR", "ITEM_BUTTERFLY")
        MiniGameType.PATTERN_PORTAL -> listOf("SHAPE_RED_CIRCLE", "SHAPE_BLUE_SQUARE", "SHAPE_GOLD_STAR")
        MiniGameType.NUMBER_BRIDGE -> listOf("ITEM_STAR", "ITEM_CRYSTAL", "ITEM_SUN")
        MiniGameType.LOGIC_MATRIX -> listOf("SHAPE_BLUE_SQUARE", "ITEM_CAR", "SHAPE_PURPLE_DIAMOND")
        MiniGameType.SHAPE_GALAXY -> listOf("SHAPE_GREEN_TRIANGLE", "SHAPE_BLUE_SQUARE", "SHAPE_GOLD_STAR")
        MiniGameType.MEMORY_TREASURE -> listOf("SHAPE_PURPLE_DIAMOND", "SHAPE_GOLD_STAR", "SHAPE_RED_CIRCLE")
        MiniGameType.COSMIC_BALANCE -> listOf("ITEM_CRYSTAL", "ITEM_MOON", "ITEM_SUN")
    }

    ElevatedCard(
        onClick = onStart,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(26.dp))
            .testTag("minigame_card_${gameType.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.22f),
                            accent.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = accent.copy(alpha = 0.24f),
                    border = BorderStroke(1.5.dp, accent.copy(alpha = 0.45f)),
                    modifier = Modifier.size(58.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = gameType.drawableRes),
                            contentDescription = gameType.localizedTitle(languageCode),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accent.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "$levelPrefix $currentLevel • ${playMode.questionCount}Q",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 0 until 3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        if (playMode.bonusStars > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${playMode.bonusStars}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                        }
                    }
                }
            }

            Text(
                text = gameType.localizedTitle(languageCode),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = gameType.localizedSubtitle(languageCode),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2
            )

            // Illustrated Token Preview Strip for Visual Recognition
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    previewTokens.forEach { tokenCode ->
                        TokenIllustrationCanvas(
                            tokenCode = tokenCode,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onStart,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("play_button_${gameType.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = playButtonLabel
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = playButtonLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
