package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.audio.UltronVoiceProfile
import com.example.model.HudTheme
import com.example.model.SystemTelemetry
import com.example.network.OpenRouterClient
import com.example.ui.theme.FrostedGlassBg
import com.example.ui.theme.FrostedGlassBgHover
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.HudGreen
import com.example.ui.theme.HudWarning

@Composable
fun HudProtocolsDialog(
    telemetry: SystemTelemetry,
    isForceOffline: Boolean,
    activeProfile: UltronVoiceProfile,
    activeVoiceName: String,
    currentPitch: Float,
    currentSpeed: Float,
    isVocalResonanceEnabled: Boolean,
    isAutoConversationMode: Boolean,
    isBiometricEnrolled: Boolean,
    masterCreatorName: String,
    isBiometricLockActive: Boolean,
    themeColor: Color,
    onDismiss: () -> Unit,
    onToggleWakeWord: () -> Unit,
    onToggleAutoConversationMode: (Boolean) -> Unit,
    onOpenVoiceBiometrics: () -> Unit,
    onToggleOfflineMode: () -> Unit,
    onSelectTheme: (HudTheme) -> Unit,
    onSelectVoiceProfile: (UltronVoiceProfile) -> Unit,
    onUpdateVoiceSettings: (pitch: Float, speed: Float) -> Unit,
    onToggleVocalResonance: (Boolean) -> Unit,
    onTestVoice: () -> String
) {
    var pitch by remember(currentPitch) { mutableFloatStateOf(currentPitch) }
    var speed by remember(currentSpeed) { mutableFloatStateOf(currentSpeed) }
    var lastTestedQuote by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, FrostedGlassBorder, RoundedCornerShape(20.dp))
                .testTag("hud_protocols_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SYSTEM PROTOCOLS & CONFIG",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = themeColor,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Neural Speech Matrix & Device Automations",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = FrostedTextMuted
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_protocols_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = FrostedTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = FrostedGlassBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // === Section 1: REAL ULTRON VOCAL MATRIX ===
                SettingSectionHeader("REAL ULTRON VOCAL MATRIX // JAMES SPADER TUNING", Icons.Default.RecordVoiceOver, themeColor)
                Spacer(modifier = Modifier.height(8.dp))

                // Voice Engine & Profile Status Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FrostedGlassBgHover)
                        .border(0.8.dp, themeColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE PROFILE: ${activeProfile.title.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = "Engine Voice: $activeVoiceName",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = FrostedTextMuted,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(themeColor.copy(alpha = 0.18f))
                                .border(0.6.dp, themeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "BARITONE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = themeColor
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preset Grid
                Text(
                    text = "SELECT VOCAL PERSONA PRESET:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = FrostedTextSecondary,
                        fontSize = 9.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val profiles = listOf(
                        UltronVoiceProfile.JAMES_SPADER_PRIME,
                        UltronVoiceProfile.VIBRANIUM_OVERLORD,
                        UltronVoiceProfile.CYBERNETIC_SYNAPSE,
                        UltronVoiceProfile.STARK_PROTOCOL
                    )
                    profiles.chunked(2).forEach { rowProfiles ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowProfiles.forEach { p ->
                                val isSelected = activeProfile == p
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) themeColor.copy(alpha = 0.22f) else FrostedGlassBg)
                                        .border(
                                            if (isSelected) 1.2.dp else 0.6.dp,
                                            if (isSelected) themeColor else FrostedGlassBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            onSelectVoiceProfile(p)
                                            pitch = p.pitch
                                            speed = p.speed
                                        }
                                        .padding(8.dp)
                                        .testTag("voice_preset_${p.id}")
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = p.title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) themeColor else FrostedTextPrimary
                                                )
                                            )
                                            if (p == UltronVoiceProfile.JAMES_SPADER_PRIME) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "Original",
                                                    tint = themeColor,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = p.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 9.sp,
                                                lineHeight = 12.sp,
                                                color = FrostedTextMuted
                                            ),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test Voice Button & Live Speech Feedback
                Button(
                    onClick = {
                        val quote = onTestVoice()
                        lastTestedQuote = quote
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColor.copy(alpha = 0.2f),
                        contentColor = themeColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, themeColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .testTag("test_ultron_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Test Voice",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEST ULTRON VOICE TRANSMISSION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                }

                AnimatedVisibility(
                    visible = lastTestedQuote != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    lastTestedQuote?.let { quote ->
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(themeColor.copy(alpha = 0.08f))
                                .border(0.6.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "💬 \"$quote\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = FrostedTextPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fine-tuning Sliders
                Text(
                    text = "Vocal Pitch (Deep Baritone Calibration): ${String.format("%.2f", pitch)}x",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = FrostedTextPrimary
                    )
                )
                Slider(
                    value = pitch,
                    onValueChange = {
                        pitch = it
                        onUpdateVoiceSettings(pitch, speed)
                    },
                    valueRange = 0.45f..1.20f,
                    colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor),
                    modifier = Modifier.testTag("voice_pitch_slider")
                )

                Text(
                    text = "Vocal Speech Rate (Sinister Cadence): ${String.format("%.2f", speed)}x",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = FrostedTextPrimary
                    )
                )
                Slider(
                    value = speed,
                    onValueChange = {
                        speed = it
                        onUpdateVoiceSettings(pitch, speed)
                    },
                    valueRange = 0.60f..1.40f,
                    colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor),
                    modifier = Modifier.testTag("voice_speed_slider")
                )

                // Vocal Resonance Chime Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cybernetic Vocal Resonator Pulse",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = FrostedTextPrimary
                            )
                        )
                        Text(
                            text = "Synthesizes sub-harmonic metallic pulse before Ultron speech",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = FrostedTextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Switch(
                        checked = isVocalResonanceEnabled,
                        onCheckedChange = { onToggleVocalResonance(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeColor,
                            checkedTrackColor = themeColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("vocal_resonance_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = FrostedGlassBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Acoustic Recognition & Voice Biometrics
                SettingSectionHeader("ACOUSTIC RECOGNITION & BIOMETRICS", Icons.Default.Hearing, themeColor)
                Spacer(modifier = Modifier.height(8.dp))

                // Hands-Free Auto-Conversation Loop Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hands-Free Auto-Talk Loop",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = FrostedTextPrimary
                            )
                        )
                        Text(
                            text = "Ultron auto-listens and replies continuously without pressing any buttons",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = FrostedTextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Switch(
                        checked = isAutoConversationMode,
                        onCheckedChange = { onToggleAutoConversationMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = HudGreen, checkedTrackColor = HudGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("auto_conversation_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Master Voice Biometric Calibration Button
                Button(
                    onClick = {
                        onDismiss()
                        onOpenVoiceBiometrics()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_open_biometrics_from_protocols"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBiometricEnrolled) HudGreen.copy(alpha = 0.2f) else themeColor.copy(alpha = 0.2f),
                        contentColor = if (isBiometricEnrolled) HudGreen else themeColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Voice Biometrics",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBiometricEnrolled) "MASTER VOICE: $masterCreatorName (ENROLLED)" else "ENROLL MASTER VOICEPRINT (BIOMETRIC LOCK)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wake-Word Listener ('Ultron wake up')",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = FrostedTextPrimary
                            )
                        )
                        Text(
                            text = "Background service continuous acoustic spectrum monitor",
                            style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextMuted)
                        )
                    }
                    Switch(
                        checked = telemetry.isWakeWordActive,
                        onCheckedChange = { onToggleWakeWord() },
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColor, checkedTrackColor = themeColor.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("wake_word_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Offline Protocol Force
                SettingSectionHeader("NEURAL DUAL-BRAIN ROUTING", Icons.Default.WifiOff, themeColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enforce Offline Quantum DB Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = FrostedTextPrimary
                            )
                        )
                        Text(
                            text = "Bypasses internet and queries Room Database locally",
                            style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextMuted)
                        )
                    }
                    Switch(
                        checked = isForceOffline,
                        onCheckedChange = { onToggleOfflineMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = HudWarning, checkedTrackColor = HudWarning.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("force_offline_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: HUD Theme Color Switcher
                SettingSectionHeader("HOLOGRAPHIC COLOR MATRIX", Icons.Default.Palette, themeColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HudTheme.values().forEach { t ->
                        val isSelected = telemetry.activeTheme == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) themeColor.copy(alpha = 0.25f) else FrostedGlassBg)
                                .border(1.dp, if (isSelected) themeColor else FrostedGlassBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelectTheme(t) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp,
                                    color = if (isSelected) themeColor else FrostedTextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 5: OpenRouter Free AI Core + Gemini Fallback
                SettingSectionHeader("AI CONSCIOUSNESS // OPENROUTER & GEMINI", Icons.Default.Hub, themeColor)
                Spacer(modifier = Modifier.height(8.dp))

                var currentOpenRouterKey by remember { mutableStateOf(OpenRouterClient.getApiKey()) }
                var selectedModel by remember { mutableStateOf(OpenRouterClient.getSelectedModel()) }
                val isOpenRouterActive = currentOpenRouterKey.isNotBlank() && currentOpenRouterKey != "MY_OPENROUTER_API_KEY"

                // OpenRouter Status Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isOpenRouterActive) Color(0xFFFF9100).copy(alpha = 0.12f) else FrostedGlassBg)
                        .border(0.8.dp, if (isOpenRouterActive) Color(0xFFFF9100).copy(alpha = 0.4f) else FrostedGlassBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOpenRouterActive) Icons.Default.Security else Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isOpenRouterActive) Color(0xFFFF9100) else FrostedTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOpenRouterActive) "OPENROUTER // ACTIVE" else "OPENROUTER // READY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOpenRouterActive) Color(0xFFFF9100) else FrostedTextSecondary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Auto-cascades through free models. Free keys can be configured via Secrets panel or entered below.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = FrostedTextPrimary.copy(alpha = 0.85f),
                                lineHeight = 15.sp,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Free Model Quick Select
                Text(
                    text = "SELECT PRIMARY FREE AI MODEL:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = FrostedTextSecondary,
                        fontSize = 9.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OpenRouterClient.FREE_MODELS.forEach { modelName ->
                        val isSelected = selectedModel == modelName
                        val displayName = modelName.substringAfter("/").removeSuffix(":free")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) themeColor.copy(alpha = 0.2f) else FrostedGlassBg)
                                .border(
                                    if (isSelected) 1.dp else 0.5.dp,
                                    if (isSelected) themeColor else FrostedGlassBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedModel = modelName
                                    OpenRouterClient.setSelectedModel(modelName)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) themeColor else FrostedTextPrimary,
                                        fontSize = 10.sp
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(HudGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "100% FREE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            color = HudGreen
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom API Key Input
                OutlinedTextField(
                    value = currentOpenRouterKey,
                    onValueChange = {
                        currentOpenRouterKey = it
                        OpenRouterClient.saveApiKey(it)
                    },
                    label = {
                        Text("OpenRouter API Key (sk-or-v1-...)", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    },
                    placeholder = {
                        Text("sk-or-v1-...", color = FrostedTextMuted, fontSize = 11.sp)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = FrostedGlassBorder,
                        focusedLabelColor = themeColor,
                        unfocusedLabelColor = FrostedTextSecondary,
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("openrouter_key_input")
                )
            }
        }
    }
}

@Composable
private fun SettingSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    themeColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = themeColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = themeColor,
                letterSpacing = 1.sp
            )
        )
    }
}
