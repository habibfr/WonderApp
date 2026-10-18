package com.example

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.ChildProfile
import com.example.data.MiniGameType
import com.example.data.WonderQuestRepository
import com.example.data.WonderQuestStrings
import com.example.ui.UiState
import com.example.ui.WonderQuestViewModel
import com.example.ui.auth.WonderQuestAuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.screens.BadgesTrophyScreen
import com.example.ui.screens.BuddiesPlaygroundScreen
import com.example.ui.screens.IslandHomeScreen
import com.example.ui.screens.MiniGamePlayScreen
import com.example.ui.screens.ParentalDashboardScreen
import com.example.ui.theme.WonderQuestTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WonderQuestRootApp()
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

enum class MainDestination {
    ISLAND,
    BUDDIES,
    BADGES,
    PARENTS
}

@Composable
fun WonderQuestRootApp(auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var guestLanguage by rememberSaveable { mutableStateOf("en") }
    var guestDarkMode by rememberSaveable { mutableStateOf(false) }

    val user = currentUser
    if (user == null) {
        WonderQuestTheme(darkTheme = guestDarkMode) {
            WonderQuestAuthScreen(
                selectedLanguage = guestLanguage,
                onLanguageSelected = { guestLanguage = it },
                isDarkMode = guestDarkMode,
                onToggleDarkMode = { guestDarkMode = !guestDarkMode },
                onAuthSuccess = {}
            )
        }
    } else {
        AuthenticatedWonderQuestApp(
            currentUserId = user.uid,
            defaultDisplayName = user.displayName ?: "Explorer"
        )
    }
}

