package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.example.ui.components.WonderQuestIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.GameSessionRecord
import com.example.data.MiniGameType
import com.example.data.WonderQuestStrings
import com.example.ui.WonderQuestUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentalDashboardScreen(
    uiState: WonderQuestUiState,
    onUnlockWithPin: (String) -> Unit,
    onSaveSettings: (String, String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onResetProgress: () -> Unit,
    onSignOut: () -> Unit,
    onLinkCloudAccount: () -> Unit = {},
    onBackToIsland: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToIsland)

    val strings = uiState.strings
    val profile = uiState.profile

    if (!uiState.isParentUnlocked) {
        ParentalPinGate(
            strings = strings,
            hasError = uiState.parentPinError,
            onUnlock = onUnlockWithPin,
            modifier = modifier
        )
        return
    }

    var childNameInput by rememberSaveable(profile.childName) { mutableStateOf(profile.childName) }
    var pinInput by rememberSaveable(profile.parentPin) { mutableStateOf(profile.parentPin) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("parental_dashboard_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Cloud Sync & Local Device Storage Status Banner
        item {
            Card(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isCloudConnected) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isCloudConnected) Icons.Default.CloudDone else Icons.Default.SdStorage,
                            contentDescription = null,
                            tint = if (uiState.isCloudConnected) Color(0xFF00796B) else Color(0xFFE65100),
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.parentDashboardTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (uiState.isCloudConnected) {
                                    "${strings.cloudSyncedStatus} • ${strings.offlineReadyStatus}"
                                } else {
                                    strings.guestSavedOnDeviceStatus
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    if (!uiState.isCloudConnected) {
                        Button(
                            onClick = onLinkCloudAccount,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("link_cloud_account_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.linkCloudAccountButton,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Visual Analytics: Math Score, Logic Score, Total Stars & Levels
        item {
            Card(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardMetricBox(
                            title = strings.mathPointsLabel,
                            value = "${profile.mathScore}",
                            accent = Color(0xFFFF7043),
                            icon = Icons.Default.Calculate,
                            modifier = Modifier.weight(1f).testTag("dashboard_math_score")
                        )
                        DashboardMetricBox(
                            title = strings.logicPointsLabel,
                            value = "${profile.logicScore}",
                            accent = Color(0xFF7E57C2),
                            icon = Icons.Default.Psychology,
                            modifier = Modifier.weight(1f).testTag("dashboard_logic_score")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardMetricBox(
                            title = strings.starsLabel,
                            value = "${profile.totalStars}",
                            accent = Color(0xFFFFB300),
                            icon = Icons.Default.Star,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricBox(
                            title = strings.levelsLabel,
                            value = "${profile.completedLevelsCount}",
                            accent = Color(0xFF26A69A),
                            icon = Icons.Default.EmojiEvents,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Visual Skill Progress Bars
                    val totalPoints = (profile.mathScore + profile.logicScore).coerceAtLeast(1L)
                    val mathRatio = (profile.mathScore.toFloat() / totalPoints.toFloat()).coerceIn(0.15f, 0.85f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${strings.mathPointsLabel} vs ${strings.logicPointsLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { mathRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Color(0xFFFF7043),
                            trackColor = Color(0xFF7E57C2)
                        )
                    }
                }
            }
        }

        // 3. Profile & Classroom Preferences Card
        item {
            Card(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.childSettingsSection,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )

                    OutlinedTextField(
                        value = childNameInput,
                        onValueChange = { childNameInput = it.take(40) },
                        label = { Text(strings.childNameLabel) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_child_name_input")
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { next ->
                            pinInput = next.filter { it.isDigit() }.take(4)
                        },
                        label = { Text(strings.parentPinLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_pin_setting_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.darkModeLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = profile.darkMode,
                            onCheckedChange = onToggleDarkMode,
                            modifier = Modifier.testTag("parent_dark_mode_switch")
                        )
                    }

                    Text(
                        text = strings.classroomLanguageLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WonderQuestStrings.supportedLanguages.forEach { (code, label) ->
                            FilterChip(
                                selected = profile.languageCode == code,
                                onClick = { onSelectLanguage(code) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("parent_lang_chip_$code")
                            )
                        }
                    }

                    Button(
                        onClick = { onSaveSettings(childNameInput, pinInput) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_parent_settings_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.saveSettingsButton, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Recent Learning Sessions Header
        item {
            Text(
                text = strings.recentSessionsTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
            )
        }

        if (uiState.recentSessions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = strings.noSessionsYet,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(18.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(uiState.recentSessions, key = { it.id.ifEmpty { it.hashCode().toString() } }) { session ->
                SessionHistoryRow(
                    session = session,
                    languageCode = profile.languageCode,
                    levelPrefix = strings.levelPrefix,
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                )
            }
        }

        // 5. Reset & Sign Out / Switch Account Actions
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onResetProgress,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("reset_progress_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.resetProgressButton, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.signOutButton, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ParentalPinGate(
    strings: com.example.data.LocalizedUiStrings,
    hasError: Boolean,
    onUnlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("parental_pin_gate"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll( androidx.compose.foundation.rememberScrollState() )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = strings.parentGateTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = strings.parentGateSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = strings.defaultPinHint,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                OutlinedTextField(
                    value = pin,
                    onValueChange = { next -> pin = next.filter { it.isDigit() }.take(4) },
                    label = { Text(strings.enterPinLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = hasError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parent_gate_pin_input")
                )

                if (hasError) {
                    Text(
                        text = strings.wrongPinError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("parent_pin_error_text")
                    )
                }

                Button(
                    onClick = { onUnlock(pin) },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("unlock_parent_hub_button")
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.unlockDashboardButton, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricBox(
    title: String,
    value: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SessionHistoryRow(
    session: GameSessionRecord,
    languageCode: String,
    levelPrefix: String,
    modifier: Modifier = Modifier
) {
    val gameType = MiniGameType.fromId(session.gameType)
    val accent = Color(gameType.accentColorHex)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accent.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        WonderQuestIcon(
                            iconKey = gameType.iconKey,
                            tint = accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = gameType.localizedTitle(languageCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$levelPrefix ${session.levelNumber} • ${session.correctAnswers}/${session.totalQuestions} (${session.durationSeconds}s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+${session.starsEarned}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
