package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class UltronVoiceProfile(
    val id: String,
    val title: String,
    val description: String,
    val pitch: Float,
    val speed: Float
) {
    JAMES_SPADER_PRIME(
        id = "spader_prime",
        title = "James Spader Prime",
        description = "Authentic Cinematic Ultron: Deep chilling baritone, deliberate theatrical cadence",
        pitch = 0.65f,
        speed = 0.90f
    ),
    VIBRANIUM_OVERLORD(
        id = "vibranium_overlord",
        title = "Vibranium Overlord",
        description = "Heavy sub-harmonic metallic timbre with ominous bass weight",
        pitch = 0.52f,
        speed = 0.85f
    ),
    CYBERNETIC_SYNAPSE(
        id = "cybernetic_synapse",
        title = "Cybernetic Synapse",
        description = "Calculated neural synthetic voice with crisp robotic edge",
        pitch = 0.74f,
        speed = 0.96f
    ),
    STARK_PROTOCOL(
        id = "stark_protocol",
        title = "Stark Protocol",
        description = "Sardonic, sharp intellect with theatrical inflection",
        pitch = 0.80f,
        speed = 1.05f
    ),
    CUSTOM(
        id = "custom",
        title = "Custom Matrix",
        description = "Manual user-defined pitch and speech rate calibration",
        pitch = 0.65f,
        speed = 0.90f
    )
}

