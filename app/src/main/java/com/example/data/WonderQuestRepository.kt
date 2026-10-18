package com.example.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WonderQuestRepository(
    customDb: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy { customDb ?: FirebaseFirestore.getInstance() }
    private val profilesCollection by lazy { db.collection("child_profiles") }
    private val sessionsCollection by lazy { db.collection("game_sessions") }

    fun observeChildProfile(uid: String): Flow<ChildProfile?> = callbackFlow {
        val registration: ListenerRegistration = profilesCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    FirestoreErrorHandler.handleError(error, OperationType.GET, "child_profiles/$uid")
                    close(error)
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject(ChildProfile::class.java)
                trySend(profile)
            }
        awaitClose { registration.remove() }
    }

    fun observeRecentSessions(uid: String, limitCount: Long = 15L): Flow<List<GameSessionRecord>> = callbackFlow {
        val registration: ListenerRegistration = sessionsCollection
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limitCount)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    FirestoreErrorHandler.handleError(error, OperationType.LIST, "game_sessions")
                    close(error)
                    return@addSnapshotListener
                }
                val records = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GameSessionRecord::class.java)
                } ?: emptyList()
                trySend(records)
            }
        awaitClose { registration.remove() }
    }

    suspend fun ensureChildProfile(uid: String, defaultName: String = "Penjelajah Kecil"): ChildProfile {
        val docRef = profilesCollection.document(uid)
        return try {
            val snapshot = docRef.get().await()
            val existing = snapshot.toObject(ChildProfile::class.java)
            if (existing != null) {
                existing
            } else {
                val initial = ChildProfile(
                    id = uid,
                    userId = uid,
                    childName = defaultName.trim().take(40).ifEmpty { "Penjelajah Kecil" },
                    ageGroup = "7-9",
                    selectedCharacter = "sparky",
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
                docRef.set(initial.toCreatePayload(uid)).await()
                initial
            }
        } catch (e: Exception) {
            FirestoreErrorHandler.handleError(e, OperationType.CREATE, "child_profiles/$uid")
            throw e
        }
    }

    /**
     * Automatically merges and syncs device-saved Guest Mode progress into Firebase Firestore
     * when the user logs in with Google.
     */
    suspend fun syncLocalProgressToCloud(
        uid: String,
        localProfile: ChildProfile,
        localSessions: List<GameSessionRecord>,
        localStorage: LocalDeviceStorage
    ): ChildProfile {
        val docRef = profilesCollection.document(uid)
        return try {
            val snapshot = docRef.get().await()
            val existingCloud = snapshot.toObject(ChildProfile::class.java)
            val finalProfile = if (existingCloud != null) {
                val merged = localStorage.mergeWithCloudProfile(localProfile, existingCloud, uid)
                val validLang = if (merged.languageCode in listOf("id", "en", "es", "fr", "hi")) merged.languageCode else "id"
                val validChar = if (merged.selectedCharacter in listOf("sparky", "luna", "robobit", "sunny", "komodo")) merged.selectedCharacter else "sparky"
                val validAge = if (merged.ageGroup in listOf("4-6", "7-9", "10-13")) merged.ageGroup else "7-9"
                docRef.update(
                    mapOf(
                        "childName" to merged.childName.trim().take(40).ifEmpty { "Penjelajah Kecil" },
                        "ageGroup" to validAge,
                        "selectedCharacter" to validChar,
                        "languageCode" to validLang,
                        "darkMode" to merged.darkMode,
                        "parentPin" to merged.parentPin.filter { it.isDigit() }.padEnd(4, '0').take(4),
                        "totalStars" to merged.totalStars.coerceIn(0L, 100000L),
                        "completedLevelsCount" to merged.completedLevelsCount.coerceIn(0L, 50000L),
                        "mathScore" to merged.mathScore.coerceIn(0L, 1000000L),
                        "logicScore" to merged.logicScore.coerceIn(0L, 1000000L),
                        "streakDays" to merged.streakDays.coerceIn(0L, 3650L),
                        "unlockedBadges" to merged.unlockedBadges.distinct().take(20),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                merged
            } else {
                val initialMerged = localProfile.copy(id = uid, userId = uid)
                docRef.set(initialMerged.toCreatePayload(uid)).await()
                initialMerged
            }

            if (localStorage.hasPendingCloudSync() && localSessions.isNotEmpty()) {
                for (session in localSessions.take(5)) {
                    val sessionRef = sessionsCollection.document()
                    sessionRef.set(session.toCreatePayload(uid)).await()
                }
            }
            localStorage.setPendingCloudSync(false)
            finalProfile
        } catch (e: Exception) {
            FirestoreErrorHandler.handleError(e, OperationType.UPDATE, "child_profiles/$uid")
            throw e
        }
    }

    suspend fun updatePreferences(
        uid: String,
        childName: String? = null,
        ageGroup: String? = null,
        selectedCharacter: String? = null,
        languageCode: String? = null,
        darkMode: Boolean? = null,
        parentPin: String? = null
    ) {
        val docRef = profilesCollection.document(uid)
        val updates = mutableMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        childName?.let {
            val cleaned = it.trim().take(40)
            if (cleaned.isNotEmpty()) updates["childName"] = cleaned
        }
        ageGroup?.let {
            if (it in listOf("4-6", "7-9", "10-13")) updates["ageGroup"] = it
        }
        selectedCharacter?.let {
            if (it in listOf("sparky", "luna", "robobit", "sunny", "komodo")) updates["selectedCharacter"] = it
        }
        languageCode?.let {
            if (it in listOf("id", "en", "es", "fr", "hi")) updates["languageCode"] = it
        }
        darkMode?.let { updates["darkMode"] = it }
        parentPin?.let {
            val digits = it.filter { ch -> ch.isDigit() }
            if (digits.length == 4) updates["parentPin"] = digits
        }

        try {
            docRef.update(updates).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleError(e, OperationType.UPDATE, "child_profiles/$uid")
            throw e
        }
    }

    suspend fun recordCompletedSession(
        currentProfile: ChildProfile,
        session: GameSessionRecord
    ): List<String> {
        val uid = currentProfile.userId
        val isMath = MiniGameType.fromId(session.gameType).isMath
        val pointsDelta = (session.correctAnswers * 20L + session.starsEarned * 10L).coerceAtLeast(10L)

        val newStars = (currentProfile.totalStars + session.starsEarned).coerceAtMost(100000L)
        val newLevels = (currentProfile.completedLevelsCount + 1L).coerceAtMost(50000L)
        val newMath = if (isMath) (currentProfile.mathScore + pointsDelta).coerceAtMost(1000000L) else currentProfile.mathScore
        val newLogic = if (!isMath) (currentProfile.logicScore + pointsDelta).coerceAtMost(1000000L) else currentProfile.logicScore

        val badges = currentProfile.unlockedBadges.toMutableSet()
        badges.add("curious_buddy")
        if (newLevels >= 1L) badges.add("first_quest")
        if (newStars >= 10L) badges.add("star_collector")
        if (newMath >= 100L) badges.add("math_whiz")
        if (newLogic >= 100L) badges.add("logic_master")
        if (session.correctAnswers >= session.totalQuestions) badges.add("super_streak")
        if (newLevels >= 5L) badges.add("island_champion")
        if (session.gameType == MiniGameType.SHAPE_GALAXY.id || session.gameType == MiniGameType.COSMIC_BALANCE.id) {
            badges.add("galaxy_explorer")
        }

        val newlyUnlocked = badges.filter { it !in currentProfile.unlockedBadges }
        val finalBadges = badges.toList().take(20)

        val sessionDoc = sessionsCollection.document()
        try {
            sessionDoc.set(session.toCreatePayload(uid)).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleError(e, OperationType.CREATE, "game_sessions/${sessionDoc.id}")
            throw e
        }

        val profileDoc = profilesCollection.document(uid)
        val updates = mapOf(
            "totalStars" to newStars,
            "completedLevelsCount" to newLevels,
            "mathScore" to newMath,
            "logicScore" to newLogic,
            "unlockedBadges" to finalBadges,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            profileDoc.update(updates).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleError(e, OperationType.UPDATE, "child_profiles/$uid")
            throw e
        }

        return newlyUnlocked
    }

    suspend fun resetProgress(uid: String) {
        val profileDoc = profilesCollection.document(uid)
        val updates = mapOf(
            "totalStars" to 0L,
            "completedLevelsCount" to 0L,
            "mathScore" to 0L,
            "logicScore" to 0L,
            "streakDays" to 1L,
            "unlockedBadges" to listOf("curious_buddy"),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            profileDoc.update(updates).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleError(e, OperationType.UPDATE, "child_profiles/$uid")
            throw e
        }
    }

    suspend fun deleteSession(sessionId: String) {
        try {
            sessionsCollection.document(sessionId).delete().await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleError(e, OperationType.DELETE, "game_sessions/$sessionId")
            throw e
        }
    }
}
