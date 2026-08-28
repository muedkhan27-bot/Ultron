package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SystemTelemetry
import com.example.ui.theme.FrostedGlassBg
import com.example.ui.theme.FrostedGlassBgHover
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.HudGreen
import com.example.ui.theme.HudWarning
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HudTelemetryHeader(
    telemetry: SystemTelemetry,
    themeColor: Color,
    isAutoConversationMode: Boolean = true,
    isBiometricEnrolled: Boolean = false,
    isBiometricLockActive: Boolean = false,
    onOpenKnowledgeBase: () -> Unit,
    onOpenVoiceBiometrics: () -> Unit,
    onToggleAutoConversationMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onCycleTheme: () -> Unit,
    onToggleOfflineMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormatter.format(now)
            currentDate = dateFormatter.format(now)
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dot")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FrostedGlassBg)
            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("hud_telemetry_header")
    ) {
        // Row 1: Brand title, Status Dot & Date/Time / Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: SYSTEM STATUS & Brand title & Live Active state
            Column {
                Text(
                    text = "SYSTEM STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = themeColor.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    text = "ULTRON-01",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Light,
                        color = FrostedTextPrimary,
                        letterSpacing = (-0.5).sp,
                        fontSize = 20.sp
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                (if (telemetry.isOnline) HudGreen else themeColor).copy(alpha = pulseAlpha)
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (telemetry.isOnline) "ONLINE CORE ACTIVE" else "OFFLINE CORE ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.5.sp,
                            color = FrostedTextMuted
                        )
                    )
                }
            }

            // Right: Date, Time & Quick Tool Icons
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = FrostedTextMuted
                        )
                    )
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Light,
                            color = FrostedTextPrimary,
                            fontSize = 17.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // Offline / Online toggle
                    FrostedHeaderIconButton(
                        onClick = onToggleOfflineMode,
                        testTag = "toggle_offline_button"
                    ) {
                        Icon(
                            imageVector = if (telemetry.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = "Toggle Offline/Online Mode",
                            tint = if (telemetry.isOnline) HudGreen else HudWarning,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Voice Biometrics Explorer
                    FrostedHeaderIconButton(
                        onClick = onOpenVoiceBiometrics,
                        testTag = "open_biometrics_button"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Master Voice Biometrics",
                            tint = if (isBiometricEnrolled) (if (isBiometricLockActive) HudGreen else themeColor) else FrostedTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Knowledge Base Explorer
                    FrostedHeaderIconButton(
                        onClick = onOpenKnowledgeBase,
                        testTag = "open_knowledge_button"
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Offline Knowledge Database",
                            tint = themeColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Theme switcher
                    FrostedHeaderIconButton(
                        onClick = onCycleTheme,
                        testTag = "cycle_theme_button"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Switch HUD Theme",
                            tint = themeColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Settings modal
                    FrostedHeaderIconButton(
                        onClick = onOpenSettings,
                        testTag = "open_settings_button"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "System Protocols & Settings",
                            tint = FrostedTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Live Telemetry Badges in Frosted Glass Container
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Auto-Talk Duplex Loop Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isAutoConversationMode) HudGreen.copy(alpha = 0.2f) else FrostedGlassBgHover)
                    .border(0.5.dp, if (isAutoConversationMode) HudGreen.copy(alpha = 0.5f) else FrostedGlassBorder, RoundedCornerShape(6.dp))
                    .clickable(onClick = onToggleAutoConversationMode)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = if (isAutoConversationMode) HudGreen else FrostedTextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isAutoConversationMode) "AUTO-TALK" else "AUTO-OFF",
                        color = if (isAutoConversationMode) HudGreen else FrostedTextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Battery Badge
            TelemetryPill(
                label = "PWR",
                value = "${telemetry.batteryPercent}%",
                icon = if (telemetry.isCharging) Icons.Default.Bolt else null,
                color = if (telemetry.batteryPercent < 20) HudWarning else themeColor
            )

            // RAM Allocation Badge
            TelemetryPill(
                label = "MEM",
                value = if (telemetry.ramTotalMb > 0) "${telemetry.ramUsedMb}MB" else "NOMINAL",
                color = themeColor.copy(alpha = 0.9f)
            )

            // Brain Source Badge
            TelemetryPill(
                label = "CORE",
                value = if (telemetry.isOnline) "GEMINI 3.5" else "ROOM DB",
                color = if (telemetry.isOnline) HudGreen else HudWarning
            )

            // Master Voice Badge
            TelemetryPill(
                label = "VOICE",
                value = if (isBiometricEnrolled) (if (isBiometricLockActive) "LOCKED" else "SAVED") else "OPEN",
                color = if (isBiometricEnrolled) HudGreen else themeColor
            )
        }
    }
}

@Composable
private fun FrostedHeaderIconButton(
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(FrostedGlassBgHover)
            .border(0.5.dp, FrostedGlassBorder, CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun TelemetryPill(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(FrostedGlassBgHover)
            .border(0.5.dp, FrostedGlassBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(9.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = FrostedTextMuted
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

