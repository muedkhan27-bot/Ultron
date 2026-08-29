package com.example.model

enum class SenderType {
    USER,
    ULTRON,
    SYSTEM
}

enum class ResponseSource {
    ONLINE_OPENROUTER,
    ONLINE_GEMINI,
    OFFLINE_DATABASE,
    SYSTEM_ACTION,
    WAKE_TRIGGER
}

data class UltronMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val sender: SenderType,
    val timestamp: Long = System.currentTimeMillis(),
    val source: ResponseSource = ResponseSource.ONLINE_GEMINI,
    val actionExecuted: String? = null,
    val isSpeaking: Boolean = false
)

data class SystemTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val ramUsedMb: Long = 0,
    val ramTotalMb: Long = 0,
    val isOnline: Boolean = true,
    val isListening: Boolean = false,
    val isWakeWordActive: Boolean = true,
    val activeTheme: HudTheme = HudTheme.ULTRON_RED
)

enum class HudTheme(val displayName: String) {
    ULTRON_RED("Ultron Crimson"),
    STARK_CYAN("Stark Arc Cyan"),
    MARK_GOLD("Avenger Amber")
}