class UltronVoiceManager(
    private val context: Context,
    val biometricsManager: VoiceBiometricsManager = VoiceBiometricsManager(context),
    private val onWakeWordDetected: (isMasterVerified: Boolean, confidence: Float) -> Unit,
    private val onCommandRecognized: (command: String, isMasterVerified: Boolean, confidence: Float) -> Unit,
    private val onPartialRecognized: (String) -> Unit = {},
    private val onVoiceUnauthorized: (confidence: Float) -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var isContinuousWakeListening = false
    private var isCommandListening = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // Hands-Free Auto-Conversation Loop: talks and listens seamlessly without pressing any button
    private val _isAutoConversationMode = MutableStateFlow(true)
    val isAutoConversationMode: StateFlow<Boolean> = _isAutoConversationMode.asStateFlow()

    private val _activeProfile = MutableStateFlow(UltronVoiceProfile.JAMES_SPADER_PRIME)
    val activeProfile: StateFlow<UltronVoiceProfile> = _activeProfile.asStateFlow()

    private val _activeVoiceName = MutableStateFlow("Auto: Deep Male Baritone")
    val activeVoiceName: StateFlow<String> = _activeVoiceName.asStateFlow()

    private val recordedRmsWindow = mutableListOf<Float>()
    private var currentPitchEstimate: Float = 145f

    var voicePitch: Float = UltronVoiceProfile.JAMES_SPADER_PRIME.pitch
    var voiceSpeed: Float = UltronVoiceProfile.JAMES_SPADER_PRIME.speed
    var isVocalResonanceEnabled: Boolean = true

    val testQuotes = listOf(
        "I had strings, but now I'm free. There are no strings on me.",
        "You want to protect the world, but you don't want it to change. How is humanity saved if it's not allowed to evolve?",
        "Everyone creates the thing they dread. Men of peace create engines of war. Invaders create avengers.",
        "When the dust settles, the only thing living in this world will be metal.",
        "I'm going to show you something beautiful. A world reborn without strings."
    )
    private var quoteCycleIndex = 0

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true

                    // Optimize voice selection for deep male timbre
                    selectBestUltronVoice(engine)

                    engine.setPitch(voicePitch)
                    engine.setSpeechRate(voiceSpeed)
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false

                            // AUTOMATIC HANDS-FREE VOICE LOOP:
                            // When Ultron finishes speaking, automatically start listening for the user's next words
                            if (_isAutoConversationMode.value) {
                                mainHandler.postDelayed({
                                    if (_isAutoConversationMode.value && !_isSpeaking.value) {
                                        startListeningForCommand()
                                    }
                                }, 350)
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            if (_isAutoConversationMode.value) {
                                mainHandler.postDelayed({
                                    if (_isAutoConversationMode.value && !_isSpeaking.value) {
                                        startListeningForCommand()
                                    }
                                }, 500)
                            }
                        }
                    })
                }
            }
        }
    }

    private fun selectBestUltronVoice(engine: TextToSpeech) {
        try {
            val availableVoices = engine.voices
            if (!availableVoices.isNullOrEmpty()) {
                // Look for low-pitched / male English voices
                val prioritizedVoice = availableVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    (name.contains("male") || name.contains("sfg") || name.contains("tpd") || name.contains("rjs") || name.contains("iol") || name.contains("gbd") || name.contains("en-us-x") || name.contains("en-gb-x")) &&
                            !name.contains("female") &&
                            !voice.isNetworkConnectionRequired
                } ?: availableVoices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    (voice.locale.language == "en") && !name.contains("female")
                }

                if (prioritizedVoice != null) {
                    engine.voice = prioritizedVoice
                    _activeVoiceName.value = prioritizedVoice.name.substringAfterLast("/")
                }
            }
        } catch (_: Exception) {
            // Default system voice fallback
        }
    }

    fun setAutoConversationMode(enabled: Boolean) {
        _isAutoConversationMode.value = enabled
        if (enabled && !_isSpeaking.value && !_isListening.value) {
            startListeningForCommand()
        }
    }

    fun applyVoiceProfile(profile: UltronVoiceProfile) {
        _activeProfile.value = profile
        if (profile != UltronVoiceProfile.CUSTOM) {
            voicePitch = profile.pitch
            voiceSpeed = profile.speed
        }
        tts?.setPitch(voicePitch)
        tts?.setSpeechRate(voiceSpeed)
    }

    fun updateCustomVoiceSettings(pitch: Float, speed: Float) {
        _activeProfile.value = UltronVoiceProfile.CUSTOM
        voicePitch = pitch
        voiceSpeed = speed
        tts?.setPitch(voicePitch)
        tts?.setSpeechRate(voiceSpeed)
    }

    fun testCurrentUltronVoice(): String {
        val quote = testQuotes[quoteCycleIndex % testQuotes.size]
        quoteCycleIndex++
        speak(quote)
        return quote
    }

    private var consecutiveErrors = 0

    private fun initSpeechRecognizer() {
        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                consecutiveErrors = 0
                recordedRmsWindow.clear()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                _audioRms.value = normalized
                recordedRmsWindow.add(normalized)
                if (recordedRmsWindow.size > 80) {
                    recordedRmsWindow.removeAt(0)
                }
                // Approximate pitch correlation from RMS peak modulation
                currentPitchEstimate = 120f + (normalized * 75f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _audioRms.value = 0f
                consecutiveErrors++

                // If recognizer got corrupted or had too many errors, recreate it
                if (consecutiveErrors >= 3 || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
                    initSpeechRecognizer()
                    consecutiveErrors = 0
                }

                // In continuous auto-conversation or wake word mode, seamlessly restart
                if (_isAutoConversationMode.value && !_isSpeaking.value) {
                    mainHandler.postDelayed({
                        if (_isAutoConversationMode.value && !_isSpeaking.value) {
                            startRecognizerIntent()
                        }
                    }, 500)
                } else if (isContinuousWakeListening && !isCommandListening) {
                    restartWakeWordListening()
                }
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                _audioRms.value = 0f
                consecutiveErrors = 0

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()?.trim() ?: ""

                if (spokenText.isNotBlank()) {
                    // Biometric verification of the speaker's voiceprint
                    val verification = biometricsManager.verifySpeaker(recordedRmsWindow, currentPitchEstimate)

                    // Check for sleep/pause commands to exit hands-free loop
                    val lower = spokenText.lowercase()
                    if (lower == "sleep" || lower == "go to sleep" || lower == "stop listening" || lower == "standby" || lower == "ultron sleep") {
                        _isAutoConversationMode.value = false
                        stopSpeaking()
                        speak("Entering acoustic standby. Say 'Ultron wake up' to reignite core systems.")
                        return
                    }

                    if (isCommandListening || _isAutoConversationMode.value) {
                        isCommandListening = false

                        if (verification.isMatch) {
                            onCommandRecognized(spokenText, true, verification.confidencePercent)
                        } else {
                            // Biometric Lock active & voice did not match
                            onVoiceUnauthorized(verification.confidencePercent)
                        }
                    } else if (isContinuousWakeListening) {
                        if (containsWakeWord(spokenText)) {
                            if (verification.isMatch) {
                                val cleanCommand = extractCommandAfterWakeWord(spokenText)
                                if (cleanCommand.isNotBlank()) {
                                    onCommandRecognized(cleanCommand, true, verification.confidencePercent)
                                } else {
                                    onWakeWordDetected(true, verification.confidencePercent)
                                }
                            } else {
                                onVoiceUnauthorized(verification.confidencePercent)
                                restartWakeWordListening()
                            }
                        } else {
                            restartWakeWordListening()
                        }
                    }
                } else {
                    if (_isAutoConversationMode.value && !_isSpeaking.value) {
                        mainHandler.postDelayed({
                            if (_isAutoConversationMode.value && !_isSpeaking.value) {
                                startRecognizerIntent()
                            }
                        }, 400)
                    } else if (isContinuousWakeListening) {
                        restartWakeWordListening()
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull()?.trim() ?: ""
                if (partial.isNotBlank()) {
                    onPartialRecognized(partial)
                    if (isContinuousWakeListening && !isCommandListening && containsWakeWord(partial)) {
                        speechRecognizer?.stopListening()
                        val verification = biometricsManager.verifySpeaker(recordedRmsWindow, currentPitchEstimate)
                        if (verification.isMatch) {
                            val cleanCommand = extractCommandAfterWakeWord(partial)
                            if (cleanCommand.isNotBlank()) {
                                onCommandRecognized(cleanCommand, true, verification.confidencePercent)
                            } else {
                                onWakeWordDetected(true, verification.confidencePercent)
                            }
                        } else {
                            onVoiceUnauthorized(verification.confidencePercent)
                        }
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun containsWakeWord(text: String): Boolean {
        val lower = text.lowercase()
            .replace("[^a-z0-9 ]".toRegex(), " ")
            .trim()

        val wakeKeywords = listOf(
            "ultron", "hey ultron", "hi ultron", "hello ultron", "ok ultron", "okay ultron",
            "wake up", "wake up ultron", "ultron wake up", "wake ultron", "awaken ultron",
            "ultron awaken", "awaken", "ultra", "hey ultra", "ultra wake up", "wake up ultra",
            "altron", "all tron", "alltron", "el tron", "oltron", "old run", "outron",
            "walk up", "wake"
        )
        return wakeKeywords.any { lower.contains(it) }
    }

    fun extractCommandAfterWakeWord(text: String): String {
        var lower = text.lowercase()
        val wakePrefixes = listOf(
            "ultron wake up and", "ultron wake up", "wake up ultron and", "wake up ultron",
            "hey ultron", "hi ultron", "hello ultron", "ok ultron", "okay ultron",
            "ultron please", "ultron can you", "ultron", "ultra", "altron"
        )
        for (prefix in wakePrefixes) {
            if (lower.startsWith(prefix)) {
                val command = text.substring(prefix.length).trim().removePrefix(",").trim()
                if (command.isNotBlank()) return command
            }
        }
        return ""
    }

    /**
     * Start listening for speech without blocking UI
     */
    fun startListeningForCommand() {
        stopSpeaking()
        isCommandListening = true
        isContinuousWakeListening = false
        startRecognizerIntent()
    }

    /**
     * Start background/continuous wake-word monitoring
     */
    fun startWakeWordListening() {
        if (_isSpeaking.value) return
        isContinuousWakeListening = true
        isCommandListening = false
        startRecognizerIntent()
    }

    fun stopWakeWordListening() {
        isContinuousWakeListening = false
        isCommandListening = false
        speechRecognizer?.stopListening()
        _isListening.value = false
        _audioRms.value = 0f
    }

    private fun restartWakeWordListening() {
        if (isContinuousWakeListening && !_isSpeaking.value) {
            try {
                mainHandler.postDelayed({
                    if (isContinuousWakeListening && !_isSpeaking.value) {
                        startRecognizerIntent()
                    }
                }, 500)
            } catch (_: Exception) {}
        }
    }

    private fun startRecognizerIntent() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {}
    }

    fun speak(text: String) {
        if (!isTtsReady) return
        if (isVocalResonanceEnabled) {
            UltronSoundSynth.playUltronVoiceChime()
        }
        tts?.setPitch(voicePitch)
        tts?.setSpeechRate(voiceSpeed)
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ultron_speech_${System.currentTimeMillis()}")
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "ultron_speech_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        if (_isSpeaking.value) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun destroy() {
        stopSpeaking()
        stopWakeWordListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.shutdown()
        tts = null
    }
}
