package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.LocalizedUiStrings
import com.example.ui.AppDestination
import com.example.ui.theme.WonderQuestDesignSystem

@Immutable
data class PlayfulNavTabItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
    val activeAccent: Color,
    val softContainer: Color
)

fun buildPlayfulNavItems(strings: LocalizedUiStrings): List<PlayfulNavTabItem> = listOf(
    PlayfulNavTabItem(
        destination = AppDestination.ISLAND_HOME,
        label = strings.navPlay,
        icon = Icons.Default.Explore,
        activeAccent = WonderQuestDesignSystem.Colors.SkyBlueInteractive,
        softContainer = WonderQuestDesignSystem.Colors.SkyBlueSoft
    ),
    PlayfulNavTabItem(
        destination = AppDestination.BUDDIES_ROOM,
        label = strings.navBuddies,
        icon = Icons.Default.Pets,
        activeAccent = WonderQuestDesignSystem.Colors.CoralInteractive,
        softContainer = WonderQuestDesignSystem.Colors.CoralSoft
    ),
    PlayfulNavTabItem(
        destination = AppDestination.BADGES_TROPHIES,
        label = strings.navBadges,
        icon = Icons.Default.EmojiEvents,
        activeAccent = WonderQuestDesignSystem.Colors.SunshineGold,
        softContainer = WonderQuestDesignSystem.Colors.SunshineSoft
    ),
    PlayfulNavTabItem(
        destination = AppDestination.PARENTAL_HUB,
        label = strings.navParents,
        icon = Icons.Default.Shield,
        activeAccent = WonderQuestDesignSystem.Colors.PurpleInteractive,
        softContainer = WonderQuestDesignSystem.Colors.PurpleSoft
    )
)

/**
 * Kid-friendly Bottom Navigation Bar with large 28.dp–30.dp icons inside colorful
 * pill medallions, spring-bouncy active animations, and 3D rounded card styling.
 */
@Composable
fun PlayfulBottomNavigationBar(
    items: List<PlayfulNavTabItem>,
    currentDestination: AppDestination,
    onSelectDestination: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("bottom_nav_bar"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentDestination == item.destination
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val itemScale by animateFloatAsState(
                    targetValue = when {
                        isPressed -> 0.92f
                        isSelected -> 1.06f
                        else -> 1f
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "nav_tab_scale_${item.destination.name}"
                )

                val pillColor by animateColorAsState(
                    targetValue = if (isSelected) item.activeAccent else Color.Transparent,
                    animationSpec = tween(durationMillis = 240),
                    label = "nav_tab_bg_${item.destination.name}"
                )

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) Color.White else item.activeAccent,
                    animationSpec = tween(durationMillis = 240),
                    label = "nav_tab_tint_${item.destination.name}"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .scale(itemScale)
                        .clip(RoundedCornerShape(22.dp))
                        .semantics { this.selected = isSelected }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(color = item.activeAccent),
                            role = Role.Tab,
                            onClick = { onSelectDestination(item.destination) }
                        )
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .testTag("nav_item_${item.destination.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) pillColor else item.activeAccent.copy(alpha = 0.14f),
                        border = if (isSelected) BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)) else null,
                        shadowElevation = if (isSelected) 4.dp else 0.dp,
                        modifier = Modifier
                            .width(56.dp)
                            .height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = iconTint,
                                modifier = Modifier.size(if (isSelected) 28.dp else 25.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) item.activeAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Active dot indicator
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(width = if (isSelected) 16.dp else 0.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) item.activeAccent else Color.Transparent)
                    )
                }
            }
        }
    }
}

/**
 * Adaptive Side Navigation Rail for tablets & wide screens with large kid-friendly icons.
 */
@Composable
fun PlayfulSideNavigationRail(
    items: List<PlayfulNavTabItem>,
    currentDestination: AppDestination,
    onSelectDestination: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag("side_nav_rail"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { item ->
                val isSelected = currentDestination == item.destination
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.06f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "rail_scale_${item.destination.name}"
                )

                Column(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(20.dp))
                        .semantics { this.selected = isSelected }
                        .clickable(
                            role = Role.Tab,
                            onClick = { onSelectDestination(item.destination) }
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("rail_item_${item.destination.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) item.activeAccent else item.activeAccent.copy(alpha = 0.14f),
                        shadowElevation = if (isSelected) 4.dp else 0.dp,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) Color.White else item.activeAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) item.activeAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Custom state switcher with directional animated transitions (slide + fade + gentle scale)
 * between 'Play', 'Buddies', 'Badges', 'Parents', and active Mini-Game screens.
 */
@Composable
fun AnimatedDestinationSwitcher(
    currentDestination: AppDestination,
    modifier: Modifier = Modifier,
    content: @Composable (AppDestination) -> Unit
) {
    AnimatedContent(
        targetState = currentDestination,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            if (targetState == AppDestination.MINI_GAME_PLAY || initialState == AppDestination.MINI_GAME_PLAY) {
                (fadeIn(animationSpec = tween(260)) + scaleIn(initialScale = 0.94f, animationSpec = tween(260)))
                    .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.96f, animationSpec = tween(200)))
            } else {
                val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                (slideInHorizontally(
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> direction * (fullWidth / 5) }
                ) + fadeIn(animationSpec = tween(240)))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(240, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -direction * (fullWidth / 6) }
                        ) + fadeOut(animationSpec = tween(180))
                    )
            }.using(SizeTransform(clip = false))
        },
        label = "wonderquest_destination_switcher"
    ) { destination ->
        content(destination)
    }
}
