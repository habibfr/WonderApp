package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgeBracket
import com.example.data.BuddyCharacter
import com.example.data.ChildProfile
import com.example.data.GameSessionRecord
import com.example.data.WonderQuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class WonderQuestViewModel(
    private val repository: WonderQuestRepository,
    private val currentUserId: String,
    defaultDisplayName: String = "Explorer"
) : ViewModel() {

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _newlyUnlockedBadges = MutableStateFlow<List<String>>(emptyList())
    val newlyUnlockedBadges: StateFlow<List<String>> = _newlyUnlockedBadges.asStateFlow()

    val profileState: StateFlow<UiState<ChildProfile>> = repository.observeProfile(currentUserId)
        .map<ChildProfile?, UiState<ChildProfile>> { profile ->
            if (profile != null) {
                UiState.Success(profile)
            } else {
                val defaultProfile = ChildProfile(
                    id = currentUserId,
                    userId = currentUserId,
                    childName = defaultDisplayName.take(40).ifEmpty { "Explorer" }
                )
                UiState.Success(defaultProfile)
            }
        }
        .catch { error ->
            Log.w(TAG, "Error observing profile", error)
            emit(UiState.Error(error.message ?: "Failed to load explorer profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val sessionsState: StateFlow<UiState<List<GameSessionRecord>>> = repository.observeSessions(currentUserId)
        .map<List<GameSessionRecord>, UiState<List<GameSessionRecord>>> { list ->
            UiState.Success(list)
        }
        .catch { error ->
            Log.w(TAG, "Error observing sessions", error)
            emit(UiState.Error(error.message ?: "Failed to load learning sessions"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    init {
        ensureProfileInitialized(defaultDisplayName)
    }

    private fun ensureProfileInitialized(defaultDisplayName: String) {
        viewModelScope.launch {
            val existing = repository.getProfile(currentUserId).getOrNull()
            if (existing == null) {
                val initial = ChildProfile(
                    id = currentUserId,
                    userId = currentUserId,
                    childName = defaultDisplayName.trim().take(40).ifEmpty { "Explorer" }
                )
                repository.createOrInitializeProfile(initial)
            }
        }
    }

    fun currentProfileOrDefault(): ChildProfile {
        return (profileState.value as? UiState.Success)?.data
            ?: ChildProfile(id = currentUserId, userId = currentUserId)
    }

    fun updateAgeBracket(bracket: AgeBracket) {
        val current = currentProfileOrDefault()
        savePreferences(
            childName = current.childName,
            ageGroup = bracket.code,
            selectedCharacter = current.selectedCharacter,
            languageCode = current.languageCode,
            darkMode = current.darkMode,
            parentPin = current.parentPin
        )
    }

    fun updateBuddyCharacter(buddy: BuddyCharacter) {
        val current = currentProfileOrDefault()
        savePreferences(
            childName = current.childName,
            ageGroup = current.ageGroup,
            selectedCharacter = buddy.id,
            languageCode = current.languageCode,
            darkMode = current.darkMode,
            parentPin = current.parentPin
        )
    }

    fun updateLanguage(languageCode: String) {
        val current = currentProfileOrDefault()
        savePreferences(
            childName = current.childName,
            ageGroup = current.ageGroup,
            selectedCharacter = current.selectedCharacter,
            languageCode = languageCode,
            darkMode = current.darkMode,
            parentPin = current.parentPin
        )
    }

    fun toggleDarkMode() {
        val current = currentProfileOrDefault()
        savePreferences(
            childName = current.childName,
            ageGroup = current.ageGroup,
            selectedCharacter = current.selectedCharacter,
            languageCode = current.languageCode,
            darkMode = !current.darkMode,
            parentPin = current.parentPin
        )
    }

    fun savePreferences(
        childName: String,
        ageGroup: String,
        selectedCharacter: String,
        languageCode: String,
        darkMode: Boolean,
        parentPin: String
    ) {
        val current = currentProfileOrDefault()
        viewModelScope.launch {
            val result = repository.updateProfilePreferences(
                childName = childName,
                ageGroup = ageGroup,
                selectedCharacter = selectedCharacter,
                languageCode = languageCode,
                darkMode = darkMode,
                parentPin = parentPin,
                currentBadges = current.unlockedBadges
            )
            if (result.isFailure) {
                // If profile doc didn't exist yet, initialize it with these settings
                repository.createOrInitializeProfile(
                    current.copy(
                        childName = childName.trim().take(40).ifEmpty { "Explorer" },
                        ageGroup = ageGroup,
                        selectedCharacter = selectedCharacter,
                        languageCode = languageCode,
                        darkMode = darkMode,
                        parentPin = parentPin.filter { it.isDigit() }.padEnd(4, '0').take(4)
                    )
                )
            }
        }
    }

    fun recordLevelCompletion(
        gameType: String,
        levelNumber: Int,
        starsEarned: Int,
        correctAnswers: Int,
        totalQuestions: Int,
        durationSeconds: Int
    ) {
        val current = currentProfileOrDefault()
        viewModelScope.launch {
            val res = repository.recordCompletedSession(
                currentProfile = current,
                gameType = gameType,
                ageGroup = current.ageGroup,
                levelNumber = levelNumber,
                starsEarned = starsEarned,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                durationSeconds = durationSeconds
            )
            res.onSuccess { unlocked ->
                _newlyUnlockedBadges.value = unlocked
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Saved to local offline cache"
            }
        }
    }

    fun clearNewlyUnlockedBadges() {
        _newlyUnlockedBadges.value = emptyList()
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetChildProgress()
        }
    }

    fun dismissBanner() {
        _statusBanner.value = null
    }

    private companion object {
        const val TAG = "WonderQuestVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
