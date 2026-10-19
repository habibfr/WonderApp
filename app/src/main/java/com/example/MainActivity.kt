package com.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BuddyCharacter
import com.example.ui.AppDestination
import com.example.ui.WonderQuestUiState
import com.example.ui.WonderQuestViewModel
import com.example.ui.auth.AuthManager
import com.example.ui.auth.WonderQuestSignInScreen
import com.example.ui.screens.BadgesTrophiesScreen
import com.example.ui.screens.BuddiesRoomScreen
import com.example.ui.screens.IslandHomeScreen
import com.example.ui.screens.MiniGamePlayScreen
import com.example.ui.screens.ParentalDashboardScreen
import com.example.ui.theme.WonderQuestTheme
import kotlinx.coroutines.launch

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        runCatching {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
            }
        }
        setContent {
            WonderQuestRoot()
        }
    }
}

@Composable
fun WonderQuestRoot(
    viewModel: WonderQuestViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager() }
    val currentUser by authManager.authState.collectAsStateWithLifecycle(
        initialValue = authManager.currentUser
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(context) {
        viewModel.attachLocalStorage(context)
    }

    LaunchedEffect(currentUser?.uid) {
        viewModel.bindAuthenticatedUser(
            uid = currentUser?.uid,
            displayName = currentUser?.displayName
        )
    }

    val triggerGoogleCloudLogin: () -> Unit = {
        val activity = context.findHostActivity()
        if (activity != null) {
            scope.launch {
                val webClientId = context.getString(R.string.default_web_client_id)
                val res = authManager.signInWithGoogle(activity, webClientId)
                res.onFailure {
                    // If Google Sign-In dialog is cancelled or unavailable, take user to the Welcome/Sign-In hub
                    viewModel.exitGuestModeToSignIn()
                }
            }
        } else {
            viewModel.exitGuestModeToSignIn()
        }
    }

    WonderQuestTheme(darkTheme = uiState.profile.darkMode) {
        if (currentUser == null && !uiState.isGuestMode) {
            WonderQuestSignInScreen(
                languageCode = uiState.profile.languageCode,
                onLanguageSelected = { viewModel.selectLanguage(it) },
                onSignInRequest = { activity ->
                    val webClientId = context.getString(R.string.default_web_client_id)
                    authManager.signInWithGoogle(activity, webClientId)
                },
                onGuestModeStart = { viewModel.enterGuestMode() },
                hasSavedDeviceProgress = uiState.hasPendingCloudSync || uiState.profile.completedLevelsCount > 0L,
                savedStarsCount = uiState.profile.totalStars
            )
        } else {
            WonderQuestAppContent(
                uiState = uiState,
                viewModel = viewModel,
                onRequestCloudSignIn = triggerGoogleCloudLogin,
                onSignOut = {
                    if (currentUser != null) {
                        authManager.signOut()
                    }
                    viewModel.exitGuestModeToSignIn()
                }
            )
        }
    }
}

