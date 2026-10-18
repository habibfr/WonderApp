package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WonderQuestRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeProfile(userId: String): Flow<ChildProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        ChildProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    fun observeSessions(userId: String): Flow<List<GameSessionRecord>> {
        val collectionPath = "users/$userId/sessions"
        return db.collection("users")
            .document(userId)
            .collection("sessions")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.toObjects(
                    GameSessionRecord::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, collectionPath)
                }
                throw error
            }
    }

    suspend fun getProfile(userId: String): Result<ChildProfile?> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(
                    ChildProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createOrInitializeProfile(profile: ChildProfile): Result<ChildProfile> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val payload = profile.toCreatePayload(uid)
            docRef.set(payload).await()
            profile.copy(id = uid, userId = uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateProfilePreferences(
        childName: String,
        ageGroup: String,
        selectedCharacter: String,
        languageCode: String,
        darkMode: Boolean,
        parentPin: String,
        currentBadges: List<String>
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val updatedBadges = if (languageCode != "en" && !currentBadges.contains("polyglot")) {
            (currentBadges + "polyglot").distinct().take(20)
        } else {
            currentBadges.take(20)
        }
        val updates = mapOf(
            "childName" to childName.trim().take(40).ifEmpty { "Explorer" },
            "ageGroup" to ageGroup,
            "selectedCharacter" to selectedCharacter,
            "languageCode" to languageCode,
            "darkMode" to darkMode,
            "parentPin" to parentPin.filter { it.isDigit() }.padEnd(4, '0').take(4),
            "unlockedBadges" to updatedBadges,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun recordCompletedSession(
        currentProfile: ChildProfile,
        gameType: String,
        ageGroup: String,
        levelNumber: Int,
        starsEarned: Int,
        correctAnswers: Int,
        totalQuestions: Int,
        durationSeconds: Int
    ): Result<List<String>> = runCatching {
        val uid = requireUserId()
        val profileRef = db.collection("users").document(uid)
        val sessionsCollection = profileRef.collection("sessions")
        val newSessionDoc = sessionsCollection.document()

        val isMathGame = gameType == "math_safari" || gameType == "number_bridge"
        val pointsEarned = (correctAnswers * 15 + starsEarned * 10).toLong()
        val newTotalStars = (currentProfile.totalStars + starsEarned).coerceAtMost(100000L)
        val newCompletedLevels = (currentProfile.completedLevelsCount + 1L).coerceAtMost(50000L)
        val newMathScore = if (isMathGame) {
            (currentProfile.mathScore + pointsEarned).coerceAtMost(1000000L)
        } else {
            currentProfile.mathScore
        }
        val newLogicScore = if (!isMathGame) {
            (currentProfile.logicScore + pointsEarned).coerceAtMost(1000000L)
        } else {
            currentProfile.logicScore
        }

        val badges = currentProfile.unlockedBadges.toMutableList()
        val newlyEarned = mutableListOf<String>()

        fun unlock(badgeId: String) {
            if (!badges.contains(badgeId) && badges.size < 20) {
                badges.add(badgeId)
                newlyEarned.add(badgeId)
            }
        }

        unlock("curious_buddy")
        unlock("first_star")
        if (starsEarned >= 3) unlock("perfect_three")
        if (newMathScore >= 100L) unlock("math_whiz")
        if (newLogicScore >= 100L) unlock("logic_master")
        if (newTotalStars >= 15L) unlock("island_champion")
        if (starsEarned >= 3 && durationSeconds <= 25) unlock("speed_solver")

        val sessionRecord = GameSessionRecord(
            userId = uid,
            gameType = gameType,
            ageGroup = ageGroup,
            levelNumber = levelNumber.toLong(),
            starsEarned = starsEarned.toLong(),
            correctAnswers = correctAnswers.toLong(),
            totalQuestions = totalQuestions.toLong(),
            durationSeconds = durationSeconds.toLong()
        )

        try {
            newSessionDoc.set(sessionRecord.toCreatePayload(uid)).await()
            val profileUpdates = mapOf(
                "totalStars" to newTotalStars,
                "completedLevelsCount" to newCompletedLevels,
                "mathScore" to newMathScore,
                "logicScore" to newLogicScore,
                "unlockedBadges" to badges.take(20),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            profileRef.update(profileUpdates).await()
            newlyEarned
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, newSessionDoc.path)
            throw e
        }
    }

    suspend fun resetChildProgress(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val profileRef = db.collection("users").document(uid)
        val updates = mapOf(
            "totalStars" to 0L,
            "completedLevelsCount" to 0L,
            "mathScore" to 0L,
            "logicScore" to 0L,
            "unlockedBadges" to listOf("curious_buddy"),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            profileRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, profileRef.path)
            throw e
        }
    }
}
