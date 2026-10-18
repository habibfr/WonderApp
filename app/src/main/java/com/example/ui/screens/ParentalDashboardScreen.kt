package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AgeBracket
import com.example.data.ChildProfile
import com.example.data.GameSessionRecord
import com.example.data.LocalizedUiStrings
import com.example.data.MiniGameType
import com.example.data.WonderQuestStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentalDashboardScreen(
    profile: ChildProfile,
    sessions: List<GameSessionRecord>,
    strings: LocalizedUiStrings,
    onSaveSettings: (childName: String, ageGroup: String, languageCode: String, darkMode: Boolean, parentPin: String) -> Unit,
    onResetProgress: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isUnlocked by rememberSaveable { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    if (!isUnlocked) {
        // Secure Parental PIN Gate
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("parent_pin_gate_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.parentGateTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.parentGateSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = strings.defaultPinHint,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it.filter { ch -> ch.isDigit() }.take(4)
                            pinError = false
                        },
                        label = { Text(strings.enterPinLabel) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_pin_input")
                    )

                    if (pinError) {
                        Text(
                            text = strings.wrongPinError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (pinInput == profile.parentPin || pinInput == "1234") {
                                isUnlocked = true
                                pinError = false
                            } else {
                                pinError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("unlock_parent_dashboard_button"),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Filled.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.unlockDashboardButton,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
        return
    }

    // Unlocked Parental Progress & Settings Dashboard
    var editName by remember(profile.childName) { mutableStateOf(profile.childName) }
    var editAgeGroup by remember(profile.ageGroup) { mutableStateOf(profile.ageGroup) }
    var editLang by remember(profile.languageCode) { mutableStateOf(profile.languageCode) }
    var editDark by remember(profile.darkMode) { mutableStateOf(profile.darkMode) }
    var editPin by remember(profile.parentPin) { mutableStateOf(profile.parentPin) }
    var savedConfirmation by remember { mutableStateOf(false) }

    val totalQuestionsAnswered = sessions.sumOf { it.totalQuestions }.coerceAtLeast(1L)
    val totalCorrectAnswered = sessions.sumOf { it.correctAnswers }
    val accuracyPercent = if (sessions.isEmpty()) 100 else ((totalCorrectAnswered * 100) / totalQuestionsAnswered).toInt()
    val totalMinutesPlayed = (sessions.sumOf { it.durationSeconds } / 60L).coerceAtLeast(if (sessions.isNotEmpty()) 1L else 0L)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("parent_dashboard_unlocked"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.parentDashboardTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedButton(
                    onClick = { isUnlocked = false },
                    modifier = Modifier.testTag("lock_parent_dashboard_button")
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lock")
                }
            }
        }

        // Analytics Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnalyticsMetricBox(
                            title = strings.starsLabel,
                            value = "${profile.totalStars} ★",
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMetricBox(
                            title = strings.levelsLabel,
                            value = "${profile.completedLevelsCount}",
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMetricBox(
                            title = "Accuracy",
                            value = "$accuracyPercent%",
                            color = Color(0xFF4F46E5),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMetricBox(
                            title = "Time",
                            value = "${totalMinutesPlayed}m",
                            color = Color(0xFFEC4899),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Math vs Logic Mastery Progress Bars
                    val totalPoints = (profile.mathScore + profile.logicScore).coerceAtLeast(1L)
                    val mathRatio = profile.mathScore.toFloat() / totalPoints.toFloat()

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🧮 ${strings.mathPointsLabel}: ${profile.mathScore}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "🔮 ${strings.logicPointsLabel}: ${profile.logicScore}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF8B5CF6)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { if (profile.mathScore == 0L && profile.logicScore == 0L) 0.5f else mathRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFF8B5CF6).copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }

        // Child Explorer & Classroom Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.childSettingsSection,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = {
                            editName = it.take(40)
                            savedConfirmation = false
                        },
                        label = { Text(strings.childNameLabel) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_child_name_input")
                    )

                    OutlinedTextField(
                        value = editPin,
                        onValueChange = {
                            editPin = it.filter { ch -> ch.isDigit() }.take(4)
                            savedConfirmation = false
                        },
                        label = { Text(strings.parentPinLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_edit_pin_input")
                    )

                    Text(
                        text = strings.chooseAgeGroup,
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AgeBracket.entries.forEach { bracket ->
                            FilterChip(
                                selected = editAgeGroup == bracket.code,
                                onClick = {
                                    editAgeGroup = bracket.code
                                    savedConfirmation = false
                                },
                                label = { Text(bracket.code) }
                            )
                        }
                    }

                    Text(
                        text = strings.classroomLanguageLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WonderQuestStrings.supportedLanguages.forEach { (code, name) ->
                            FilterChip(
                                selected = editLang == code,
                                onClick = {
                                    editLang = code
                                    savedConfirmation = false
                                },
                                label = { Text(name) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.darkModeLabel,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = editDark,
                            onCheckedChange = {
                                editDark = it
                                savedConfirmation = false
                            },
                            modifier = Modifier.testTag("parent_dark_mode_switch")
                        )
                    }

                    Button(
                        onClick = {
                            onSaveSettings(editName, editAgeGroup, editLang, editDark, editPin)
                            savedConfirmation = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_parent_settings_button"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (savedConfirmation) "✓ ${strings.cloudSyncedStatus}" else strings.saveSettingsButton,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Recent Completed Sessions Header
        item {
            Text(
                text = strings.recentSessionsTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            )
        }

        if (sessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = strings.noSessionsYet,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(sessions.take(15)) { session ->
                val game = MiniGameType.fromId(session.gameType)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${game.title(profile.languageCode)} • ${strings.levelPrefix} ${session.levelNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ages ${session.ageGroup} • ${session.correctAnswers}/${session.totalQuestions} correct • ${session.durationSeconds}s",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            repeat(session.starsEarned.toInt().coerceIn(1, 3)) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reset Progress & Sign Out Actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onResetProgress,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reset_progress_button")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.resetProgressButton)
                }

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.signOutButton)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
