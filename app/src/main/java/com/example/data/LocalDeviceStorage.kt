package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Timestamp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID

/**
 * Persists ChildProfile, GameSessionRecords, and Guest Mode state on the local device storage
 * using SharedPreferences so children can play immediately in Guest Mode or offline, and
 * seamlessly auto-sync/merge their local progress into Firebase Cloud upon login.
 */
class LocalDeviceStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isGuestModeEnabled(): Boolean = prefs.getBoolean(KEY_IS_GUEST_MODE, true)

    fun setGuestModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_GUEST_MODE, enabled).apply()
    }

    fun hasPendingCloudSync(): Boolean = prefs.getBoolean(KEY_PENDING_CLOUD_SYNC, false)

    fun setPendingCloudSync(pending: Boolean) {
        prefs.edit().putBoolean(KEY_PENDING_CLOUD_SYNC, pending).apply()
    }

    fun isSoundMuted(): Boolean = prefs.getBoolean(KEY_SOUND_MUTED, false)

    fun setSoundMuted(muted: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_MUTED, muted).apply()
    }

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun loadLocalProfile(): ChildProfile {
        val raw = prefs.getString(KEY_LOCAL_PROFILE, null) ?: return ChildProfile(
            id = "local_guest_profile",
            userId = "guest_local",
            childName = "Penjelajah Kecil",
            ageGroup = "7-9",
            selectedCharacter = "komodo",
            languageCode = "id",
            darkMode = false,
            parentPin = "1234",
            totalStars = 0L,
            completedLevelsCount = 0L,
            mathScore = 0L,
            logicScore = 0L,
            streakDays = 1L,
            unlockedBadges = listOf("curious_buddy")
        )
        return try {
            val obj = JSONObject(raw)
            val badgesArray = obj.optJSONArray("unlockedBadges") ?: JSONArray()
            val badges = mutableListOf<String>()
            for (i in 0 until badgesArray.length()) {
                val badge = badgesArray.optString(i)
                if (badge.isNotBlank()) badges.add(badge)
            }
            if (badges.isEmpty()) badges.add("curious_buddy")

            ChildProfile(
                id = obj.optString("id", "local_guest_profile"),
                userId = obj.optString("userId", "guest_local"),
                childName = obj.optString("childName", "Penjelajah Kecil"),
                ageGroup = obj.optString("ageGroup", "7-9"),
                selectedCharacter = obj.optString("selectedCharacter", "komodo"),
                languageCode = obj.optString("languageCode", "id"),
                darkMode = obj.optBoolean("darkMode", false),
                parentPin = obj.optString("parentPin", "1234"),
                totalStars = obj.optLong("totalStars", 0L),
                completedLevelsCount = obj.optLong("completedLevelsCount", 0L),
                mathScore = obj.optLong("mathScore", 0L),
                logicScore = obj.optLong("logicScore", 0L),
                streakDays = obj.optLong("streakDays", 1L),
                unlockedBadges = badges.distinct()
            )
        } catch (_: Exception) {
            ChildProfile(
                id = "local_guest_profile",
                userId = "guest_local",
                selectedCharacter = "komodo",
                languageCode = "id"
            )
        }
    }

    fun saveLocalProfile(profile: ChildProfile, markForSync: Boolean = true) {
        val obj = JSONObject().apply {
            put("id", profile.id.ifBlank { "local_guest_profile" })
            put("userId", profile.userId.ifBlank { "guest_local" })
            put("childName", profile.childName)
            put("ageGroup", profile.ageGroup)
            put("selectedCharacter", profile.selectedCharacter)
            put("languageCode", profile.languageCode)
            put("darkMode", profile.darkMode)
            put("parentPin", profile.parentPin)
            put("totalStars", profile.totalStars)
            put("completedLevelsCount", profile.completedLevelsCount)
            put("mathScore", profile.mathScore)
            put("logicScore", profile.logicScore)
            put("streakDays", profile.streakDays)
            val badgesJson = JSONArray()
            profile.unlockedBadges.distinct().forEach { badgesJson.put(it) }
            put("unlockedBadges", badgesJson)
        }
        val editor = prefs.edit().putString(KEY_LOCAL_PROFILE, obj.toString())
        if (markForSync) {
            editor.putBoolean(KEY_PENDING_CLOUD_SYNC, true)
        }
        editor.apply()
    }

    fun loadLocalSessions(): List<GameSessionRecord> {
        val raw = prefs.getString(KEY_LOCAL_SESSIONS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<GameSessionRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val timestampMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                list.add(
                    GameSessionRecord(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        userId = obj.optString("userId", "guest_local"),
                        gameType = obj.optString("gameType", "math_safari"),
                        ageGroup = obj.optString("ageGroup", "7-9"),
                        levelNumber = obj.optLong("levelNumber", 1L),
                        starsEarned = obj.optLong("starsEarned", 3L),
                        correctAnswers = obj.optLong("correctAnswers", 5L),
                        totalQuestions = obj.optLong("totalQuestions", 5L),
                        durationSeconds = obj.optLong("durationSeconds", 30L),
                        createdAt = Timestamp(Date(timestampMillis))
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addLocalSession(session: GameSessionRecord, markForSync: Boolean = true) {
        val current = loadLocalSessions().toMutableList()
        val newRecord = session.copy(
            id = session.id.ifBlank { "local_${System.currentTimeMillis()}" },
            createdAt = session.createdAt ?: Timestamp.now()
        )
        current.add(0, newRecord)
        saveSessionsList(current.take(25), markForSync)
    }

    fun saveSessionsList(sessions: List<GameSessionRecord>, markForSync: Boolean = false) {
        val arr = JSONArray()
        sessions.take(25).forEach { record ->
            val obj = JSONObject().apply {
                put("id", record.id.ifBlank { UUID.randomUUID().toString() })
                put("userId", record.userId)
                put("gameType", record.gameType)
                put("ageGroup", record.ageGroup)
                put("levelNumber", record.levelNumber)
                put("starsEarned", record.starsEarned)
                put("correctAnswers", record.correctAnswers)
                put("totalQuestions", record.totalQuestions)
                put("durationSeconds", record.durationSeconds)
                put("createdAtMillis", (record.createdAt?.toDate()?.time ?: System.currentTimeMillis()))
            }
            arr.put(obj)
        }
        val editor = prefs.edit().putString(KEY_LOCAL_SESSIONS, arr.toString())
        if (markForSync) {
            editor.putBoolean(KEY_PENDING_CLOUD_SYNC, true)
        }
        editor.apply()
    }

    /**
     * Merges local device progress with cloud profile so that neither stars, scores, nor badges
     * earned in Guest Mode are lost when logging into Firebase Cloud.
     */
    fun mergeWithCloudProfile(local: ChildProfile, cloud: ChildProfile, uid: String): ChildProfile {
        val mergedBadges = (cloud.unlockedBadges + local.unlockedBadges + "curious_buddy")
            .distinct()
            .take(20)
        val mergedStars = maxOf(cloud.totalStars, local.totalStars)
        val mergedLevels = maxOf(cloud.completedLevelsCount, local.completedLevelsCount)
        val mergedMath = maxOf(cloud.mathScore, local.mathScore)
        val mergedLogic = maxOf(cloud.logicScore, local.logicScore)
        val mergedStreak = maxOf(cloud.streakDays, local.streakDays)

        val preferredName = if (local.childName.isNotBlank() && local.childName != "Penjelajah Kecil" && local.childName != "Explorer") {
            local.childName
        } else {
            cloud.childName.ifBlank { "Penjelajah Kecil" }
        }

        return cloud.copy(
            id = uid,
            userId = uid,
            childName = preferredName,
            ageGroup = local.ageGroup.ifBlank { cloud.ageGroup },
            selectedCharacter = local.selectedCharacter.ifBlank { cloud.selectedCharacter },
            languageCode = local.languageCode.ifBlank { cloud.languageCode.ifBlank { "id" } },
            darkMode = local.darkMode || cloud.darkMode,
            parentPin = local.parentPin.ifBlank { cloud.parentPin },
            totalStars = mergedStars,
            completedLevelsCount = mergedLevels,
            mathScore = mergedMath,
            logicScore = mergedLogic,
            streakDays = mergedStreak,
            unlockedBadges = mergedBadges
        )
    }

    companion object {
        private const val PREFS_NAME = "wonderquest_device_storage"
        private const val KEY_IS_GUEST_MODE = "is_guest_mode"
        private const val KEY_PENDING_CLOUD_SYNC = "pending_cloud_sync"
        private const val KEY_SOUND_MUTED = "is_sound_muted"
        private const val KEY_ONBOARDING_COMPLETED = "is_onboarding_completed"
        private const val KEY_LOCAL_PROFILE = "local_child_profile_json"
        private const val KEY_LOCAL_SESSIONS = "local_game_sessions_json"
    }
}
