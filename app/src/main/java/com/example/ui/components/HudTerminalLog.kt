package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ResponseSource
import com.example.model.SenderType
import com.example.model.UltronMessage
import com.example.ui.theme.FrostedGlassBg
import com.example.ui.theme.FrostedGlassBgHover
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.ui.theme.FrostedTextSecondary
import com.example.ui.theme.HudGreen
import com.example.ui.theme.HudWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HudTerminalLog(
    messages: List<UltronMessage>,
    listState: LazyListState,
    themeColor: Color,
    onSpeakMessage: (UltronMessage) -> Unit,
    onClearLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FrostedGlassBg)
            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
            .testTag("hud_terminal_log")
    ) {
        // Terminal Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FrostedGlassBgHover)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "► NEURAL LOG // STREAM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        fontSize = 9.5.sp,
                        letterSpacing = 1.2.sp
                    )
                )
            }

            IconButton(
                onClick = onClearLog,
                modifier = Modifier.size(24.dp).testTag("clear_terminal_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Terminal Feed",
                    tint = FrostedTextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // Message Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageItem(
                    message = msg,
                    themeColor = themeColor,
                    onSpeak = { onSpeakMessage(msg) }
                )
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: UltronMessage,
    themeColor: Color,
    onSpeak: () -> Unit
) {
    val isUser = message.sender == SenderType.USER
    val isSystem = message.sender == SenderType.SYSTEM
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp))

    val borderColor = when {
        isUser -> Color.White.copy(alpha = 0.15f)
        isSystem -> HudWarning.copy(alpha = 0.35f)
        else -> themeColor.copy(alpha = 0.3f)
    }

    val bgColor = when {
        isUser -> FrostedGlassBgHover
        isSystem -> Color(0xFF1F170D).copy(alpha = 0.65f)
        else -> FrostedGlassBg
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .border(0.6.dp, borderColor, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .testTag("message_card_${message.id}")
        ) {
            Column {
                // Header: Sender icon, name, timestamp, and badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                isUser -> Icons.Default.Person
                                isSystem -> Icons.Default.CheckCircle
                                else -> Icons.Default.SmartToy
                            },
                            contentDescription = null,
                            tint = when {
                                isUser -> Color(0xFF80D8FF)
                                isSystem -> HudWarning
                                else -> themeColor
                            },
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isUser -> "USER"
                                isSystem -> "SYSTEM_PROTOCOL"
                                else -> "ULTRON"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                color = when {
                                    isUser -> Color(0xFF80D8FF)
                                    isSystem -> HudWarning
                                    else -> themeColor
                                }
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Action Badge
                        if (message.actionExecuted != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(HudGreen.copy(alpha = 0.15f))
                                    .border(0.5.dp, HudGreen.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = message.actionExecuted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HudGreen
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        // Source Badge (Gemini vs Room DB)
                        if (!isUser && !isSystem) {
                            val (sourceLabel, sourceColor) = when (message.source) {
                                ResponseSource.ONLINE_OPENROUTER -> Pair("OPENROUTER", Color(0xFFFF9100))
                                ResponseSource.ONLINE_GEMINI -> Pair("GEMINI", HudGreen)
                                ResponseSource.OFFLINE_DATABASE -> Pair("OFFLINE DB", HudWarning)
                                ResponseSource.SYSTEM_ACTION -> Pair("ACTION", Color(0xFF00E5FF))
                                ResponseSource.WAKE_TRIGGER -> Pair("WAKE", themeColor)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(sourceColor.copy(alpha = 0.12f))
                                    .border(0.5.dp, sourceColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = sourceLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        color = sourceColor
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                color = FrostedTextMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Text Body
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) FrostedTextPrimary else FrostedTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )

                // Vocal replay button for Ultron responses
                if (!isUser) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(22.dp).testTag("speak_message_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Vocalize response",
                                tint = themeColor.copy(alpha = 0.75f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
