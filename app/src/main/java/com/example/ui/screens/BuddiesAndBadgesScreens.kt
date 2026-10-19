package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import com.example.ui.components.BuddyMiniAvatar
import com.example.ui.components.TrophyVaultIllustratedBanner
import com.example.ui.components.WonderQuestIcon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BadgeCatalog
import com.example.data.BadgeDefinition
import com.example.data.BuddyCharacter
import com.example.data.BuddyReaction
import com.example.data.ChildProfile
import com.example.data.GameSessionRecord
import com.example.data.LocalizedUiStrings
import com.example.ui.components.AnimatedBuddyCanvas
import com.example.ui.components.RechartsWeeklyActivityCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuddiesRoomScreen(
    selectedCharacterId: String,
    currentReaction: BuddyReaction,
    languageCode: String,
    strings: LocalizedUiStrings,
    buddyHappiness: Int = 90,
    buddyEnergy: Int = 95,
    treatsFedCount: Int = 0,
    onSelectBuddy: (String) -> Unit,
    onTriggerReaction: (BuddyReaction) -> Unit,
    onBackToIsland: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToIsland)

    val activeBuddy = BuddyCharacter.fromId(selectedCharacterId)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 240.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("buddies_room_grid"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(activeBuddy.primaryColorHex).copy(alpha = 0.16f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.buddiesScreenTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = strings.buddiesScreenSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    AnimatedBuddyCanvas(
                        character = activeBuddy,
                        reaction = currentReaction,
                        modifier = Modifier
                            .size(188.dp)
                            .clickable { onTriggerReaction(BuddyReaction.DANCING) }
                            .testTag("active_buddy_stage_canvas")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        BuddyMiniAvatar(
                            character = activeBuddy,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${activeBuddy.localizedName(languageCode)} — ${activeBuddy.localizedTitle(languageCode)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Interactive Happiness, Energy & Treats Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$buddyHappiness%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$buddyEnergy%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "×$treatsFedCount",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // 6 Interactive Reaction & Prop Buttons
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilledTonalButton(
                            onClick = { onTriggerReaction(BuddyReaction.DANCING) },
                            modifier = Modifier.testTag("reaction_dance_button")
                        ) {
                            Icon(imageVector = Icons.Default.Celebration, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.triggerDanceButton, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = { onTriggerReaction(BuddyReaction.SPARKLE) },
                            modifier = Modifier.testTag("reaction_sparkle_button")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.triggerSparkleButton, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = { onTriggerReaction(BuddyReaction.HIGH_FIVE) },
                            modifier = Modifier.testTag("reaction_high_five_button")
                        ) {
                            Icon(imageVector = Icons.Default.PanTool, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.triggerHighFiveButton, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = { onTriggerReaction(BuddyReaction.EATING_TREAT) },
                            modifier = Modifier.testTag("reaction_feed_treat_button")
                        ) {
                            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.feedTreatButton, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = { onTriggerReaction(BuddyReaction.MAGIC_WAND) },
                            modifier = Modifier.testTag("reaction_magic_wand_button")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.magicWandButton, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = { onTriggerReaction(BuddyReaction.SLEEPY) },
                            modifier = Modifier.testTag("reaction_sleep_button")
                        ) {
                            Icon(imageVector = Icons.Default.Bedtime, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.sleepTightButton, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(BuddyCharacter.entries, key = { it.id }) { buddy ->
            val isSelected = buddy.id == selectedCharacterId
            val accent = Color(buddy.primaryColorHex)

            ElevatedCard(
                onClick = { onSelectBuddy(buddy.id) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("buddy_card_${buddy.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedBuddyCanvas(
                        character = buddy,
                        reaction = if (isSelected) BuddyReaction.CELEBRATING else BuddyReaction.IDLE,
                        modifier = Modifier.size(96.dp)
                    )
                    Text(
                        text = buddy.localizedName(languageCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = buddy.localizedTitle(languageCode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { onSelectBuddy(buddy.id) },
                        enabled = !isSelected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("select_buddy_button_${buddy.id}")
                    ) {
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.selectedBuddyBadge, fontWeight = FontWeight.Bold)
                        } else {
                            Text(strings.selectBuddyButton, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgesTrophiesScreen(
    unlockedBadgeIds: List<String>,
    languageCode: String,
    strings: LocalizedUiStrings,
    onBackToIsland: () -> Unit,
    recentSessions: List<GameSessionRecord> = emptyList(),
    profile: ChildProfile = ChildProfile(),
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToIsland)

    val totalBadges = BadgeCatalog.allBadges.size
    val unlockedCount = BadgeCatalog.allBadges.count { it.id in unlockedBadgeIds }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 240.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("badges_trophies_grid"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TrophyVaultIllustratedBanner(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(118.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.badgesScreenTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$unlockedCount / $totalBadges",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = strings.badgesScreenSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LinearProgressIndicator(
                        progress = { (unlockedCount.toFloat() / totalBadges.coerceAtLeast(1)).coerceIn(0.05f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = Color(0xFFFFB300)
                    )
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            RechartsWeeklyActivityCard(
                sessions = recentSessions,
                profile = profile,
                languageCode = languageCode
            )
        }

        items(BadgeCatalog.allBadges, key = { it.id }) { badge ->
            val unlocked = badge.id in unlockedBadgeIds
            BadgeItemCard(
                badge = badge,
                unlocked = unlocked,
                languageCode = languageCode,
                unlockedLabel = strings.badgeUnlockedLabel,
                lockedLabel = strings.badgeLockedLabel
            )
        }
    }
}

@Composable
private fun BadgeItemCard(
    badge: BadgeDefinition,
    unlocked: Boolean,
    languageCode: String,
    unlockedLabel: String,
    lockedLabel: String
) {
    val accent = Color(badge.colorHex)
    var celebrated by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (celebrated) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "badge_scale_${badge.id}"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unlocked) 6.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { celebrated = !celebrated }
            .testTag("badge_card_${badge.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (unlocked) {
                        Brush.verticalGradient(
                            colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent)
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    }
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (unlocked) accent.copy(alpha = 0.24f) else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (unlocked) {
                        WonderQuestIcon(
                            iconKey = badge.iconKey,
                            tint = accent,
                            modifier = Modifier.size(34.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = lockedLabel,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Text(
                text = badge.localizedTitle(languageCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = badge.localizedDesc(languageCode),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                minLines = 2
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = if (unlocked) accent.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (unlocked) unlockedLabel else lockedLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}
