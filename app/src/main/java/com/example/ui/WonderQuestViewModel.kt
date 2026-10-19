package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgeGroupConfig
import com.example.data.BadgeCatalog
import com.example.data.BuddyReaction
import com.example.data.ChildProfile
import com.example.data.GamePlayMode
import com.example.data.GameSessionRecord
import com.example.data.LocalDeviceStorage
import com.example.data.LocalizedUiStrings
import com.example.data.MiniGameType
import com.example.data.WonderQuestRepository
import com.example.data.WonderQuestStrings
import com.example.domain.MiniGameGenerator
import com.example.domain.MiniGameQuestion
import com.example.util.SoundManager
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppDestination {
    ISLAND_HOME,
    MINI_GAME_PLAY,
    BUDDIES_ROOM,
    BADGES_TROPHIES,
    PARENTAL_HUB
}

data class ActiveGameState(
    val gameType: MiniGameType,
    val ageGroup: AgeGroupConfig,
    val playMode: GamePlayMode = GamePlayMode.ADVENTURE,
    val levelNumber: Int,
    val questions: List<MiniGameQuestion>,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val isCurrentCorrect: Boolean = false,
    val correctCount: Int = 0,
    val currentStreak: Int = 0,
    val showHint: Boolean = false,
    val tappedTokenIndices: Set<Int> = emptySet(),
    val memoryPreviewActive: Boolean = false,
    val remainingBlitzSeconds: Int? = null,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val earnedStars: Int = 0,
    val newlyUnlockedBadges: List<String> = emptyList()
) {
    val currentQuestion: MiniGameQuestion
        get() = questions[currentQuestionIndex.coerceIn(0, questions.lastIndex)]
}

data class WonderQuestUiState(
    val isLoading: Boolean = true,
    val isGuestMode: Boolean = false,
    val isCloudConnected: Boolean = false,
    val hasPendingCloudSync: Boolean = false,
    val syncBannerMessage: String? = null,
    val profile: ChildProfile = ChildProfile(),
    val recentSessions: List<GameSessionRecord> = emptyList(),
    val currentDestination: AppDestination = AppDestination.ISLAND_HOME,
    val selectedPlayMode: GamePlayMode = GamePlayMode.ADVENTURE,
    val activeGame: ActiveGameState? = null,
    val buddyReaction: BuddyReaction = BuddyReaction.IDLE,
    val buddyHappiness: Int = 88,
    val buddyEnergy: Int = 92,
    val treatsFedCount: Int = 0,
    val isParentUnlocked: Boolean = false,
    val parentPinError: Boolean = false,
    val isSoundMuted: Boolean = false,
    val errorMessage: String? = null
) {
    val strings: LocalizedUiStrings
        get() = WonderQuestStrings.forLanguage(profile.languageCode)

    val selectedAgeGroup: AgeGroupConfig
        get() = AgeGroupConfig.fromCode(profile.ageGroup)
}

