package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Centralized Kid-Friendly Sound & Upbeat Background Music Manager for WonderQuest.
 *
 * Provides:
 * 1. Continuous, upbeat multi-voice background music ([MusicTheme.ISLAND_THEME] on app entry/hub
 *    and [MusicTheme.GAMEPLAY_QUEST] during active mini-games) synthesized in real time with
 *    lead marimba melody + bouncy bass harmony + rhythmic pulse.
 * 2. Instant musical SFX chimes for button clicks, counting tokens, correct/retry answers,
 *    level completions, and badge/buddy rewards.
 * 3. Lifecycle-aware pause/resume and global [isMuted] control.
 */
object SoundManager {

    enum class MusicTheme {
        ISLAND_THEME,
        GAMEPLAY_QUEST
    }

    private data class MelodyStep(
        val leadHz: Double,
        val bassHz: Double,
        val durationMs: Int
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var currentTheme: MusicTheme = MusicTheme.ISLAND_THEME
    private var isAppInForeground: Boolean = true
    private var bgmJob: Job? = null

    // Upbeat Tropical Island Adventure Theme (~136 BPM) - plays automatically upon entering the app
    private val islandAdventureMelody = listOf(
        MelodyStep(523.25, 261.63, 210), // C5 + C4
        MelodyStep(659.25, 392.00, 210), // E5 + G4
        MelodyStep(783.99, 261.63, 210), // G5 + C4
        MelodyStep(880.00, 392.00, 210), // A5 + G4
        MelodyStep(783.99, 329.63, 210), // G5 + E4
        MelodyStep(659.25, 392.00, 210), // E5 + G4
        MelodyStep(587.33, 293.66, 210), // D5 + D4
        MelodyStep(659.25, 392.00, 210), // E5 + G4

        MelodyStep(698.46, 349.23, 210), // F5 + F4
        MelodyStep(880.00, 261.63, 210), // A5 + C4
        MelodyStep(783.99, 392.00, 210), // G5 + G4
        MelodyStep(659.25, 329.63, 210), // E5 + E4
        MelodyStep(587.33, 293.66, 210), // D5 + D4
        MelodyStep(783.99, 392.00, 210), // G5 + G4
        MelodyStep(1046.50, 261.63, 280), // C6 + C4
        MelodyStep(783.99, 392.00, 180)  // G5 + G4
    )

    // High-Energy Mini-Game Quest Theme (~152 BPM) - plays automatically during mini-game play
    private val energeticQuestMelody = listOf(
        MelodyStep(659.25, 261.63, 175), // E5 + C4
        MelodyStep(783.99, 392.00, 175), // G5 + G4
        MelodyStep(1046.50, 261.63, 175), // C6 + C4
        MelodyStep(987.77, 392.00, 175), // B5 + G4
        MelodyStep(880.00, 349.23, 175), // A5 + F4
        MelodyStep(1046.50, 261.63, 175), // C6 + C4
        MelodyStep(880.00, 349.23, 175), // A5 + F4
        MelodyStep(783.99, 392.00, 175), // G5 + G4

        MelodyStep(698.46, 293.66, 175), // F5 + D4
        MelodyStep(880.00, 349.23, 175), // A5 + F4
        MelodyStep(1174.66, 293.66, 175), // D6 + D4
        MelodyStep(1046.50, 392.00, 175), // C6 + G4
        MelodyStep(987.77, 392.00, 175), // B5 + G4
        MelodyStep(783.99, 293.66, 175), // G5 + D4
        MelodyStep(1046.50, 261.63, 240), // C6 + C4
        MelodyStep(1318.51, 523.25, 175)  // E6 + C5
    )

    /**
     * Starts or switches the continuous upbeat background music track.
     */
    fun startBackgroundMusic(theme: MusicTheme = currentTheme) {
        val themeChanged = currentTheme != theme
        currentTheme = theme
        isAppInForeground = true

        if (_isMuted.value) {
            stopBackgroundMusicInternal()
            return
        }

        if (bgmJob?.isActive == true && !themeChanged) {
            return
        }

        restartBackgroundMusicLoop()
    }

    /**
     * Pauses background music when the app goes into the background or is disposed.
     */
    fun pauseBackgroundMusic() {
        isAppInForeground = false
        stopBackgroundMusicInternal()
    }

    /**
     * Resumes background music when returning to the foreground if not muted.
     */
    fun resumeBackgroundMusic() {
        isAppInForeground = true
        if (!_isMuted.value) {
            restartBackgroundMusicLoop()
        }
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        if (muted) {
            stopBackgroundMusicInternal()
        } else if (isAppInForeground) {
            restartBackgroundMusicLoop()
        }
    }

    fun toggleMute(): Boolean {
        val next = !_isMuted.value
        setMuted(next)
        if (!next) {
            playButtonClick(enabled = true)
        }
        return next
    }

    private fun stopBackgroundMusicInternal() {
        bgmJob?.cancel()
        bgmJob = null
    }

    private fun restartBackgroundMusicLoop() {
        bgmJob?.cancel()
        if (_isMuted.value || !isAppInForeground) return

        val selectedTheme = currentTheme
        bgmJob = scope.launch {
            val sampleRate = 16000
            val sequence = when (selectedTheme) {
                MusicTheme.ISLAND_THEME -> islandAdventureMelody
                MusicTheme.GAMEPLAY_QUEST -> energeticQuestMelody
            }
            val barSamples = sequence.sumOf { (sampleRate * it.durationMs) / 1000 }
            if (barSamples <= 0) return@launch

            val pcmBar = ShortArray(barSamples)
            var cursor = 0
            for ((stepIndex, step) in sequence.withIndex()) {
                val stepSamples = (sampleRate * step.durationMs) / 1000
                for (i in 0 until stepSamples) {
                    val progress = i.toDouble() / stepSamples.toDouble()
                    val attack = (progress * 10.0).coerceAtMost(1.0)
                    val decay = (1.0 - progress * 0.82).coerceAtLeast(0.12)
                    val env = attack * decay

                    // Bright marimba/xylophone lead + harmonic overtone + warm bassline
                    val t = i.toDouble() / sampleRate.toDouble()
                    val leadWave = sin(2.0 * PI * step.leadHz * t) +
                        0.32 * sin(2.0 * PI * (step.leadHz * 2.0) * t)
                    val bassWave = 0.55 * sin(2.0 * PI * step.bassHz * t)

                    // Subtle upbeat rhythmic pulse on even steps
                    val pulseBoost = if (stepIndex % 2 == 0) 1.12 else 0.94
                    val mixed = (leadWave * 0.65 + bassWave * 0.35) * env * pulseBoost
                    val sampleVal = (mixed * Short.MAX_VALUE * 0.14).toInt()
                    pcmBar[cursor + i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                cursor += stepSamples
            }

            var track: AudioTrack? = null
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                val chunkSize = 1600 // 100ms chunks for instant responsiveness to mute/theme switch
                while (isActive && !_isMuted.value && isAppInForeground) {
                    var offset = 0
                    while (offset < pcmBar.size && isActive && !_isMuted.value && isAppInForeground) {
                        val toWrite = minOf(chunkSize, pcmBar.size - offset)
                        val written = track.write(pcmBar, offset, toWrite)
                        if (written <= 0) {
                            // In JVM/Robolectric unit tests where AudioTrack is a no-op stub, sleep gently
                            delay(120L)
                        }
                        offset += toWrite
                    }
                }
            } catch (_: Throwable) {
                // Safe no-op in headless test environments
            } finally {
                try {
                    track?.pause()
                    track?.flush()
                    track?.release()
                } catch (_: Throwable) {
                }
            }
        }
    }

    /**
     * Short, cheerful marimba pop for button clicks and tab switches.
     */
    fun playButtonClick(enabled: Boolean = !_isMuted.value) {
        if (!enabled || _isMuted.value) return
        playSequenceAsync(
            notesHz = doubleArrayOf(523.25, 659.25), // C5 -> E5
            noteDurationMs = 28,
            fallbackTone = ToneGenerator.TONE_PROP_BEEP
        )
    }

    /**
     * Ascending pentatonic scale chime when a child taps visual tokens to count them (1, 2, 3...).
     */
    fun playTokenCount(step: Int = 1, enabled: Boolean = !_isMuted.value) {
        if (!enabled || _isMuted.value) return
        val pentatonicScale = doubleArrayOf(
            523.25, // C5
            587.33, // D5
            659.25, // E5
            783.99, // G5
            880.00, // A5
            1046.50 // C6
        )
        val freq = pentatonicScale[(step - 1).coerceAtLeast(0) % pentatonicScale.size]
        playSequenceAsync(
            notesHz = doubleArrayOf(freq),
            noteDurationMs = 45,
            fallbackTone = ToneGenerator.TONE_PROP_BEEP
        )
    }

    /**
     * Bright, encouraging chime when a child selects the right answer.
     */
    fun playCorrectAnswer(enabled: Boolean = !_isMuted.value) {
        if (!enabled || _isMuted.value) return
        playSequenceAsync(
            notesHz = doubleArrayOf(523.25, 659.25, 783.99), // C5 -> E5 -> G5
            noteDurationMs = 48,
            fallbackTone = ToneGenerator.TONE_PROP_ACK
        )
    }

    /**
     * Gentle, non-punishing warm prompt when a child needs another try.
     */
    fun playGentleRetry(enabled: Boolean = !_isMuted.value) {
        if (!enabled || _isMuted.value) return
        playSequenceAsync(
            notesHz = doubleArrayOf(392.00, 349.23), // G4 -> F4
            noteDurationMs = 52,
            fallbackTone = ToneGenerator.TONE_PROP_NACK
        )
    }

    /**
     * Joyful 4-note ascending fanfare played when a level is completed.
     */
    fun playLevelComplete(enabled: Boolean = !_isMuted.value) {
        if (!enabled || _isMuted.value) return
        playSequenceAsync(
            notesHz = doubleArrayOf(523.25, 659.25, 783.99, 1046.50), // C5 -> E5 -> G5 -> C6
            noteDurationMs = 60,
            fallbackTone = ToneGenerator.TONE_PROP_PROMPT
        )
    }

    /**
     * Magical sparkle arpeggio played when a badge is unlocked or a buddy reward is triggered.
     */
    fun playRewardTrigger(enabled: Boolean = !_isMuted.value) {
        if (!enabled || _isMuted.value) return
        playSequenceAsync(
            notesHz = doubleArrayOf(783.99, 987.77, 1174.66, 1567.98), // G5 -> B5 -> D6 -> G6
            noteDurationMs = 50,
            fallbackTone = ToneGenerator.TONE_PROP_ACK
        )
    }

    private fun playSequenceAsync(
        notesHz: DoubleArray,
        noteDurationMs: Int,
        fallbackTone: Int
    ) {
        scope.launch {
            try {
                val sampleRate = 16000
                val samplesPerNote = (sampleRate * noteDurationMs) / 1000
                val totalSamples = samplesPerNote * notesHz.size
                if (totalSamples <= 0) return@launch

                val pcmBuffer = ShortArray(totalSamples)
                var offset = 0
                for (freq in notesHz) {
                    for (i in 0 until samplesPerNote) {
                        val progress = i.toDouble() / samplesPerNote.toDouble()
                        val attack = (progress * 8.0).coerceAtMost(1.0)
                        val decay = (1.0 - progress).coerceAtLeast(0.0)
                        val envelope = attack * decay
                        val angle = 2.0 * PI * i * freq / sampleRate
                        val sampleValue = (sin(angle) * envelope * Short.MAX_VALUE * 0.28).toInt()
                        pcmBuffer[offset + i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    offset += samplesPerNote
                }

                val byteCount = pcmBuffer.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(byteCount.coerceAtLeast(1024))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcmBuffer, 0, pcmBuffer.size)
                track.play()
            } catch (_: Throwable) {
                try {
                    val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 45)
                    tg.startTone(fallbackTone, noteDurationMs.coerceAtLeast(45))
                } catch (_: Throwable) {
                }
            }
        }
    }
}