@Composable
fun AuthenticatedWonderQuestApp(
    currentUserId: String,
    defaultDisplayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: WonderQuestViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                WonderQuestViewModel(
                    repository = WonderQuestRepository(db),
                    currentUserId = currentUserId,
                    defaultDisplayName = defaultDisplayName
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val sessionsState by viewModel.sessionsState.collectAsStateWithLifecycle()
    val newlyUnlockedBadges by viewModel.newlyUnlockedBadges.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val activeProfile = (profileState as? UiState.Success)?.data
        ?: ChildProfile(id = currentUserId, userId = currentUserId, childName = defaultDisplayName)
    val activeSessions = (sessionsState as? UiState.Success)?.data ?: emptyList()
    val strings = WonderQuestStrings.forLanguage(activeProfile.languageCode)

    val isOnline = rememberNetworkConnectivityState(context)

    var currentTab by rememberSaveable { mutableStateOf(MainDestination.ISLAND) }
    var activeMiniGameId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeLevelNumber by rememberSaveable { mutableIntStateOf(1) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        statusBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissBanner()
        }
    }

    // Back handler for secondary tabs when not inside a mini-game
    BackHandler(enabled = activeMiniGameId == null && currentTab != MainDestination.ISLAND) {
        currentTab = MainDestination.ISLAND
    }

    WonderQuestTheme(darkTheme = activeProfile.darkMode) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!isExpandedScreen && activeMiniGameId == null) {
                        NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                            NavigationBarItem(
                                selected = currentTab == MainDestination.ISLAND,
                                onClick = { currentTab = MainDestination.ISLAND },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == MainDestination.ISLAND) Icons.Filled.Explore else Icons.Outlined.Explore,
                                        contentDescription = strings.navPlay
                                    )
                                },
                                label = { Text(strings.navPlay) },
                                modifier = Modifier.testTag("nav_tab_island")
                            )
                            NavigationBarItem(
                                selected = currentTab == MainDestination.BUDDIES,
                                onClick = { currentTab = MainDestination.BUDDIES },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == MainDestination.BUDDIES) Icons.Filled.Pets else Icons.Outlined.Pets,
                                        contentDescription = strings.navBuddies
                                    )
                                },
                                label = { Text(strings.navBuddies) },
                                modifier = Modifier.testTag("nav_tab_buddies")
                            )
                            NavigationBarItem(
                                selected = currentTab == MainDestination.BADGES,
                                onClick = { currentTab = MainDestination.BADGES },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == MainDestination.BADGES) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                                        contentDescription = strings.navBadges
                                    )
                                },
                                label = { Text(strings.navBadges) },
                                modifier = Modifier.testTag("nav_tab_badges")
                            )
                            NavigationBarItem(
                                selected = currentTab == MainDestination.PARENTS,
                                onClick = { currentTab = MainDestination.PARENTS },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == MainDestination.PARENTS) Icons.Filled.Shield else Icons.Outlined.Shield,
                                        contentDescription = strings.navParents
                                    )
                                },
                                label = { Text(strings.navParents) },
                                modifier = Modifier.testTag("nav_tab_parents")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen && activeMiniGameId == null) {
                        NavigationRail(modifier = Modifier.testTag("side_nav_rail")) {
                            NavigationRailItem(
                                selected = currentTab == MainDestination.ISLAND,
                                onClick = { currentTab = MainDestination.ISLAND },
                                icon = { Icon(Icons.Filled.Explore, contentDescription = strings.navPlay) },
                                label = { Text(strings.navPlay) },
                                modifier = Modifier.testTag("rail_tab_island")
                            )
                            NavigationRailItem(
                                selected = currentTab == MainDestination.BUDDIES,
                                onClick = { currentTab = MainDestination.BUDDIES },
                                icon = { Icon(Icons.Filled.Pets, contentDescription = strings.navBuddies) },
                                label = { Text(strings.navBuddies) },
                                modifier = Modifier.testTag("rail_tab_buddies")
                            )
                            NavigationRailItem(
                                selected = currentTab == MainDestination.BADGES,
                                onClick = { currentTab = MainDestination.BADGES },
                                icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = strings.navBadges) },
                                label = { Text(strings.navBadges) },
                                modifier = Modifier.testTag("rail_tab_badges")
                            )
                            NavigationRailItem(
                                selected = currentTab == MainDestination.PARENTS,
                                onClick = { currentTab = MainDestination.PARENTS },
                                icon = { Icon(Icons.Filled.Shield, contentDescription = strings.navParents) },
                                label = { Text(strings.navParents) },
                                modifier = Modifier.testTag("rail_tab_parents")
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (profileState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else if (activeMiniGameId != null) {
                            val gameType = MiniGameType.fromId(activeMiniGameId!!)
                            MiniGamePlayScreen(
                                gameType = gameType,
                                levelNumber = activeLevelNumber,
                                profile = activeProfile,
                                strings = strings,
                                newlyUnlockedBadges = newlyUnlockedBadges,
                                onLevelCompleted = { stars, correct, total, durationSec ->
                                    viewModel.recordLevelCompletion(
                                        gameType = gameType.id,
                                        levelNumber = activeLevelNumber,
                                        starsEarned = stars,
                                        correctAnswers = correct,
                                        totalQuestions = total,
                                        durationSeconds = durationSec
                                    )
                                },
                                onPlayNextLevel = { nextLevel ->
                                    viewModel.clearNewlyUnlockedBadges()
                                    activeLevelNumber = nextLevel.coerceIn(1, 50)
                                },
                                onBackToIsland = {
                                    viewModel.clearNewlyUnlockedBadges()
                                    activeMiniGameId = null
                                }
                            )
                        } else {
                            when (currentTab) {
                                MainDestination.ISLAND -> IslandHomeScreen(
                                    profile = activeProfile,
                                    strings = strings,
                                    isOnline = isOnline,
                                    onSelectAgeBracket = { viewModel.updateAgeBracket(it) },
                                    onSelectLanguage = { viewModel.updateLanguage(it) },
                                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                                    onStartMiniGame = { game, level ->
                                        viewModel.clearNewlyUnlockedBadges()
                                        activeLevelNumber = level
                                        activeMiniGameId = game.id
                                    },
                                    onNavigateToBuddies = { currentTab = MainDestination.BUDDIES }
                                )
                                MainDestination.BUDDIES -> BuddiesPlaygroundScreen(
                                    profile = activeProfile,
                                    strings = strings,
                                    onSelectBuddy = { viewModel.updateBuddyCharacter(it) }
                                )
                                MainDestination.BADGES -> BadgesTrophyScreen(
                                    profile = activeProfile,
                                    strings = strings
                                )
                                MainDestination.PARENTS -> ParentalDashboardScreen(
                                    profile = activeProfile,
                                    sessions = activeSessions,
                                    strings = strings,
                                    onSaveSettings = { name, age, lang, dark, pin ->
                                        viewModel.savePreferences(
                                            childName = name,
                                            ageGroup = age,
                                            selectedCharacter = activeProfile.selectedCharacter,
                                            languageCode = lang,
                                            darkMode = dark,
                                            parentPin = pin
                                        )
                                    },
                                    onResetProgress = { viewModel.resetProgress() },
                                    onSignOut = {
                                        signOutUser(
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {},
                                            scope = scope
                                        )
                                    }
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
private fun rememberNetworkConnectivityState(context: Context): Boolean {
    var isConnected by remember {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = cm?.activeNetwork
        val caps = activeNet?.let { cm.getNetworkCapabilities(it) }
        val connected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        mutableStateOf(connected)
    }

    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
            }

            override fun onLost(network: Network) {
                isConnected = false
            }
        }
        try {
            cm?.registerDefaultNetworkCallback(callback)
        } catch (ignored: Exception) {
        }
        onDispose {
            try {
                cm?.unregisterNetworkCallback(callback)
            } catch (ignored: Exception) {
            }
        }
    }

    return isConnected
}