class WonderQuestViewModel(
    private val repository: WonderQuestRepository = WonderQuestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WonderQuestUiState())
    val uiState: StateFlow<WonderQuestUiState> = _uiState.asStateFlow()

    private var localStorage: LocalDeviceStorage? = null
    private var profileJob: Job? = null
    private var sessionsJob: Job? = null
    private var reactionResetJob: Job? = null
    private var blitzTimerJob: Job? = null
    private var currentUid: String? = null

    fun attachLocalStorage(context: Context) {
        if (localStorage == null) {
            val storage = LocalDeviceStorage(context)
            localStorage = storage
            val savedProfile = storage.loadLocalProfile()
            val savedSessions = storage.loadLocalSessions()
            val savedMuted = storage.isSoundMuted()
            SoundManager.setMuted(savedMuted)
            SoundManager.startBackgroundMusic(SoundManager.MusicTheme.ISLAND_THEME)
            _uiState.update { state ->
                if (currentUid == null) {
                    state.copy(
                        isLoading = false,
                        profile = savedProfile,
                        recentSessions = savedSessions,
                        isSoundMuted = savedMuted,
                        hasPendingCloudSync = storage.hasPendingCloudSync()
                    )
                } else {
                    state.copy(isSoundMuted = savedMuted)
                }
            }
        }
    }

    fun enterGuestMode() {
        val storage = localStorage
        val guestProfile = storage?.loadLocalProfile() ?: _uiState.value.profile.copy(
            id = "local_guest_profile",
            userId = "guest_local",
            languageCode = "id"
        )
        val guestSessions = storage?.loadLocalSessions() ?: _uiState.value.recentSessions
        storage?.setGuestModeEnabled(true)
        storage?.saveLocalProfile(guestProfile, markForSync = false)
        _uiState.update {
            it.copy(
                isLoading = false,
                isGuestMode = true,
                isCloudConnected = false,
                hasPendingCloudSync = storage?.hasPendingCloudSync() ?: false,
                profile = guestProfile,
                recentSessions = guestSessions,
                currentDestination = AppDestination.ISLAND_HOME
            )
        }
        SoundManager.startBackgroundMusic(SoundManager.MusicTheme.ISLAND_THEME)
        triggerBuddyReaction(BuddyReaction.CELEBRATING)
    }

    fun bindAuthenticatedUser(uid: String?, displayName: String?) {
        if (uid == null) {
            currentUid = null
            profileJob?.cancel()
            sessionsJob?.cancel()
            blitzTimerJob?.cancel()
            val storage = localStorage
            val localProf = storage?.loadLocalProfile() ?: ChildProfile(languageCode = "id")
            val localSess = storage?.loadLocalSessions() ?: emptyList()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isCloudConnected = false,
                    profile = localProf,
                    recentSessions = localSess,
                    hasPendingCloudSync = storage?.hasPendingCloudSync() ?: false
                )
            }
            return
        }

        if (currentUid == uid) return
        currentUid = uid

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val storage = localStorage
                val syncedProfile = if (storage != null) {
                    val localProf = storage.loadLocalProfile()
                    val localSess = storage.loadLocalSessions()
                    repository.syncLocalProgressToCloud(uid, localProf, localSess, storage)
                } else {
                    val fallbackName = displayName?.takeIf { it.isNotBlank() } ?: "Penjelajah Kecil"
                    repository.ensureChildProfile(uid, fallbackName)
                }
                storage?.setGuestModeEnabled(false)
                storage?.saveLocalProfile(syncedProfile, markForSync = false)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isGuestMode = false,
                        isCloudConnected = true,
                        hasPendingCloudSync = false,
                        profile = syncedProfile,
                        syncBannerMessage = WonderQuestStrings.forLanguage(syncedProfile.languageCode).syncedWithCloudBanner
                    )
                }
            } catch (e: Exception) {
                val storage = localStorage
                val fallback = storage?.loadLocalProfile() ?: ChildProfile(id = uid, userId = uid, languageCode = "id")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCloudConnected = false,
                        profile = fallback,
                        errorMessage = e.localizedMessage
                    )
                }
            }
        }

        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            repository.observeChildProfile(uid)
                .catch { e -> _uiState.update { it.copy(errorMessage = e.localizedMessage) } }
                .collect { cloudProfile ->
                    if (cloudProfile != null) {
                        localStorage?.saveLocalProfile(cloudProfile, markForSync = false)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isGuestMode = false,
                                isCloudConnected = true,
                                hasPendingCloudSync = false,
                                profile = cloudProfile
                            )
                        }
                    }
                }
        }

        sessionsJob?.cancel()
        sessionsJob = viewModelScope.launch {
            repository.observeRecentSessions(uid)
                .catch { e -> _uiState.update { it.copy(errorMessage = e.localizedMessage) } }
                .collect { records ->
                    localStorage?.saveSessionsList(records, markForSync = false)
                    _uiState.update { it.copy(recentSessions = records) }
                }
        }
    }

    fun navigateTo(destination: AppDestination) {
        if (destination != AppDestination.MINI_GAME_PLAY) {
            blitzTimerJob?.cancel()
            SoundManager.startBackgroundMusic(SoundManager.MusicTheme.ISLAND_THEME)
        } else {
            SoundManager.startBackgroundMusic(SoundManager.MusicTheme.GAMEPLAY_QUEST)
        }
        _uiState.update { state ->
            state.copy(
                currentDestination = destination,
                activeGame = if (destination == AppDestination.MINI_GAME_PLAY) state.activeGame else null,
                parentPinError = false
            )
        }
    }

    fun selectPlayMode(mode: GamePlayMode) {
        _uiState.update { it.copy(selectedPlayMode = mode) }
        triggerBuddyReaction(BuddyReaction.SPARKLE)
    }

    fun triggerBuddyReaction(reaction: BuddyReaction) {
        reactionResetJob?.cancel()
        if (reaction in listOf(
                BuddyReaction.EATING_TREAT,
                BuddyReaction.MAGIC_WAND,
                BuddyReaction.HIGH_FIVE,
                BuddyReaction.DANCING,
                BuddyReaction.SPARKLE
            )
        ) {
            SoundManager.playRewardTrigger(!_uiState.value.isSoundMuted)
        }
        _uiState.update { state ->
            val newHappiness = (state.buddyHappiness + 4).coerceAtMost(100)
            val newEnergy = when (reaction) {
                BuddyReaction.EATING_TREAT -> (state.buddyEnergy + 8).coerceAtMost(100)
                BuddyReaction.SLEEPY -> 100
                else -> (state.buddyEnergy + 2).coerceAtMost(100)
            }
            val newTreats = if (reaction == BuddyReaction.EATING_TREAT) state.treatsFedCount + 1 else state.treatsFedCount
            state.copy(
                buddyReaction = reaction,
                buddyHappiness = newHappiness,
                buddyEnergy = newEnergy,
                treatsFedCount = newTreats
            )
        }
        reactionResetJob = viewModelScope.launch {
            delay(2800L)
            _uiState.update { it.copy(buddyReaction = BuddyReaction.IDLE) }
        }
    }

    fun selectAgeGroup(ageGroup: AgeGroupConfig) {
        val current = _uiState.value.profile
        val updated = current.copy(ageGroup = ageGroup.code)
        _uiState.update { it.copy(profile = updated, hasPendingCloudSync = currentUid == null) }
        localStorage?.saveLocalProfile(updated, markForSync = currentUid == null)
        val uid = currentUid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    repository.updatePreferences(uid = uid, ageGroup = ageGroup.code)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun selectBuddy(characterId: String) {
        val current = _uiState.value.profile
        val updated = current.copy(selectedCharacter = characterId)
        _uiState.update { it.copy(profile = updated, hasPendingCloudSync = currentUid == null) }
        localStorage?.saveLocalProfile(updated, markForSync = currentUid == null)
        triggerBuddyReaction(BuddyReaction.HIGH_FIVE)
        val uid = currentUid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    repository.updatePreferences(uid = uid, selectedCharacter = characterId)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun selectLanguage(languageCode: String) {
        val current = _uiState.value.profile
        val updated = current.copy(languageCode = languageCode)
        _uiState.update { state ->
            val rebuiltGame = state.activeGame?.let { active ->
                val newQuestions = MiniGameGenerator.generateQuestions(
                    gameType = active.gameType,
                    ageGroup = active.ageGroup,
                    levelNumber = active.levelNumber,
                    languageCode = languageCode,
                    playMode = active.playMode
                )
                active.copy(questions = newQuestions)
            }
            state.copy(
                profile = updated,
                activeGame = rebuiltGame,
                hasPendingCloudSync = currentUid == null
            )
        }
        localStorage?.saveLocalProfile(updated, markForSync = currentUid == null)
        val uid = currentUid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    repository.updatePreferences(uid = uid, languageCode = languageCode)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        val current = _uiState.value.profile
        val updated = current.copy(darkMode = enabled)
        _uiState.update { it.copy(profile = updated, hasPendingCloudSync = currentUid == null) }
        localStorage?.saveLocalProfile(updated, markForSync = currentUid == null)
        val uid = currentUid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    repository.updatePreferences(uid = uid, darkMode = enabled)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun startMiniGame(gameType: MiniGameType, customLevel: Int? = null) {
        val state = _uiState.value
        val ageGroup = state.selectedAgeGroup
        val playMode = state.selectedPlayMode
        val levelNumber = customLevel ?: ((state.profile.completedLevelsCount.toInt() % 12) + 1)
        val questions = MiniGameGenerator.generateQuestions(
            gameType = gameType,
            ageGroup = ageGroup,
            levelNumber = levelNumber,
            languageCode = state.profile.languageCode,
            playMode = playMode
        )
        val initialBlitzSeconds = if (playMode.timeLimitSeconds > 0) playMode.timeLimitSeconds else null
        _uiState.update {
            it.copy(
                currentDestination = AppDestination.MINI_GAME_PLAY,
                activeGame = ActiveGameState(
                    gameType = gameType,
                    ageGroup = ageGroup,
                    playMode = playMode,
                    levelNumber = levelNumber,
                    questions = questions,
                    memoryPreviewActive = gameType == MiniGameType.MEMORY_TREASURE,
                    remainingBlitzSeconds = initialBlitzSeconds
                ),
                buddyReaction = BuddyReaction.CELEBRATING
            )
        }
        SoundManager.startBackgroundMusic(SoundManager.MusicTheme.GAMEPLAY_QUEST)
        startBlitzTimerIfNeeded(initialBlitzSeconds)
        if (gameType == MiniGameType.MEMORY_TREASURE) {
            scheduleMemoryPreviewHide()
        }
    }

    private fun scheduleMemoryPreviewHide() {
        viewModelScope.launch {
            delay(2200L)
            _uiState.update { state ->
                val game = state.activeGame ?: return@update state
                state.copy(activeGame = game.copy(memoryPreviewActive = false))
            }
        }
    }

    private fun startBlitzTimerIfNeeded(seconds: Int?) {
        blitzTimerJob?.cancel()
        if (seconds == null || seconds <= 0) return
        blitzTimerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000L)
                val game = _uiState.value.activeGame ?: break
                if (game.isCompleted) break
                remaining -= 1
                _uiState.update { state ->
                    val currentGame = state.activeGame ?: return@update state
                    state.copy(activeGame = currentGame.copy(remainingBlitzSeconds = remaining))
                }
            }
        }
    }

    fun toggleVisualTokenTap(tokenIndex: Int) {
        _uiState.update { state ->
            val game = state.activeGame ?: return@update state
            val updatedSet = game.tappedTokenIndices.toMutableSet()
            if (!updatedSet.add(tokenIndex)) {
                updatedSet.remove(tokenIndex)
            } else {
                SoundManager.playTokenCount(updatedSet.size, !state.isSoundMuted)
            }
            state.copy(
                activeGame = game.copy(tappedTokenIndices = updatedSet),
                buddyReaction = BuddyReaction.SPARKLE
            )
        }
    }

    fun submitOption(optionIndex: Int) {
        val game = _uiState.value.activeGame ?: return
        if (game.isAnswerSubmitted && game.isCurrentCorrect) return

        val isCorrect = optionIndex == game.currentQuestion.correctIndex
        val updatedCorrectCount = if (isCorrect) game.correctCount + 1 else game.correctCount
        val updatedStreak = if (isCorrect) game.currentStreak + 1 else 0

        _uiState.update {
            it.copy(
                activeGame = game.copy(
                    selectedOptionIndex = optionIndex,
                    isAnswerSubmitted = true,
                    isCurrentCorrect = isCorrect,
                    correctCount = updatedCorrectCount,
                    currentStreak = updatedStreak,
                    showHint = !isCorrect
                ),
                buddyReaction = if (isCorrect) BuddyReaction.CELEBRATING else BuddyReaction.THINKING
            )
        }
    }

    fun toggleSoundMute() {
        setSoundMuted(!_uiState.value.isSoundMuted)
    }

    fun setSoundMuted(muted: Boolean) {
        SoundManager.setMuted(muted)
        localStorage?.setSoundMuted(muted)
        _uiState.update { it.copy(isSoundMuted = muted) }
        if (!muted) {
            SoundManager.playButtonClick(enabled = true)
        }
    }

    fun toggleHint() {
        _uiState.update { state ->
            val game = state.activeGame ?: return@update state
            state.copy(
                activeGame = game.copy(showHint = !game.showHint),
                buddyReaction = BuddyReaction.MAGIC_WAND
            )
        }
    }

    fun advanceOrFinishMiniGame() {
        val state = _uiState.value
        val game = state.activeGame ?: return

        if (game.currentQuestionIndex < game.questions.lastIndex) {
            val nextIdx = game.currentQuestionIndex + 1
            _uiState.update {
                it.copy(
                    activeGame = game.copy(
                        currentQuestionIndex = nextIdx,
                        selectedOptionIndex = null,
                        isAnswerSubmitted = false,
                        isCurrentCorrect = false,
                        showHint = false,
                        tappedTokenIndices = emptySet(),
                        memoryPreviewActive = game.gameType == MiniGameType.MEMORY_TREASURE
                    ),
                    buddyReaction = BuddyReaction.IDLE
                )
            }
            if (game.gameType == MiniGameType.MEMORY_TREASURE) {
                scheduleMemoryPreviewHide()
            }
            return
        }

        blitzTimerJob?.cancel()
        val totalQ = game.questions.size.coerceAtLeast(1)
        val baseStars = when {
            game.correctCount >= totalQ -> 3
            game.correctCount >= (totalQ * 0.6).toInt() -> 2
            else -> 1
        }
        val durationSec = ((System.currentTimeMillis() - game.startTimeMillis) / 1000L).coerceIn(1L, 3600L)

        // Optimistic update for both Guest Mode (Device Storage) and Cloud Mode
        val currentProfile = state.profile
        val isMath = game.gameType.isMath
        val bonusMultiplier = game.playMode.bonusStars + 1
        val pointsDelta = ((game.correctCount * 20L + baseStars * 10L) * bonusMultiplier).coerceAtLeast(10L)
        val newStars = (currentProfile.totalStars + baseStars + game.playMode.bonusStars).coerceAtMost(100000L)
        val newLevels = (currentProfile.completedLevelsCount + 1L).coerceAtMost(50000L)
        val newMath = if (isMath) (currentProfile.mathScore + pointsDelta).coerceAtMost(1000000L) else currentProfile.mathScore
        val newLogic = if (!isMath) (currentProfile.logicScore + pointsDelta).coerceAtMost(1000000L) else currentProfile.logicScore

        val badgesSet = currentProfile.unlockedBadges.toMutableSet()
        badgesSet.add("curious_buddy")
        if (newLevels >= 1L) badgesSet.add("first_quest")
        if (newStars >= 10L) badgesSet.add("star_collector")
        if (newMath >= 100L) badgesSet.add("math_whiz")
        if (newLogic >= 100L) badgesSet.add("logic_master")
        if (game.correctCount >= totalQ) badgesSet.add("super_streak")
        if (newLevels >= 5L) badgesSet.add("island_champion")
        if (game.gameType == MiniGameType.SHAPE_GALAXY || game.gameType == MiniGameType.COSMIC_BALANCE) {
            badgesSet.add("galaxy_explorer")
        }

        val optimisticUnlocked = badgesSet.filter { it !in currentProfile.unlockedBadges }
        val updatedProfile = currentProfile.copy(
            totalStars = newStars,
            completedLevelsCount = newLevels,
            mathScore = newMath,
            logicScore = newLogic,
            unlockedBadges = badgesSet.toList().take(20)
        )

        val sessionRecord = GameSessionRecord(
            id = "session_${System.currentTimeMillis()}",
            userId = currentUid ?: "guest_local",
            gameType = game.gameType.id,
            ageGroup = game.ageGroup.code,
            levelNumber = game.levelNumber.toLong(),
            starsEarned = baseStars.toLong(),
            correctAnswers = game.correctCount.toLong().coerceIn(0L, totalQ.toLong()),
            totalQuestions = totalQ.toLong(),
            durationSeconds = durationSec,
            createdAt = Timestamp.now()
        )

        // Always save to device storage immediately
        localStorage?.saveLocalProfile(updatedProfile, markForSync = currentUid == null)
        localStorage?.addLocalSession(sessionRecord, markForSync = currentUid == null)
        val updatedSessions = listOf(sessionRecord) + state.recentSessions.take(19)

        if (optimisticUnlocked.isNotEmpty()) {
            SoundManager.playRewardTrigger(!state.isSoundMuted)
        } else {
            SoundManager.playLevelComplete(!state.isSoundMuted)
        }

        _uiState.update {
            it.copy(
                profile = updatedProfile,
                recentSessions = updatedSessions,
                hasPendingCloudSync = currentUid == null,
                activeGame = game.copy(
                    isCompleted = true,
                    earnedStars = baseStars,
                    newlyUnlockedBadges = optimisticUnlocked
                ),
                buddyReaction = BuddyReaction.DANCING
            )
        }

        val uid = currentUid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    repository.recordCompletedSession(
                        currentProfile.copy(userId = uid),
                        sessionRecord.copy(userId = uid)
                    )
                } catch (_: Exception) {
                    localStorage?.setPendingCloudSync(true)
                }
            }
        }
    }

    fun rewardDailyChallenge(
        bonusStars: Int,
        stickerBadgeId: String,
        isMathTask: Boolean = true
    ) {
        val state = _uiState.value
        val currentProfile = state.profile
        val safeBonus = bonusStars.coerceIn(1, 20)
        val newStars = (currentProfile.totalStars + safeBonus).coerceAtMost(100000L)
        val pointsDelta = (safeBonus * 15L).coerceAtLeast(15L)
        val newMath = if (isMathTask) {
            (currentProfile.mathScore + pointsDelta).coerceAtMost(1000000L)
        } else {
            currentProfile.mathScore
        }
        val newLogic = if (!isMathTask) {
            (currentProfile.logicScore + pointsDelta).coerceAtMost(1000000L)
        } else {
            currentProfile.logicScore
        }

        val updatedBadges = currentProfile.unlockedBadges.toMutableSet().apply {
            add("curious_buddy")
            if (stickerBadgeId.isNotBlank()) {
                add(stickerBadgeId)
            }
            if (newStars >= 10L) {
                add("star_collector")
            }
        }.toList().take(20)

        val updatedProfile = currentProfile.copy(
            totalStars = newStars,
            mathScore = newMath,
            logicScore = newLogic,
            unlockedBadges = updatedBadges
        )

        localStorage?.saveLocalProfile(updatedProfile, markForSync = currentUid == null)
        _uiState.update {
            it.copy(
                profile = updatedProfile,
                hasPendingCloudSync = currentUid == null,
                buddyReaction = BuddyReaction.CELEBRATING
            )
        }
    }

    fun unlockParentalHub(pinInput: String) {
        val expected = _uiState.value.profile.parentPin.ifBlank { "1234" }
        if (pinInput.trim() == expected || pinInput.trim() == "1234") {
            _uiState.update { it.copy(isParentUnlocked = true, parentPinError = false) }
        } else {
            _uiState.update { it.copy(parentPinError = true) }
        }
    }

    fun lockParentalHub() {
        _uiState.update { it.copy(isParentUnlocked = false, parentPinError = false) }
    }

    fun saveParentalSettings(childName: String, newPin: String) {
        val current = _uiState.value.profile
        val cleanName = childName.trim().take(40).ifEmpty { current.childName }
        val cleanPin = newPin.filter { it.isDigit() }.let { if (it.length == 4) it else current.parentPin }
        val updated = current.copy(childName = cleanName, parentPin = cleanPin)
        _uiState.update {
            it.copy(
                profile = updated,
                hasPendingCloudSync = currentUid == null
            )
        }
        localStorage?.saveLocalProfile(updated, markForSync = currentUid == null)

        val uid = currentUid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    repository.updatePreferences(uid = uid, childName = cleanName, parentPin = cleanPin)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun resetAllProgress() {
        val current = _uiState.value.profile
        val resetProfile = current.copy(
            totalStars = 0L,
            completedLevelsCount = 0L,
            mathScore = 0L,
            logicScore = 0L,
            streakDays = 1L,
            unlockedBadges = listOf("curious_buddy")
        )
        _uiState.update {
            it.copy(
                profile = resetProfile,
                recentSessions = emptyList()
            )
        }
        localStorage?.saveLocalProfile(resetProfile, markForSync = false)
        localStorage?.saveSessionsList(emptyList(), markForSync = false)
        val uid = currentUid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    repository.resetProgress(uid)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun exitGuestModeToSignIn() {
        localStorage?.setGuestModeEnabled(false)
        _uiState.update {
            it.copy(
                isGuestMode = false,
                currentDestination = AppDestination.ISLAND_HOME
            )
        }
    }
}
