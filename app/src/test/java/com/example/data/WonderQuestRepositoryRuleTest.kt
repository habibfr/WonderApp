package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WonderQuestRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveProfile_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = WonderQuestRepository(firestore)

        val initialProfile = ChildProfile(
            userId = aliceUid,
            childName = "Leo Star",
            ageGroup = "7-9",
            selectedCharacter = "sparky",
            languageCode = "en",
            darkMode = false,
            parentPin = "2468"
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrInitializeProfile(initialProfile)
        }
        assertTrue(createResult.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeProfile(aliceUid).first { it != null && it.childName == "Leo Star" }
        }
        assertNotNull(observed)
        assertEquals("Leo Star", observed?.childName)
        assertEquals("2468", observed?.parentPin)
    }

    @Test
    fun recordCompletedSession_updatesStarsAndCreatesSessionRecord() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = WonderQuestRepository(firestore)

        val profile = ChildProfile(userId = aliceUid, childName = "Mia")
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrInitializeProfile(profile).getOrThrow()
        }

        val badgesResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordCompletedSession(
                currentProfile = profile,
                gameType = "math_safari",
                ageGroup = "7-9",
                levelNumber = 1,
                starsEarned = 3,
                correctAnswers = 5,
                totalQuestions = 5,
                durationSeconds = 20
            )
        }
        assertTrue(badgesResult.isSuccess)

        val sessions = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSessions(aliceUid).first { it.isNotEmpty() }
        }
        assertTrue(sessions.any { it.gameType == "math_safari" && it.starsEarned == 3L })
    }

    @Test
    fun getProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = WonderQuestRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createOrInitializeProfile(ChildProfile(userId = aliceUid, childName = "Alice")).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = WonderQuestRepository(firestore)
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getProfile(aliceUid)
        }
        assertTrue(crossReadResult.isFailure)
        val ex = crossReadResult.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeSessions_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = WonderQuestRepository(firestore)
        val uncachedUserId = "unauth_target_${System.currentTimeMillis()}"

        var caughtException: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeSessions(uncachedUserId).first()
            }
        } catch (e: FirebaseFirestoreException) {
            caughtException = e
        }
        assertNotNull(caughtException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, caughtException?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_wonder@test.com"
        const val BOB_EMAIL = "bob_wonder@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
