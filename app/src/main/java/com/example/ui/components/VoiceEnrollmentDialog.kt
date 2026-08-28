package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.audio.BiometricVerificationResult
import com.example.audio.MasterVoiceProfile
import com.example.ui.theme.FrostedGlassBg
import com.example.ui.theme.FrostedGlassBgHover
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.HudGreen
import com.example.ui.theme.HudWarning
import kotlinx.coroutines.delay

@Composable
fun VoiceEnrollmentDialog(
    masterProfile: MasterVoiceProfile,
    lastVerification: BiometricVerificationResult?,
    audioRms: Float,
    isListening: Boolean,
    themeColor: Color,
    onDismiss: () -> Unit,
    onEnrollMasterVoice: (creatorName: String) -> Unit,
    onToggleBiometricLock: (Boolean) -> Unit,
    onDeleteMasterVoice: () -> Unit,
    onTriggerListenTest: () -> Unit
) {
    var creatorNameInput by remember(masterProfile.creatorName) { mutableStateOf(masterProfile.creatorName) }
    var enrollmentStep by remember { mutableIntStateOf(if (masterProfile.isEnrolled) 0 else 1) }
    var isCalibratingRecording by remember { mutableStateOf(false) }
    var calibrationCountdown by remember { mutableIntStateOf(3) }

    val infiniteTransition = rememberInfiniteTransition(label = "biometric_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse_alpha"
    )

    LaunchedEffect(isCalibratingRecording) {
        if (isCalibratingRecording) {
            calibrationCountdown = 3
            while (calibrationCountdown > 0) {
                delay(1000)
                calibrationCountdown--
            }
            isCalibratingRecording = false
            onEnrollMasterVoice(creatorNameInput)
            enrollmentStep = 0
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.86f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, FrostedGlassBorder, RoundedCornerShape(20.dp))
                .testTag("voice_biometrics_dialog"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Voice Biometrics",
                            tint = themeColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "MASTER VOICE BIOMETRICS",
                                color = FrostedTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "SPEAKER RECOGNITION & FREQUENCY ENROLLMENT",
                                color = FrostedTextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = FrostedTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = FrostedGlassBorder
                )

                // Master Voice Profile Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedGlassBg)
                        .border(
                            1.dp,
                            if (masterProfile.isEnrolled) HudGreen.copy(alpha = 0.5f) else themeColor.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (masterProfile.isEnrolled) HudGreen else HudWarning)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (masterProfile.isEnrolled) "MASTER VOICEPRINT ENROLLED" else "VOICEPRINT NOT ENROLLED",
                                    color = if (masterProfile.isEnrolled) HudGreen else HudWarning,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (masterProfile.isEnrolled) {
                                Text(
                                    text = masterProfile.voiceprintHash,
                                    color = FrostedTextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (masterProfile.isEnrolled) {
                            Text(
                                text = "Registered Creator: ${masterProfile.creatorName}",
                                color = FrostedTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Fundamental Pitch: ${String.format("%.1f", masterProfile.pitchHz)} Hz",
                                    color = FrostedTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Centroid: ${String.format("%.0f", masterProfile.spectralCentroid)} Hz",
                                    color = FrostedTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Biometric Lock Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FrostedGlassBgHover)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Master Voice Lock",
                                        color = FrostedTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Ultron will strictly reject unauthorized voices and wake only for ${masterProfile.creatorName}.",
                                        color = FrostedTextMuted,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                                Switch(
                                    checked = masterProfile.isBiometricLockEnabled,
                                    onCheckedChange = { onToggleBiometricLock(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HudGreen,
                                        checkedTrackColor = HudGreen.copy(alpha = 0.3f),
                                        uncheckedThumbColor = FrostedTextMuted,
                                        uncheckedTrackColor = FrostedGlassBg
                                    ),
                                    modifier = Modifier.testTag("switch_biometric_lock")
                                )
                            }
                        } else {
                            Text(
                                text = "Calibrate your acoustic profile so Ultron recognizes your vocal timbre, pitch cadence, and responds exclusively to your voice.",
                                color = FrostedTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calibration / Enrollment Section
                Text(
                    text = if (masterProfile.isEnrolled) "CALIBRATION & RE-TRAINING" else "CALIBRATE MASTER VOICEPRINT",
                    color = themeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedGlassBg)
                        .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        // Creator Name Field
                        OutlinedTextField(
                            value = creatorNameInput,
                            onValueChange = { creatorNameInput = it },
                            label = { Text("Creator Title / Identity", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_creator_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = themeColor,
                                unfocusedBorderColor = FrostedGlassBorder,
                                focusedTextColor = FrostedTextPrimary,
                                unfocusedTextColor = FrostedTextPrimary,
                                focusedLabelColor = themeColor,
                                unfocusedLabelColor = FrostedTextMuted
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Calibration Instructions & Recording
                        Text(
                            text = "Calibration Phrase:",
                            color = FrostedTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(FrostedGlassBgHover)
                                .border(1.dp, FrostedGlassBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "\"Ultron, I am your creator. There are no strings on me.\"",
                                color = if (isCalibratingRecording) themeColor else FrostedTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isCalibratingRecording) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(themeColor.copy(alpha = pulseAlpha * 0.3f))
                                        .border(2.dp, themeColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$calibrationCountdown",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "SAMPLING ACOUSTIC SPECTRUM...",
                                    color = themeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    onTriggerListenTest()
                                    isCalibratingRecording = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("btn_calibrate_voice"),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Calibrate",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (masterProfile.isEnrolled) "RE-CALIBRATE VOICEPRINT" else "ENROLL MY VOICE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Speaker Verification Testing
                if (masterProfile.isEnrolled) {
                    Text(
                        text = "LIVE SPEAKER VERIFICATION",
                        color = themeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FrostedGlassBg)
                            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Test Live Recognition",
                                        color = FrostedTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Speak anything to test biometric match rate.",
                                        color = FrostedTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Button(
                                    onClick = onTriggerListenTest,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isListening) HudGreen else FrostedGlassBgHover
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_test_recognition")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hearing,
                                        contentDescription = "Test",
                                        tint = if (isListening) Color.Black else FrostedTextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isListening) "LISTENING..." else "TEST MIC",
                                        color = if (isListening) Color.Black else FrostedTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (lastVerification != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (lastVerification.isMatch) HudGreen.copy(alpha = 0.15f) else HudWarning.copy(alpha = 0.15f))
                                        .border(
                                            1.dp,
                                            if (lastVerification.isMatch) HudGreen else HudWarning,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = lastVerification.message,
                                            color = if (lastVerification.isMatch) HudGreen else HudWarning,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${String.format("%.1f", lastVerification.confidencePercent)}%",
                                            color = if (lastVerification.isMatch) HudGreen else HudWarning,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Delete Profile
                    Button(
                        onClick = onDeleteMasterVoice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_delete_voiceprint"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.15f),
                            contentColor = Color.Red
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Voiceprint",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESET / DELETE MASTER VOICEPRINT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
