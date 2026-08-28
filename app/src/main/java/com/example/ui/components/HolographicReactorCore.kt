package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SystemTelemetry
import com.example.ui.theme.FrostedDarkBg
import com.example.ui.theme.FrostedGlassBg
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedTextMuted
import com.example.ui.theme.FrostedTextPrimary
import com.example.viewmodel.CoreHudState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HolographicReactorCore(
    coreState: CoreHudState,
    audioRms: Float,
    isListening: Boolean,
    themeColor: Color,
    telemetry: SystemTelemetry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_anim")

    // Continuous smooth rotations
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_outer"
    )

    val rotationMiddle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_middle"
    )

    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_inner"
    )

    // Breathing pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Dynamic scale combining breathing pulse + live voice RMS amplitude
    val dynamicAudioScale = (pulseScale + (audioRms * 0.28f)).coerceIn(0.85f, 1.45f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("holographic_reactor_core"),
        contentAlignment = Alignment.Center
    ) {
        // Core Arc Canvas Visualizer
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.minDimension / 2f

                // 1. Ambient Glass Glow Backdrop
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            themeColor.copy(alpha = if (coreState == CoreHudState.LISTENING) 0.35f else 0.20f),
                            themeColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = maxRadius * dynamicAudioScale
                    ),
                    radius = maxRadius * dynamicAudioScale,
                    center = center
                )

                // 2. Dashed Outer Ring (border-dashed border-red-900/40)
                rotate(rotationOuter, pivot = center) {
                    drawCircle(
                        color = themeColor.copy(alpha = 0.35f),
                        radius = maxRadius * 0.96f,
                        center = center,
                        style = Stroke(
                            width = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    )

                    // 4 Orbital Satellite Nodes
                    for (i in 0 until 4) {
                        val angle = (i * 90f) * (PI.toFloat() / 180f)
                        val nodePos = Offset(
                            center.x + (maxRadius * 0.96f) * cos(angle),
                            center.y + (maxRadius * 0.96f) * sin(angle)
                        )
                        drawCircle(color = themeColor, radius = 3.5f, center = nodePos)
                    }
                }

                // 3. Middle Concentric Frosted Hairline Ring (border-[1px] border-red-500/20)
                drawCircle(
                    color = themeColor.copy(alpha = 0.22f),
                    radius = maxRadius * 0.82f,
                    center = center,
                    style = Stroke(width = 1f)
                )

                // 4. Fine Translucent Glass Ring (border-[0.5px] border-white/10)
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = maxRadius * 0.68f,
                    center = center,
                    style = Stroke(width = 0.8f)
                )

                // 5. Segmented HUD Rotating Arc Brackets
                rotate(rotationMiddle, pivot = center) {
                    for (i in 0 until 4) {
                        drawArc(
                            color = themeColor.copy(alpha = 0.8f),
                            startAngle = i * 90f + 15f,
                            sweepAngle = 60f,
                            useCenter = false,
                            topLeft = Offset(center.x - maxRadius * 0.58f, center.y - maxRadius * 0.58f),
                            size = Size(maxRadius * 1.16f, maxRadius * 1.16f),
                            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                        )
                    }
                }

                // 6. Inner Hexagonal Core Matrix
                rotate(rotationInner, pivot = center) {
                    val hexRadius = maxRadius * 0.44f * dynamicAudioScale
                    drawHexagon(center = center, radius = hexRadius, color = themeColor.copy(alpha = 0.7f), strokeWidth = 1.8f)

                    // Reticle targeting crosshairs
                    drawLine(
                        color = themeColor.copy(alpha = 0.45f),
                        start = Offset(center.x - hexRadius * 1.2f, center.y),
                        end = Offset(center.x + hexRadius * 1.2f, center.y),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = themeColor.copy(alpha = 0.45f),
                        start = Offset(center.x, center.y - hexRadius * 1.2f),
                        end = Offset(center.x, center.y + hexRadius * 1.2f),
                        strokeWidth = 1f
                    )
                }

                // 7. Central Conscious Nucleus (Deep Black Core with Neon Arc Bulb)
                val coreRadius = (maxRadius * 0.22f * dynamicAudioScale).coerceAtLeast(14f)
                drawCircle(
                    color = FrostedDarkBg,
                    radius = coreRadius * 1.4f,
                    center = center
                )
                drawCircle(
                    color = themeColor.copy(alpha = 0.6f),
                    radius = coreRadius * 1.4f,
                    center = center,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            themeColor,
                            themeColor.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = coreRadius * 1.2f
                    ),
                    radius = coreRadius,
                    center = center
                )

                // 8. Audio Oscilloscope Waveforms
                if (isListening || coreState == CoreHudState.SPEAKING) {
                    drawAudioOscilloscope(center = center, radius = maxRadius * 0.36f, audioRms = audioRms, color = themeColor)
                }
            }

            // Neural Sync Pill Tag
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(4.dp))
                    .background(FrostedDarkBg)
                    .border(0.5.dp, themeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (coreState == CoreHudState.LISTENING) "AUDIO SYNC" else "NEURAL SYNC",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Core State Subtitle Label
            Text(
                text = when (coreState) {
                    CoreHudState.STANDBY -> "ULTRON-01"
                    CoreHudState.WAKING -> "INITIALIZING"
                    CoreHudState.LISTENING -> "LISTENING"
                    CoreHudState.COMPUTING -> "COMPUTING"
                    CoreHudState.SPEAKING -> "TRANSMITTING"
                    CoreHudState.ERROR -> "CORE FAULT"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = FrostedTextPrimary
                ),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
            )
        }

        // Left Frosted Telemetry Mini Cards (CPU & MEM)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 2.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FrostedMiniCard(
                label = "CPU",
                value = "14%",
                themeColor = themeColor
            )
            FrostedMiniCard(
                label = "MEM",
                value = if (telemetry.ramUsedMb > 0) "${(telemetry.ramUsedMb / 1024f).let { String.format("%.1fGB", it) }}" else "2.4GB",
                themeColor = themeColor
            )
        }

        // Right Frosted Telemetry Mini Cards (TEMP & PWR)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 2.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            FrostedMiniCard(
                label = "TEMP",
                value = "38°C",
                themeColor = themeColor
            )
            FrostedMiniCard(
                label = "PWR",
                value = "${telemetry.batteryPercent}%",
                themeColor = themeColor
            )
        }
    }
}

@Composable
private fun FrostedMiniCard(
    label: String,
    value: String,
    themeColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FrostedGlassBg)
            .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = FrostedTextMuted,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
            )
        }
    }
}

private fun DrawScope.drawHexagon(center: Offset, radius: Float, color: Color, strokeWidth: Float) {
    val path = Path()
    for (i in 0..6) {
        val angle = (i * 60f) * (PI.toFloat() / 180f)
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
}

private fun DrawScope.drawAudioOscilloscope(center: Offset, radius: Float, audioRms: Float, color: Color) {
    val barCount = 16
    for (i in 0 until barCount) {
        val angle = (i * (360f / barCount)) * (PI.toFloat() / 180f)
        val jitter = (sin(i * 1.4f + audioRms * 10f) * 8f * audioRms).toFloat()
        val startR = radius
        val endR = radius + 6f + (audioRms * 18f) + jitter
        val p1 = Offset(center.x + startR * cos(angle), center.y + startR * sin(angle))
        val p2 = Offset(center.x + endR * cos(angle), center.y + endR * sin(angle))
        drawLine(color = color.copy(alpha = 0.8f), start = p1, end = p2, strokeWidth = 2f, cap = StrokeCap.Round)
    }
}

