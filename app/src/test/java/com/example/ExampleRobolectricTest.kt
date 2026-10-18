package com.example

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BuddyReaction
import com.example.data.MiniGameType
import com.example.ui.AppDestination
import com.example.ui.WonderQuestViewModel
import com.example.ui.theme.WonderQuestTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun welcomeScreen_defaultsToIndonesianAndEntersGuestMode() {
        val viewModel = WonderQuestViewModel()
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            WonderQuestTheme(darkTheme = uiState.profile.darkMode) {
                if (!uiState.isGuestMode) {
                    com.example.ui.auth.WonderQuestSignInScreen(
                        languageCode = uiState.profile.languageCode,
                        onLanguageSelected = { viewModel.selectLanguage(it) },
                        onSignInRequest = { Result.failure(IllegalStateException("Test")) },
                        onGuestModeStart = { viewModel.enterGuestMode() }
                    )
                } else {
                    WonderQuestAppContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onSignOut = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        assertEquals("id", viewModel.uiState.value.profile.languageCode)
        composeTestRule.onNodeWithTag("welcome_buddy_canvas", useUnmergedTree = true).assertExists()

        // Test multilingual language switching on welcome screen
        composeTestRule.onNodeWithTag("welcome_lang_en").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals("en", viewModel.uiState.value.profile.languageCode)

        composeTestRule.onNodeWithTag("welcome_lang_id").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals("id", viewModel.uiState.value.profile.languageCode)

        composeTestRule.onNodeWithTag("guest_play_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("google_sign_in_button").performScrollTo().assertIsDisplayed()

        // Tap Guest Mode button and verify transition to Island Home Screen
        composeTestRule.onNodeWithTag("guest_play_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(viewModel.uiState.value.isGuestMode)
        composeTestRule.onNodeWithTag("storage_sync_status_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hero_buddy_card").assertExists()
    }

    @Test
    fun islandAndNewMiniGames_completeQuestAndUnlockBadgesInGuestMode() {
        val viewModel = WonderQuestViewModel()
        viewModel.enterGuestMode()

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            WonderQuestTheme(darkTheme = uiState.profile.darkMode) {
                WonderQuestAppContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onSignOut = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hero_buddy_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("storage_sync_status_banner").assertIsDisplayed()

        // Launch Shape Galaxy mini-game
        viewModel.startMiniGame(MiniGameType.SHAPE_GALAXY, customLevel = 1)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("minigame_play_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("polygon_galaxy_canvas").assertIsDisplayed()

        // Tap visual token #0 to test interactive counting
        composeTestRule.onNodeWithTag("visual_token_0").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(0 in (viewModel.uiState.value.activeGame?.tappedTokenIndices ?: emptySet()))

        // Complete all questions
        val totalQ = viewModel.uiState.value.activeGame?.questions?.size ?: 5
        repeat(totalQ) {
            val active = viewModel.uiState.value.activeGame!!
            val correctIdx = active.currentQuestion.correctIndex
            viewModel.submitOption(correctIdx)
            viewModel.advanceOrFinishMiniGame()
        }
        composeTestRule.waitForIdle()

        val completedGame = viewModel.uiState.value.activeGame!!
        assertTrue(completedGame.isCompleted)
        assertEquals(3, completedGame.earnedStars)
        assertTrue(viewModel.uiState.value.profile.unlockedBadges.contains("galaxy_explorer"))
    }

    @Test
    fun buddiesBadgesAndParentalGate_interactivePropsAndUnlockFlow() {
        val viewModel = WonderQuestViewModel()
        viewModel.enterGuestMode()
        viewModel.navigateTo(AppDestination.BUDDIES_ROOM)

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            WonderQuestTheme {
                WonderQuestAppContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onSignOut = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("active_buddy_stage_canvas").assertIsDisplayed()
        viewModel.triggerBuddyReaction(BuddyReaction.EATING_TREAT)
        composeTestRule.waitForIdle()
        assertEquals(1, viewModel.uiState.value.treatsFedCount)

        // Select Komodo buddy
        viewModel.selectBuddy("komodo")
        assertEquals("komodo", viewModel.uiState.value.profile.selectedCharacter)

        // Navigate to Parental Hub and unlock with default PIN 1234
        viewModel.navigateTo(AppDestination.PARENTAL_HUB)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("parental_pin_gate").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parent_gate_pin_input").performScrollTo().performTextReplacement("1234")
        composeTestRule.onNodeWithTag("unlock_parent_hub_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertTrue(viewModel.uiState.value.isParentUnlocked)
    }
}
