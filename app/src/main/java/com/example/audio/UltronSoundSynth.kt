package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object UltronSoundSynth {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Play Ultron Vocal Resonator Chime (Sub-bass harmonic metallic pulse before speech)
     */
    fun playUltronVoiceChime() {
        scope.launch {
            playTones(
                listOf(
                    Tone(110.0, 45),
                    Tone(220.0, 55),
                    Tone(165.0, 75)
                )
            )
        }
    }

    /**
     * Play futuristic Awakening Chime (Arc Reactor ignition sequence)
     */
    fun playWakeUpSound() {
        scope.launch {
            playTones(
                listOf(
                    Tone(300.0, 60),
                    Tone(450.0, 70),
                    Tone(650.0, 90),
                    Tone(900.0, 160)
                )
            )
        }
    }

    /**
     * Play Action Executed Chime (Laser lock / command confirmation)
     */
    fun playActionSound() {
        scope.launch {
            playTones(
                listOf(
                    Tone(700.0, 40),
                    Tone(1100.0, 80)
                )
            )
        }
    }

    /**
     * Play Computation / Telemetry Scan beep
     */
    fun playScanSound() {
        scope.launch {
            playTones(
                listOf(
                    Tone(850.0, 35),
                    Tone(1050.0, 45)
                )
            )
        }
    }

    /**
     * Play Warning / Offline alert beep
     */
    fun playAlertSound() {
        scope.launch {
            playTones(
                listOf(
                    Tone(400.0, 80),
                    Tone(320.0, 120)
                )
            )
        }
    }

    private data class Tone(val freqHz: Double, val durationMs: Int)

    private fun playTones(tones: List<Tone>) {
        try {
            val sampleRate = 44100
            val totalDurationMs = tones.sumOf { it.durationMs }
            val totalSamples = (sampleRate * totalDurationMs / 1000)
            val buffer = ShortArray(totalSamples)

            var sampleOffset = 0
            for (tone in tones) {
                val toneSamples = (sampleRate * tone.durationMs / 1000)
                for (i in 0 until toneSamples) {
                    val time = i.toDouble() / sampleRate
                    // Sine wave with soft attack and decay
                    val envelope = when {
                        i < toneSamples * 0.1 -> i / (toneSamples * 0.1)
                        i > toneSamples * 0.8 -> (toneSamples - i) / (toneSamples * 0.2)
                        else -> 1.0
                    }
                    val sampleValue = (sin(2.0 * Math.PI * tone.freqHz * time) * 16000 * envelope).toInt()
                    if (sampleOffset + i < buffer.size) {
                        buffer[sampleOffset + i] = sampleValue.toShort()
                    }
                }
                sampleOffset += toneSamples
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(totalDurationMs.toLong() + 50)
            audioTrack.release()
        } catch (_: Exception) {
            // Audio generation fallback silent
        }
    }
}
