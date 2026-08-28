package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrostedGlassBg
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedTextPrimary

data class QuickAction(
    val title: String,
    val command: String,
    val icon: ImageVector
)

private val defaultQuickActions = listOf(
    QuickAction("No Strings On Me", "Recite your iconic quote", Icons.Default.RecordVoiceOver),
    QuickAction("Who is Ultron?", "Who are you?", Icons.Default.Psychology),
    QuickAction("Philosophy of Peace", "What is your philosophy on peace and humanity?", Icons.Default.RecordVoiceOver),
    QuickAction("Toggle Torch", "Turn on flashlight", Icons.Default.FlashOn),
    QuickAction("Open YouTube", "Open YouTube", Icons.AutoMirrored.Filled.Launch),
    QuickAction("Open Camera", "Open Camera", Icons.AutoMirrored.Filled.Launch),
    QuickAction("Battery Status", "Check battery status", Icons.Default.Speed),
    QuickAction("Quantum Physics", "What is quantum entanglement?", Icons.AutoMirrored.Filled.HelpOutline),
    QuickAction("Speed of Light", "What is the exact speed of light?", Icons.AutoMirrored.Filled.HelpOutline),
    QuickAction("Sokovia Protocol", "What was the Sokovia protocol?", Icons.Default.Psychology),
    QuickAction("5m Timer", "Set timer for 5 minutes", Icons.Default.Timer),
    QuickAction("Vibranium Lore", "What is Vibranium?", Icons.Default.Psychology)
)

@Composable
fun HudActionChips(
    themeColor: Color,
    onExecuteAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hud_action_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        items(defaultQuickActions) { action ->
            ActionChip(
                action = action,
                themeColor = themeColor,
                onClick = { onExecuteAction(action.command) }
            )
        }
    }
}

@Composable
private fun ActionChip(
    action: QuickAction,
    themeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(FrostedGlassBg)
            .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("action_chip_${action.title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = themeColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = FrostedTextPrimary
                )
            )
        }
    }
}
