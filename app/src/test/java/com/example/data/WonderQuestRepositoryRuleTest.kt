package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WonderQuestRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private lateinit var repository: WonderQuestRepository

    @Before
    fun initRepository() {
        repository = WonderQuestRepository(firestore)
    }

    @Test
    fun ensureChildProfile_createsValidDocumentWithIndonesianDefault() = runTest {
        val uid = signInTestUser("kid1@wonderquest.test")
        val profile = repository.ensureChildProfile(uid, "Maya")

        assertEquals(uid, profile.userId)
        assertEquals("Maya", profile.childName)
        assertEquals("id", profile.languageCode)

        val snapshot = firestore.collection("child_profiles").document(uid).get().await()
        assertTrue(snapshot.exists())
        assertEquals("Maya", snapshot.getString("childName"))
        assertEquals("id", snapshot.getString("languageCode"))
        assertEquals("7-9", snapshot.getString("ageGroup"))
    }

    @Test
    fun syncLocalProgressToCloud_mergesGuestDeviceDataIntoFirestore() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = LocalDeviceStorage(context)
        val guestProfile = ChildProfile(
            id = "local_guest_profile",
            userId = "guest_local",
            childName = "Bintang",
            ageGroup = "4-6",
            selectedCharacter = "komodo",
            languageCode = "id",
            totalStars = 12L,
            completedLevelsCount = 4L,
            mathScore = 180L,
            logicScore = 120L,
            unlockedBadges = listOf("curious_buddy", "first_quest", "star_collector")
        )
        val guestSessions = listOf(
            GameSessionRecord(
                id = "local_1",
                userId = "guest_local",
                gameType = "shape_galaxy",
                ageGroup = "4-6",
                levelNumber = 1L,
                starsEarned = 3L,
                correctAnswers = 5L,
                totalQuestions = 5L,
                durationSeconds = 22L
            )
        )
        storage.saveLocalProfile(guestProfile, markForSync = true)
        storage.saveSessionsList(guestSessions, markForSync = true)

        val uid = signInTestUser("guest_to_cloud@wonderquest.test")
        val synced = repository.syncLocalProgressToCloud(uid, guestProfile, guestSessions, storage)

        assertEquals("Bintang", synced.childName)
        assertEquals("komodo", synced.selectedCharacter)
        assertEquals(12L, synced.totalStars)

        val snapshot = firestore.collection("child_profiles").document(uid).get().await()
        assertTrue(snapshot.exists())
        assertEquals(12L, snapshot.getLong("totalStars"))
        assertEquals("komodo", snapshot.getString("selectedCharacter"))
    }

    @Test
    fun updatePreferences_updatesKomodoAndIndonesianPreferences() = runTest {
        val uid = signInTestUser("kid2@wonderquest.test")
        repository.ensureChildProfile(uid, "Leo")

        repository.updatePreferences(
            uid = uid,
            childName = "Leo Star",
            ageGroup = "10-13",
            selectedCharacter = "komodo",
            languageCode = "id",
            darkMode = true,
            parentPin = "9876"
        )

        val snapshot = firestore.collection("child_profiles").document(uid).get().await()
        assertEquals("Leo Star", snapshot.getString("childName"))
        assertEquals("10-13", snapshot.getString("ageGroup"))
        assertEquals("komodo", snapshot.getString("selectedCharacter"))
        assertEquals("id", snapshot.getString("languageCode"))
        assertEquals(true, snapshot.getBoolean("darkMode"))
        assertEquals("9876", snapshot.getString("parentPin"))
    }

    @Test
    fun recordCompletedSession_savesNewGalaxySessionAndIncrementsProfileStats() = runTest {
        val uid = signInTestUser("kid3@wonderquest.test")
        val initial = repository.ensureChildProfile(uid, "Aria")

        val session = GameSessionRecord(
            userId = uid,
            gameType = "cosmic_balance",
            ageGroup = "7-9",
            levelNumber = 1L,
            starsEarned = 3L,
            correctAnswers = 5L,
            totalQuestions = 5L,
            durationSeconds = 25L
        )

        val unlocked = repository.recordCompletedSession(initial, session)
        assertTrue(unlocked.contains("first_quest"))
        assertTrue(unlocked.contains("super_streak"))
        assertTrue(unlocked.contains("galaxy_explorer"))

        val updatedSnap = firestore.collection("child_profiles").document(uid).get().await()
        assertEquals(3L, updatedSnap.getLong("totalStars"))
        assertEquals(1L, updatedSnap.getLong("completedLevelsCount"))
    }

    @Test
    fun crossUserReadAndWrite_isDeniedBySecurityRules() = runTest {
        val ownerUid = signInTestUser("owner@wonderquest.test")
        repository.ensureChildProfile(ownerUid, "OwnerKid")

        signInTestUser("intruder@wonderquest.test")
        try {
            firestore.collection("child_profiles").document(ownerUid).get().await()
            fail("Expected PERMISSION_DENIED when reading another user's profile")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }
}
