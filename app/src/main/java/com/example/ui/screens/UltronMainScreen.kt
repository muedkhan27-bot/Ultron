package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.HudTheme
import com.example.ui.components.HolographicReactorCore
import com.example.ui.components.HudActionChips
import com.example.ui.components.HudProtocolsDialog
import com.example.ui.components.HudTelemetryHeader
import com.example.ui.components.HudTerminalLog
import com.example.ui.components.OfflineKnowledgeDialog
import com.example.ui.components.VoiceEnrollmentDialog
import com.example.ui.theme.AmbientAmberGlow
import com.example.ui.theme.AmbientCrimsonGlow
import com.example.ui.theme.FrostedDarkBg
import com.example.ui.theme.FrostedGlassBg
import com.example.ui.theme.FrostedGlassBgHover
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.StarkCyanPrimary
import com.example.ui.theme.UltronDarkBg
import com.example.ui.theme.UltronPrimary
import com.example.ui.theme.MarkGoldPrimary
import com.example.viewmodel.CoreHudState
import com.example.viewmodel.UltronViewModel

@Composable
fun UltronMainScreen(
    viewModel: UltronViewModel,
    onRequestPermissions: () -> Unit
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val coreState by viewModel.coreState.collectAsStateWithLifecycle()
    val partialSpeech by viewModel.partialSpeech.collectAsStateWithLifecycle()
    val isForceOffline by viewModel.forceOfflineMode.collectAsStateWithLifecycle()
    val knowledgeList by viewModel.offlineKnowledgeList.collectAsStateWithLifecycle()
    val audioRms by viewModel.voiceManager.audioRms.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()
    val activeVoiceProfile by viewModel.voiceManager.activeProfile.collectAsStateWithLifecycle()
    val activeVoiceName by viewModel.voiceManager.activeVoiceName.collectAsStateWithLifecycle()
    val masterVoiceProfile by viewModel.masterVoiceProfile.collectAsStateWithLifecycle()
    val lastBiometricVerification by viewModel.lastBiometricVerification.collectAsStateWithLifecycle()
    val isAutoConversationMode by viewModel.isAutoConversationMode.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    var showKnowledgeDialog by remember { mutableStateOf(false) }
    var showProtocolsDialog by remember { mutableStateOf(false) }
    var showVoiceBiometricsDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Dynamic HUD Theme Color
    val themeColor = when (telemetry.activeTheme) {
        HudTheme.ULTRON_RED -> UltronPrimary
        HudTheme.STARK_CYAN -> StarkCyanPrimary
        HudTheme.MARK_GOLD -> MarkGoldPrimary
    }

    // Auto scroll terminal log on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Mic Button Pulse Animation when Listening
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FrostedDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("ultron_main_screen")
    ) {
        // Ambient Frosted Glass Background Glow Effects
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Top-left Crimson / Theme Ambient Blur
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(themeColor.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.75f
                ),
                center = Offset(0f, 0f),
                radius = size.width * 0.75f
            )

            // 2. Center-right Amber Ambient Blur
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AmbientAmberGlow.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width, size.height * 0.45f),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width, size.height * 0.45f),
                radius = size.width * 0.7f
            )

            // 3. Bottom Gradient Fog
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, themeColor.copy(alpha = 0.08f), FrostedDarkBg),
                    startY = size.height * 0.6f,
                    endY = size.height
                )
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Telemetry Header (Frosted Glass)
                HudTelemetryHeader(
                    telemetry = telemetry,
                    themeColor = themeColor,
                    isAutoConversationMode = isAutoConversationMode,
                    isBiometricEnrolled = masterVoiceProfile.isEnrolled,
                    isBiometricLockActive = masterVoiceProfile.isBiometricLockEnabled,
                    onOpenKnowledgeBase = { showKnowledgeDialog = true },
                    onOpenVoiceBiometrics = { showVoiceBiometricsDialog = true },
                    onToggleAutoConversationMode = { viewModel.toggleAutoConversationMode() },
                    onOpenSettings = { showProtocolsDialog = true },
                    onCycleTheme = { viewModel.cycleTheme() },
                    onToggleOfflineMode = { viewModel.toggleOfflineMode() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Holographic Arc Reactor Core Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HolographicReactorCore(
                        coreState = coreState,
                        audioRms = audioRms,
                        isListening = coreState == CoreHudState.LISTENING,
                        themeColor = themeColor,
                        telemetry = telemetry,
                        onClick = {
                            onRequestPermissions()
                            if (coreState == CoreHudState.LISTENING) {
                                viewModel.stopVoiceListening()
                            } else {
                                viewModel.triggerVoiceListening()
                            }
                        }
                    )
                }

                // 3. Live Recognized Transcript / Wake Word Status Banner
                AnimatedVisibility(
                    visible = partialSpeech.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FrostedGlassBgHover)
                            .border(1.dp, themeColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("partial_speech_banner"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = partialSpeech,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = FrostedTextPrimary,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 4. Quick Action Chips Row (Frosted Pill Style)
                HudActionChips(
                    themeColor = themeColor,
                    onExecuteAction = { command ->
                        viewModel.processUserPrompt(command)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Scrollable Holographic Dialogue Terminal Feed (Frosted Glass Container)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    HudTerminalLog(
                        messages = messages,
                        listState = listState,
                        themeColor = themeColor,
                        onSpeakMessage = { msg -> viewModel.speakMessage(msg) },
                        onClearLog = { viewModel.clearLog() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Frosted Glass Voice & Manual Command Input Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(FrostedGlassBg)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Frosted Mic Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(if (coreState == CoreHudState.LISTENING) micPulseScale else 1f),
                            contentAlignment = Alignment.Center
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    onRequestPermissions()
                                    if (coreState == CoreHudState.LISTENING) {
                                        viewModel.stopVoiceListening()
                                    } else {
                                        viewModel.triggerVoiceListening()
                                    }
                                },
                                shape = CircleShape,
                                containerColor = if (coreState == CoreHudState.LISTENING) {
                                    themeColor
                                } else {
                                    themeColor.copy(alpha = 0.18f)
                                },
                                contentColor = if (coreState == CoreHudState.LISTENING) Color.White else themeColor,
                                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, if (coreState == CoreHudState.LISTENING) themeColor else themeColor.copy(alpha = 0.35f), CircleShape)
                                    .testTag("voice_mic_fab")
                            ) {
                                Icon(
                                    imageVector = if (coreState == CoreHudState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Voice Directive Input",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text Command Field
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    text = "Awaiting directive... (or 'Ultron wake up')",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = FrostedTextMuted,
                                        fontSize = 12.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("directive_text_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = themeColor,
                                focusedTextColor = FrostedTextPrimary,
                                unfocusedTextColor = FrostedTextPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.processUserPrompt(textInput)
                                        textInput = ""
                                        focusManager.clearFocus()
                                    }
                                }
                            )
                        )

                        // Transmit Directive Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (textInput.isNotBlank()) themeColor.copy(alpha = 0.2f) else FrostedGlassBg)
                                .border(0.5.dp, if (textInput.isNotBlank()) themeColor.copy(alpha = 0.5f) else FrostedGlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.processUserPrompt(textInput)
                                        textInput = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                modifier = Modifier.size(38.dp).testTag("send_directive_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Transmit Directive",
                                    tint = if (textInput.isNotBlank()) themeColor else FrostedTextMuted,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // Micro Footer Diagnostic Labels
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = "DIAGNOSTIC",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            letterSpacing = 1.5.sp,
                            color = FrostedTextMuted
                        )
                    )
                    Text(
                        text = "CONNECTIVITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            letterSpacing = 1.5.sp,
                            color = FrostedTextMuted
                        )
                    )
                    Text(
                        text = "ENVIRONMENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            letterSpacing = 1.5.sp,
                            color = FrostedTextMuted
                        )
                    )
                }
            }
        }

        // Offline Knowledge Base Dialog
        if (showKnowledgeDialog) {
            OfflineKnowledgeDialog(
                knowledgeList = knowledgeList,
                themeColor = themeColor,
                onDismiss = { showKnowledgeDialog = false },
                onSelectKnowledge = { item ->
                    viewModel.processUserPrompt(item.question)
                }
            )
        }

        // System Protocols & Settings Dialog
        if (showProtocolsDialog) {
            HudProtocolsDialog(
                telemetry = telemetry,
                isForceOffline = isForceOffline,
                activeProfile = activeVoiceProfile,
                activeVoiceName = activeVoiceName,
                currentPitch = viewModel.voiceManager.voicePitch,
                currentSpeed = viewModel.voiceManager.voiceSpeed,
                isVocalResonanceEnabled = viewModel.voiceManager.isVocalResonanceEnabled,
                isAutoConversationMode = isAutoConversationMode,
                isBiometricEnrolled = masterVoiceProfile.isEnrolled,
                masterCreatorName = masterVoiceProfile.creatorName,
                isBiometricLockActive = masterVoiceProfile.isBiometricLockEnabled,
                themeColor = themeColor,
                onDismiss = { showProtocolsDialog = false },
                onToggleWakeWord = { viewModel.toggleWakeWordBackgroundService() },
                onToggleAutoConversationMode = { viewModel.toggleAutoConversationMode(it) },
                onOpenVoiceBiometrics = { showVoiceBiometricsDialog = true },
                onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                onSelectTheme = { theme ->
                    if (telemetry.activeTheme != theme) {
                        viewModel.cycleTheme()
                    }
                },
                onSelectVoiceProfile = { profile ->
                    viewModel.applyVoiceProfile(profile)
                },
                onUpdateVoiceSettings = { pitch, speed ->
                    viewModel.updateVoiceSettings(pitch, speed)
                },
                onToggleVocalResonance = { enabled ->
                    viewModel.toggleVocalResonance(enabled)
                },
                onTestVoice = {
                    viewModel.testUltronVoice()
                }
            )
        }

        // Master Voice Biometric Calibration Dialog
        if (showVoiceBiometricsDialog) {
            VoiceEnrollmentDialog(
                masterProfile = masterVoiceProfile,
                lastVerification = lastBiometricVerification,
                audioRms = audioRms,
                isListening = coreState == CoreHudState.LISTENING,
                themeColor = themeColor,
                onDismiss = { showVoiceBiometricsDialog = false },
                onEnrollMasterVoice = { creatorName ->
                    viewModel.completeVoiceEnrollment(creatorName)
                },
                onToggleBiometricLock = { enabled ->
                    viewModel.toggleBiometricLock(enabled)
                },
                onDeleteMasterVoice = {
                    viewModel.deleteMasterVoiceProfile()
                },
                onTriggerListenTest = {
                    onRequestPermissions()
                    viewModel.triggerVoiceListening()
                }
            )
        }
    }
}
