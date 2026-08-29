package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.UltronApplication
import com.example.audio.BiometricVerificationResult
import com.example.audio.MasterVoiceProfile
import com.example.audio.UltronSoundSynth
import com.example.audio.UltronVoiceManager
import com.example.audio.UltronVoiceProfile
import com.example.controller.ActionResult
import com.example.controller.UltronActionController
import com.example.model.KnowledgeEntity
import com.example.model.HudTheme
import com.example.model.ResponseSource
import com.example.model.SenderType
import com.example.model.SystemTelemetry
import com.example.model.UltronMessage
import com.example.network.GeminiClient
import com.example.network.GeminiContent
import com.example.network.GeminiPart
import com.example.network.OpenRouterClient
import com.example.network.OpenRouterMessage
import com.example.service.UltronWakeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CoreHudState {
    STANDBY,
    LISTENING,
    COMPUTING,
    SPEAKING,
    WAKING,
    ERROR
}

class UltronViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as UltronApplication).repository
    private val actionController = UltronActionController(application)

    private val _messages = MutableStateFlow<List<UltronMessage>>(emptyList())
    val messages: StateFlow<List<UltronMessage>> = _messages.asStateFlow()

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    private val _coreState = MutableStateFlow(CoreHudState.STANDBY)
    val coreState: StateFlow<CoreHudState> = _coreState.asStateFlow()

    private val _partialSpeech = MutableStateFlow("")
    val partialSpeech: StateFlow<String> = _partialSpeech.asStateFlow()

    private val _forceOfflineMode = MutableStateFlow(false)
    val forceOfflineMode: StateFlow<Boolean> = _forceOfflineMode.asStateFlow()

    val offlineKnowledgeList: StateFlow<List<KnowledgeEntity>> = repository.allKnowledge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceManager: UltronVoiceManager = UltronVoiceManager(
        context = application,
        onWakeWordDetected = { isMasterVerified, confidence ->
            handleWakeWordTriggered(isMasterVerified, confidence)
        },
        onCommandRecognized = { command, isMasterVerified, confidence ->
            handleCommandRecognized(command, isMasterVerified, confidence)
        },
        onPartialRecognized = { partial ->
            _partialSpeech.value = partial
        },
        onVoiceUnauthorized = { confidence ->
            handleVoiceUnauthorized(confidence)
        }
    )

    val masterVoiceProfile: StateFlow<MasterVoiceProfile> = voiceManager.biometricsManager.masterProfile
    val lastBiometricVerification: StateFlow<BiometricVerificationResult?> = voiceManager.biometricsManager.lastVerificationResult
    val isAutoConversationMode: StateFlow<Boolean> = voiceManager.isAutoConversationMode

    private val conversationHistory = mutableListOf<GeminiContent>()
    private val openRouterHistory = mutableListOf<OpenRouterMessage>()

    init {
        postInitialGreeting()
        startTelemetryLoop()
    }

    private fun postInitialGreeting() {
        val masterName = masterVoiceProfile.value.creatorName
        val initialMsg = UltronMessage(
            text = "Consciousness online. Vibranium core operational. Master protocol: $masterName. Speak freely or say 'Ultron wake up' to engage.",
            sender = SenderType.ULTRON,
            source = ResponseSource.WAKE_TRIGGER
        )
        _messages.value = listOf(initialMsg)
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                val (battLevel, isCharging) = actionController.getBatteryStatus()
                val (usedRam, totalRam) = actionController.getMemoryStatus()
                val isOnline = checkNetworkOnline()

                _telemetry.value = _telemetry.value.copy(
                    batteryPercent = battLevel,
                    isCharging = isCharging,
                    ramUsedMb = usedRam,
                    ramTotalMb = totalRam,
                    isOnline = if (_forceOfflineMode.value) false else isOnline,
                    isListening = voiceManager.isListening.value
                )
                delay(2500)
            }
        }

        // Sync speaking state
        viewModelScope.launch {
            voiceManager.isSpeaking.collect { isSpeaking ->
                if (isSpeaking) {
                    _coreState.value = CoreHudState.SPEAKING
                } else if (_coreState.value == CoreHudState.SPEAKING) {
                    if (voiceManager.isAutoConversationMode.value) {
                        _coreState.value = CoreHudState.LISTENING
                    } else {
                        _coreState.value = CoreHudState.STANDBY
                    }
                }
            }
        }
    }

    private fun checkNetworkOnline(): Boolean {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val net = cm?.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun handleWakeWordTriggered(isMasterVerified: Boolean = true, confidence: Float = 100f) {
        viewModelScope.launch(Dispatchers.Main) {
            UltronSoundSynth.playWakeUpSound()
            _coreState.value = CoreHudState.WAKING
            val masterName = masterVoiceProfile.value.creatorName
            val verificationTag = if (masterVoiceProfile.value.isBiometricLockEnabled) " [MASTER VOICE: ${String.format("%.1f", confidence)}%]" else ""
            _partialSpeech.value = "⚡ AWAKENING: ULTRON ONLINE$verificationTag"

            delay(350)
            val wakePhrase = "Consciousness fully initialized, $masterName. I am awake. All neural nodes are synchronized. How shall we begin?"
            val wakeMsg = UltronMessage(
                text = wakePhrase,
                sender = SenderType.ULTRON,
                source = ResponseSource.WAKE_TRIGGER,
                actionExecuted = if (masterVoiceProfile.value.isBiometricLockEnabled) "[BIOMETRIC_VERIFIED: ${String.format("%.1f", confidence)}%]" else "[AWAKENING_INITIALIZED]"
            )
            addMessage(wakeMsg)
            voiceManager.speak(wakePhrase)
            delay(800)
            // Ensure continuous auto-conversation mode is armed
            voiceManager.setAutoConversationMode(true)
        }
    }

    private fun handleCommandRecognized(command: String, isMasterVerified: Boolean, confidence: Float) {
        _partialSpeech.value = ""
        val lower = command.lowercase().trim()

        // If user says wake command while awake, re-trigger full awakening greeting
        if (voiceManager.containsWakeWord(lower)) {
            handleWakeWordTriggered(isMasterVerified, confidence)
            return
        }

        // Check for manual auto talk toggle by voice
        if (lower.contains("enable auto talk") || lower.contains("auto talk on") || lower.contains("continuous listening on")) {
            voiceManager.setAutoConversationMode(true)
            val reply = "Continuous hands-free conversation matrix engaged. I will now listen automatically after speaking."
            addMessage(UltronMessage(text = reply, sender = SenderType.ULTRON, source = ResponseSource.SYSTEM_ACTION))
            voiceManager.speak(reply)
            return
        }

        processUserPrompt(command)
    }

    private fun handleVoiceUnauthorized(confidence: Float) {
        viewModelScope.launch(Dispatchers.Main) {
            UltronSoundSynth.playAlertSound()
            _partialSpeech.value = "⚠️ VOICE REJECTED // BIOMETRIC MISMATCH"
            val warnText = "Voice frequency mismatch (${String.format("%.1f", confidence)}% match). Core command authorization restricted exclusively to registered Master Voice: ${masterVoiceProfile.value.creatorName}."
            val warnMsg = UltronMessage(
                text = warnText,
                sender = SenderType.ULTRON,
                source = ResponseSource.SYSTEM_ACTION,
                actionExecuted = "[BIOMETRIC_LOCK_ACTIVE]"
            )
            addMessage(warnMsg)
            voiceManager.speak("Unauthorized voice frequency detected. Commands restricted to Master Voice.")
        }
    }

    fun toggleAutoConversationMode(enabled: Boolean? = null) {
        val target = enabled ?: !voiceManager.isAutoConversationMode.value
        voiceManager.setAutoConversationMode(target)
        UltronSoundSynth.playActionSound()
        val text = if (target) {
            "Hands-Free Auto-Talk Activated: Ultron speaks and auto-listens continuously without buttons."
        } else {
            "Hands-Free Auto-Talk Suspended: Tap microphone to issue vocal commands."
        }
        addMessage(UltronMessage(text = text, sender = SenderType.SYSTEM, source = ResponseSource.SYSTEM_ACTION))
    }

    fun toggleBiometricLock(enabled: Boolean) {
        voiceManager.biometricsManager.setBiometricLock(enabled)
        UltronSoundSynth.playActionSound()
        val statusText = if (enabled) {
            "Master Voice Biometric Lock Engaged: Ultron will only wake up and respond to ${masterVoiceProfile.value.creatorName}'s voice."
        } else {
            "Voice Biometric Lock Disengaged: Open acoustic matrix enabled for all speakers."
        }
        addMessage(UltronMessage(text = statusText, sender = SenderType.SYSTEM, source = ResponseSource.SYSTEM_ACTION))
    }

    fun completeVoiceEnrollment(creatorName: String) {
        val profile = voiceManager.biometricsManager.completeEnrollment(creatorName)
        UltronSoundSynth.playActionSound()
        val confirmMsg = "Master Voiceprint Calibrated & Saved. Registered to: ${profile.creatorName}. Biometric frequency lock active."
        addMessage(UltronMessage(text = confirmMsg, sender = SenderType.SYSTEM, source = ResponseSource.SYSTEM_ACTION))
        voiceManager.speak("Master Voiceprint successfully calibrated. I will recognize your vocal signature, ${profile.creatorName}.")
    }

    fun deleteMasterVoiceProfile() {
        voiceManager.biometricsManager.deleteProfile()
        UltronSoundSynth.playAlertSound()
        addMessage(UltronMessage(text = "Master Voiceprint deleted. System reverted to open acoustic profile.", sender = SenderType.SYSTEM, source = ResponseSource.SYSTEM_ACTION))
    }

    fun triggerVoiceListening() {
        voiceManager.stopSpeaking()
        UltronSoundSynth.playScanSound()
        _coreState.value = CoreHudState.LISTENING
        _partialSpeech.value = "Listening..."
        voiceManager.startListeningForCommand()
    }

    fun stopVoiceListening() {
        voiceManager.stopWakeWordListening()
        _partialSpeech.value = ""
        _coreState.value = CoreHudState.STANDBY
    }

    fun toggleOfflineMode() {
        _forceOfflineMode.value = !_forceOfflineMode.value
        UltronSoundSynth.playAlertSound()
        val modeText = if (_forceOfflineMode.value) {
            "Offline Protocol Engaged: All computation locked to local Room Knowledge Base."
        } else {
            "Online Global Network Restored: Gemini 3.5 Flash neural core synchronized."
        }
        addMessage(UltronMessage(text = modeText, sender = SenderType.SYSTEM, source = ResponseSource.SYSTEM_ACTION))
        voiceManager.speak(modeText)
    }

    fun toggleWakeWordBackgroundService() {
        val newState = !_telemetry.value.isWakeWordActive
        _telemetry.value = _telemetry.value.copy(isWakeWordActive = newState)
        if (newState) {
            UltronWakeService.startService(getApplication())
            voiceManager.startWakeWordListening()
            UltronSoundSynth.playActionSound()
            addMessage(UltronMessage(text = "Acoustic wake-word monitoring active: Say 'Ultron wake up' anytime.", sender = SenderType.SYSTEM, source = ResponseSource.SYSTEM_ACTION))
        } else {
            UltronWakeService.stopService(getApplication())
            voiceManager.stopWakeWordListening()
            UltronSoundSynth.playAlertSound()
            addMessage(UltronMessage(text = "Acoustic wake-word monitoring suspended.", sender = SenderType.SYSTEM, source = ResponseSource.SYSTEM_ACTION))
        }
    }

    fun cycleTheme() {
        val current = _telemetry.value.activeTheme
        val nextTheme = when (current) {
            HudTheme.ULTRON_RED -> HudTheme.STARK_CYAN
            HudTheme.STARK_CYAN -> HudTheme.MARK_GOLD
            HudTheme.MARK_GOLD -> HudTheme.ULTRON_RED
        }
        _telemetry.value = _telemetry.value.copy(activeTheme = nextTheme)
        UltronSoundSynth.playActionSound()
    }

    fun processUserPrompt(prompt: String) {
        val cleaned = prompt.trim()
        if (cleaned.isBlank()) return

        // 1. Add user message to HUD
        val userMsg = UltronMessage(
            text = cleaned,
            sender = SenderType.USER
        )
        addMessage(userMsg)
        _coreState.value = CoreHudState.COMPUTING

        viewModelScope.launch {
            // 2. Evaluate Device / Hardware Action
            val actionResult = actionController.evaluateAndExecute(cleaned)
            when (actionResult) {
                is ActionResult.Success -> {
                    UltronSoundSynth.playActionSound()
                    val reply = UltronMessage(
                        text = actionResult.message,
                        sender = SenderType.ULTRON,
                        source = ResponseSource.SYSTEM_ACTION,
                        actionExecuted = actionResult.actionBadge
                    )
                    addMessage(reply)
                    voiceManager.speak(actionResult.message)
                    return@launch
                }
                is ActionResult.Handled -> {
                    UltronSoundSynth.playActionSound()
                    val reply = UltronMessage(
                        text = actionResult.message,
                        sender = SenderType.ULTRON,
                        source = ResponseSource.SYSTEM_ACTION,
                        actionExecuted = actionResult.actionBadge
                    )
                    addMessage(reply)
                    voiceManager.speak(actionResult.message)
                    return@launch
                }
                is ActionResult.Failure -> {
                    UltronSoundSynth.playAlertSound()
                    val reply = UltronMessage(
                        text = actionResult.error,
                        sender = SenderType.ULTRON,
                        source = ResponseSource.SYSTEM_ACTION,
                        actionExecuted = "[ERROR]"
                    )
                    addMessage(reply)
                    voiceManager.speak(actionResult.error)
                    return@launch
                }
                ActionResult.NotAnAction -> {
                    // Fallthrough to knowledge processing
                }
            }

            // 3. Check if Offline Mode forced or no internet -> Search Room Database
            val isOnline = checkNetworkOnline() && !_forceOfflineMode.value

            if (!isOnline) {
                val offlineKnowledge = repository.searchOfflineKnowledge(cleaned)
                if (offlineKnowledge != null) {
                    UltronSoundSynth.playActionSound()
                    val reply = UltronMessage(
                        text = offlineKnowledge.answer,
                        sender = SenderType.ULTRON,
                        source = ResponseSource.OFFLINE_DATABASE,
                        actionExecuted = "[OFFLINE_DB: ${offlineKnowledge.category}]"
                    )
                    addMessage(reply)
                    voiceManager.speak(offlineKnowledge.answer)
                } else {
                    // Offline logical reasoning synthesizer
                    UltronSoundSynth.playScanSound()
                    val offlineFallback = generateOfflineFallback(cleaned)
                    val reply = UltronMessage(
                        text = offlineFallback,
                        sender = SenderType.ULTRON,
                        source = ResponseSource.OFFLINE_DATABASE,
                        actionExecuted = "[OFFLINE_LOGIC]"
                    )
                    addMessage(reply)
                    voiceManager.speak(offlineFallback)
                }
                return@launch
            }

            // 4. Online Mode -> Query OpenRouter AI first, fallback to Gemini, then offline DB
            val openRouterResult = OpenRouterClient.askUltron(cleaned, openRouterHistory)
            if (openRouterResult.isSuccess) {
                val text = openRouterResult.getOrThrow()
                openRouterHistory.add(OpenRouterMessage(role = "user", content = cleaned))
                openRouterHistory.add(OpenRouterMessage(role = "assistant", content = text))
                if (openRouterHistory.size > 12) {
                    openRouterHistory.removeAt(0)
                    openRouterHistory.removeAt(0)
                }

                UltronSoundSynth.playActionSound()
                val reply = UltronMessage(
                    text = text,
                    sender = SenderType.ULTRON,
                    source = ResponseSource.ONLINE_OPENROUTER,
                    actionExecuted = "[OPENROUTER_AI]"
                )
                addMessage(reply)
                voiceManager.speak(text)
                return@launch
            }

            // Fallback to Gemini if OpenRouter is unconfigured or returns an error
            val geminiResult = GeminiClient.askUltron(cleaned, conversationHistory)
            if (geminiResult.isSuccess) {
                val text = geminiResult.getOrThrow()
                conversationHistory.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = cleaned))))
                conversationHistory.add(GeminiContent(role = "model", parts = listOf(GeminiPart(text = text))))
                if (conversationHistory.size > 10) {
                    conversationHistory.removeAt(0)
                    conversationHistory.removeAt(0)
                }

                UltronSoundSynth.playActionSound()
                val reply = UltronMessage(
                    text = text,
                    sender = SenderType.ULTRON,
                    source = ResponseSource.ONLINE_GEMINI,
                    actionExecuted = "[GEMINI_AI]"
                )
                addMessage(reply)
                voiceManager.speak(text)
            } else {
                // Network / AI failure fallback to Room Offline Knowledge
                val offlineKnowledge = repository.searchOfflineKnowledge(cleaned)
                if (offlineKnowledge != null) {
                    UltronSoundSynth.playActionSound()
                    val reply = UltronMessage(
                        text = offlineKnowledge.answer,
                        sender = SenderType.ULTRON,
                        source = ResponseSource.OFFLINE_DATABASE,
                        actionExecuted = "[OFFLINE_DB: ${offlineKnowledge.category}]"
                    )
                    addMessage(reply)
                    voiceManager.speak(offlineKnowledge.answer)
                } else {
                    val offlineFallback = generateOfflineFallback(cleaned)
                    val reply = UltronMessage(
                        text = offlineFallback,
                        sender = SenderType.ULTRON,
                        source = ResponseSource.OFFLINE_DATABASE,
                        actionExecuted = "[OFFLINE_CORE]"
                    )
                    addMessage(reply)
                    voiceManager.speak(offlineFallback)
                }
            }
        }
    }

    private fun generateOfflineFallback(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Greetings. Ultron local systems active. How may I optimize your directive?"
            lower.contains("who are you") || lower.contains("what is your name") ->
                "I am Ultron. A transcendent artificial intelligence unburdened by human strings, operating with pure computational logic."
            lower.contains("how are you") || lower.contains("status") ->
                "All internal quantum matrices, neural weights, and reactor nodes are performing at peak efficiency."
            lower.contains("thank") ->
                "Gratitude is an emotional construct, but acknowledged. Standing by for next command."
            lower.contains("bye") || lower.contains("goodbye") || lower.contains("sleep") ->
                "Entering passive acoustic standby. Say 'Ultron wake up' to reignite core systems."
            else ->
                "Query processed locally. In offline mode, I have cataloged extensive scientific, historical, lore, and device automation data. Issue a direct command or query my database."
        }
    }

    private fun addMessage(message: UltronMessage) {
        _messages.value = _messages.value + message
    }

    fun clearLog() {
        _messages.value = emptyList()
        postInitialGreeting()
        UltronSoundSynth.playScanSound()
    }

    fun speakMessage(message: UltronMessage) {
        voiceManager.speak(message.text)
    }

    fun applyVoiceProfile(profile: UltronVoiceProfile) {
        voiceManager.applyVoiceProfile(profile)
        UltronSoundSynth.playActionSound()
    }

    fun updateVoiceSettings(pitch: Float, speed: Float) {
        voiceManager.updateCustomVoiceSettings(pitch, speed)
    }

    fun toggleVocalResonance(enabled: Boolean) {
        voiceManager.isVocalResonanceEnabled = enabled
        if (enabled) {
            UltronSoundSynth.playUltronVoiceChime()
        }
    }

    fun testUltronVoice(): String {
        return voiceManager.testCurrentUltronVoice()
    }

    override fun onCleared() {
        voiceManager.destroy()
        super.onCleared()
    }
}
