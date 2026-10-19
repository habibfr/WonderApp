package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.AgeGroupConfig
import com.example.data.BuddyCharacter

/**
 * Convenience forwarding composable in `com.example.ui.components` so `OnboardingScreen`
 * can be imported from either `com.example.ui.screens` or `com.example.ui.components`.
 */
@Composable
fun OnboardingScreen(
    selectedCharacter: BuddyCharacter = BuddyCharacter.KOMODO,
    selectedAgeGroup: AgeGroupConfig = AgeGroupConfig.JUNIOR,
    languageCode: String = "id",
    isSoundMuted: Boolean = false,
    onSelectCharacter: (BuddyCharacter) -> Unit = {},
    onSelectAgeGroup: (AgeGroupConfig) -> Unit = {},
    onCompleteOnboarding: (earnedStarterStars: Int) -> Unit = {},
    onSkipOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    com.example.ui.screens.OnboardingScreen(
        selectedCharacter = selectedCharacter,
        selectedAgeGroup = selectedAgeGroup,
        languageCode = languageCode,
        isSoundMuted = isSoundMuted,
        onSelectCharacter = onSelectCharacter,
        onSelectAgeGroup = onSelectAgeGroup,
        onCompleteOnboarding = onCompleteOnboarding,
        onSkipOnboarding = onSkipOnboarding,
        modifier = modifier
    )
}