@Composable
fun WonderQuestAppContent(
    uiState: WonderQuestUiState,
    viewModel: WonderQuestViewModel,
    onRequestCloudSignIn: () -> Unit = {},
    onSignOut: () -> Unit
) {
    val strings = uiState.strings

    val navItems = listOf(
        Triple(AppDestination.ISLAND_HOME, strings.navPlay, Icons.Default.Explore),
        Triple(AppDestination.BUDDIES_ROOM, strings.navBuddies, Icons.Default.Pets),
        Triple(AppDestination.BADGES_TROPHIES, strings.navBadges, Icons.Default.EmojiEvents),
        Triple(AppDestination.PARENTAL_HUB, strings.navParents, Icons.Default.Shield)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp
        val showNavBars = uiState.currentDestination != AppDestination.MINI_GAME_PLAY

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (!isWideScreen && showNavBars) {
                    NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                        navItems.forEach { (dest, label, icon) ->
                            val selected = uiState.currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (dest != AppDestination.PARENTAL_HUB) {
                                        viewModel.lockParentalHub()
                                    }
                                    viewModel.navigateTo(dest)
                                },
                                icon = { Icon(imageVector = icon, contentDescription = label) },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen && showNavBars) {
                    NavigationRail(modifier = Modifier.testTag("side_nav_rail")) {
                        navItems.forEach { (dest, label, icon) ->
                            val selected = uiState.currentDestination == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    if (dest != AppDestination.PARENTAL_HUB) {
                                        viewModel.lockParentalHub()
                                    }
                                    viewModel.navigateTo(dest)
                                },
                                icon = { Icon(imageVector = icon, contentDescription = label) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("rail_item_${dest.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (uiState.currentDestination) {
                        AppDestination.ISLAND_HOME -> {
                            IslandHomeScreen(
                                uiState = uiState,
                                onSelectAgeGroup = { viewModel.selectAgeGroup(it) },
                                onSelectPlayMode = { viewModel.selectPlayMode(it) },
                                onSelectLanguage = { viewModel.selectLanguage(it) },
                                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                onBuddyTap = { viewModel.triggerBuddyReaction(it) },
                                onStartMiniGame = { viewModel.startMiniGame(it) },
                                onRequestCloudSignIn = onRequestCloudSignIn
                            )
                        }
                        AppDestination.MINI_GAME_PLAY -> {
                            val active = uiState.activeGame
                            if (active != null) {
                                MiniGamePlayScreen(
                                    activeGame = active,
                                    buddy = BuddyCharacter.fromId(uiState.profile.selectedCharacter),
                                    buddyReaction = uiState.buddyReaction,
                                    languageCode = uiState.profile.languageCode,
                                    strings = strings,
                                    onSelectOption = { viewModel.submitOption(it) },
                                    onToggleHint = { viewModel.toggleHint() },
                                    onToggleVisualToken = { viewModel.toggleVisualTokenTap(it) },
                                    onNextOrFinish = { viewModel.advanceOrFinishMiniGame() },
                                    onPlayNextLevel = {
                                        viewModel.startMiniGame(
                                            gameType = active.gameType,
                                            customLevel = (active.levelNumber % 50) + 1
                                        )
                                    },
                                    onBackToIsland = { viewModel.navigateTo(AppDestination.ISLAND_HOME) }
                                )
                            }
                        }
                        AppDestination.BUDDIES_ROOM -> {
                            BuddiesRoomScreen(
                                selectedCharacterId = uiState.profile.selectedCharacter,
                                currentReaction = uiState.buddyReaction,
                                languageCode = uiState.profile.languageCode,
                                strings = strings,
                                buddyHappiness = uiState.buddyHappiness,
                                buddyEnergy = uiState.buddyEnergy,
                                treatsFedCount = uiState.treatsFedCount,
                                onSelectBuddy = { viewModel.selectBuddy(it) },
                                onTriggerReaction = { viewModel.triggerBuddyReaction(it) },
                                onBackToIsland = { viewModel.navigateTo(AppDestination.ISLAND_HOME) }
                            )
                        }
                        AppDestination.BADGES_TROPHIES -> {
                            BadgesTrophiesScreen(
                                unlockedBadgeIds = uiState.profile.unlockedBadges,
                                languageCode = uiState.profile.languageCode,
                                strings = strings,
                                recentSessions = uiState.recentSessions,
                                profile = uiState.profile,
                                onBackToIsland = { viewModel.navigateTo(AppDestination.ISLAND_HOME) }
                            )
                        }
                        AppDestination.PARENTAL_HUB -> {
                            ParentalDashboardScreen(
                                uiState = uiState,
                                onUnlockWithPin = { viewModel.unlockParentalHub(it) },
                                onSaveSettings = { name, pin -> viewModel.saveParentalSettings(name, pin) },
                                onSelectLanguage = { viewModel.selectLanguage(it) },
                                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                onResetProgress = { viewModel.resetAllProgress() },
                                onSignOut = onSignOut,
                                onLinkCloudAccount = onRequestCloudSignIn,
                                onBackToIsland = {
                                    viewModel.lockParentalHub()
                                    viewModel.navigateTo(AppDestination.ISLAND_HOME)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
