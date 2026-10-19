package com.example

import androidx.compose.foundation.verticalScroll
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

    @Test
    fun weeklyActivityChart_visualizesMathAndLogicProblemsSolvedAndSupportsToggles() {
        val viewModel = WonderQuestViewModel()
        viewModel.enterGuestMode()

        // Complete a Math game (MATH_SAFARI) and a Logic game (PATTERN_PORTAL)
        viewModel.startMiniGame(MiniGameType.MATH_SAFARI, customLevel = 1)
        val mathCount = viewModel.uiState.value.activeGame?.questions?.size ?: 5
        repeat(mathCount) {
            val active = viewModel.uiState.value.activeGame!!
            viewModel.submitOption(active.currentQuestion.correctIndex)
            viewModel.advanceOrFinishMiniGame()
        }

        viewModel.startMiniGame(MiniGameType.PATTERN_PORTAL, customLevel = 1)
        val logicCount = viewModel.uiState.value.activeGame?.questions?.size ?: 5
        repeat(logicCount) {
            val active = viewModel.uiState.value.activeGame!!
            viewModel.submitOption(active.currentQuestion.correctIndex)
            viewModel.advanceOrFinishMiniGame()
        }

        viewModel.navigateTo(AppDestination.BADGES_TROPHIES)

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

        composeTestRule.onNodeWithTag("weekly_activity_chart_card").assertExists()
        composeTestRule.onNodeWithTag("recharts_responsive_canvas").assertExists()
        composeTestRule.onNodeWithTag("recharts_tooltip_card").assertExists()

        // Toggle chart style to Bar Chart and back to Area Chart
        composeTestRule.onNodeWithTag("chart_style_toggle_bar").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chart_style_toggle_area").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Filter series by Math Only, Logic Only, and Both
        composeTestRule.onNodeWithTag("chart_series_filter_math").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chart_series_filter_logic").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chart_series_filter_all").performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun dailyChallenges_displaysThreeRandomizedTasksAndRewardsBonusStarsAndStickers() {
        var totalRewardStars = 0
        val unlockedStickers = mutableListOf<String>()

        composeTestRule.setContent {
            WonderQuestTheme {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier.verticalScroll(
                        androidx.compose.foundation.rememberScrollState()
                    )
                ) {
                    com.example.ui.components.DailyChallenges(
                        ageGroup = com.example.data.AgeGroupConfig.JUNIOR,
                        languageCode = "id",
                        onRewardEarned = { stars, badgeId, _ ->
                            totalRewardStars += stars
                            unlockedStickers.add(badgeId)
                        },
                        onAllThreeCompletedBonus = { grandBonus, grandBadge ->
                            totalRewardStars += grandBonus
                            unlockedStickers.add(grandBadge)
                        }
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // Verify all 3 daily challenge task cards and sticker slots are rendered
        composeTestRule.onNodeWithTag("daily_challenges_section").assertExists()
        composeTestRule.onNodeWithTag("daily_challenge_card_0").assertExists()
        composeTestRule.onNodeWithTag("daily_challenge_card_1").assertExists()
        composeTestRule.onNodeWithTag("daily_challenge_card_2").assertExists()
        composeTestRule.onNodeWithTag("daily_sticker_slot_0").assertExists()
        composeTestRule.onNodeWithTag("daily_sticker_slot_1").assertExists()
        composeTestRule.onNodeWithTag("daily_sticker_slot_2").assertExists()

        // Solve all 3 randomized tasks using the deterministic seed=1 task list
        val expectedTasks = com.example.ui.components.fetchDailyChallengeTasks(
            ageGroup = com.example.data.AgeGroupConfig.JUNIOR,
            languageCode = "id",
            seed = 1
        )
        assertEquals(3, expectedTasks.size)

        expectedTasks.forEachIndexed { taskIdx, task ->
            val correctIdx = task.question.correctIndex
            composeTestRule
                .onNodeWithTag("daily_task_${taskIdx}_option_$correctIdx")
                .performScrollTo()
                .performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("daily_task_${taskIdx}_completed_badge").assertExists()
        }

        // Verify bonus stars (2 + 3 + 4 + 5 grand bonus = 14) and stickers were awarded
        assertEquals(14, totalRewardStars)
        assertTrue(unlockedStickers.contains("star_collector"))
        assertTrue(unlockedStickers.contains("first_quest"))
        assertTrue(unlockedStickers.contains("super_streak"))
        assertTrue(unlockedStickers.contains("island_champion"))
        composeTestRule.onNodeWithTag("daily_challenges_all_complete_banner").assertExists()
    }

    @Test
    fun rewardOverlay_displaysEarnedStarsUnlockedBadgesAndTriggersActions() {
        var nextLevelClicked = false
        var dismissClicked = false

        composeTestRule.setContent {
            WonderQuestTheme {
                com.example.ui.components.RewardOverlay(
                    visible = true,
                    title = "Level Selesai!",
                    subtitle = "Safari Matematika • Level 1",
                    earnedStars = 3,
                    maxStars = 3,
                    correctAnswers = 5,
                    totalQuestions = 5,
                    unlockedBadgeIds = listOf("first_quest", "super_streak"),
                    onPrimaryAction = { nextLevelClicked = true },
                    onDismiss = { dismissClicked = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("reward_overlay_modal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reward_star_medallion_1").assertExists()
        composeTestRule.onNodeWithTag("reward_star_medallion_2").assertExists()
        composeTestRule.onNodeWithTag("reward_star_medallion_3").assertExists()
        composeTestRule.onNodeWithTag("reward_unlocked_badges_row").assertExists()
        composeTestRule.onNodeWithTag("reward_badge_chip_first_quest").assertExists()
        composeTestRule.onNodeWithTag("reward_badge_chip_super_streak").assertExists()

        composeTestRule.onNodeWithTag("play_next_level_button").performScrollTo().performClick()
        assertTrue(nextLevelClicked)

        composeTestRule.onNodeWithTag("back_to_island_button").performScrollTo().performClick()
        assertTrue(dismissClicked)
    }
}
