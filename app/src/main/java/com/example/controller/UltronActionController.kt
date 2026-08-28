package com.example.controller

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log

sealed class ActionResult {
    data class Success(val message: String, val actionBadge: String) : ActionResult()
    data class Handled(val message: String, val actionBadge: String) : ActionResult()
    data class Failure(val error: String) : ActionResult()
    object NotAnAction : ActionResult()
}

class UltronActionController(private val context: Context) {

    private var isTorchOn = false

    /**
     * Attempts to parse and execute a phone action from a user's prompt.
     * Returns ActionResult if recognized, or ActionResult.NotAnAction if standard conversational query.
     */
    fun evaluateAndExecute(command: String): ActionResult {
        val lower = command.trim().lowercase()

        // 1. Flashlight / Torch
        if (lower.contains("torch") || lower.contains("flashlight")) {
            return if (lower.contains("off") || lower.contains("disable") || lower.contains("stop")) {
                setFlashlight(false)
            } else {
                setFlashlight(true)
            }
        }

        // 2. Open / Launch Applications
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ")) {
            val appTarget = lower.replaceFirst("^(open|launch|start)\\s+".toRegex(), "")
                .replace("the app", "")
                .replace("app", "")
                .trim()
            if (appTarget.isNotBlank()) {
                return launchApplication(appTarget)
            }
        }

        // 3. Make Phone Call
        if (lower.startsWith("call ") || lower.startsWith("dial ")) {
            val target = lower.replaceFirst("^(call|dial)\\s+".toRegex(), "").trim()
            return makePhoneCall(target)
        }

        // 4. Send Message / SMS
        if (lower.startsWith("send message") || lower.startsWith("text ") || lower.startsWith("send sms") || lower.startsWith("msg ")) {
            return parseAndSendMessage(lower)
        }

        // 5. Set Alarm / Timer
        if (lower.contains("alarm") || lower.contains("wake me up at")) {
            return parseAndSetAlarm(lower)
        }
        if (lower.contains("timer")) {
            return parseAndSetTimer(lower)
        }

        // 6. Battery / Power Diagnostics
        if (lower.contains("battery") || lower.contains("power level") || lower.contains("charge status")) {
            val batteryInfo = getBatteryStatus()
            return ActionResult.Success(
                "Power cell status: ${batteryInfo.first}% capacity, ${if (batteryInfo.second) "charging via arc grid" else "discharging on internal cell"}.",
                "[BATTERY: ${batteryInfo.first}%]"
            )
        }

        // 7. System Memory / RAM Status
        if (lower.contains("memory") || lower.contains("ram") || lower.contains("system status") || lower.contains("diagnostics")) {
            val (usedMb, totalMb) = getMemoryStatus()
            return ActionResult.Success(
                "Memory allocation: $usedMb MB active / $totalMb MB total bandwidth. Neural subsystems operating within normal parameters.",
                "[SYS_RAM: $usedMb/$totalMb MB]"
            )
        }

        // 8. Device Settings Shortcuts
        if (lower.contains("wifi") || lower.contains("wi-fi")) {
            return openSettings(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi control panel", "[NET_CONFIG]")
        }
        if (lower.contains("bluetooth")) {
            return openSettings(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth transceiver settings", "[BT_CONFIG]")
        }
        if (lower.contains("device settings") || lower == "settings") {
            return openSettings(Settings.ACTION_SETTINGS, "Android core system preferences", "[SYS_SETTINGS]")
        }

        // 9. Web Search
        if (lower.startsWith("search ") || lower.startsWith("google ") || lower.startsWith("look up ")) {
            val query = lower.replaceFirst("^(search|google|look up)\\s+(for\\s+)?".toRegex(), "").trim()
            return searchWeb(query)
        }

        return ActionResult.NotAnAction
    }

    private fun setFlashlight(enable: Boolean): ActionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return ActionResult.Failure("No optical flash emitter detected on device.")
            cameraManager.setTorchMode(cameraId, enable)
            isTorchOn = enable
            if (enable) {
                ActionResult.Success("Optical emitter ignited. Flashlight operational.", "[TORCH: ON]")
            } else {
                ActionResult.Success("Optical emitter extinguished.", "[TORCH: OFF]")
            }
        } catch (e: Exception) {
            ActionResult.Failure("Failed to toggle optical flash: ${e.message}")
        }
    }

    private fun launchApplication(appName: String): ActionResult {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        // Known aliases
        val targetName = when (appName) {
            "camera" -> "camera"
            "browser", "internet", "chrome" -> "chrome"
            "youtube" -> "youtube"
            "music", "spotify" -> "spotify"
            "maps", "navigation" -> "maps"
            "whatsapp" -> "whatsapp"
            "calc", "calculator" -> "calculator"
            "gallery", "photos" -> "gallery"
            "clock", "alarms" -> "clock"
            "play store", "store" -> "vending"
            else -> appName
        }

        // Find match in installed packages
        var matchedPkg: String? = null
        var matchedLabel: String? = null

        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            val pkg = app.packageName.lowercase()

            if (label == targetName || pkg.contains(targetName)) {
                matchedPkg = app.packageName
                matchedLabel = pm.getApplicationLabel(app).toString()
                break
            }
        }

        // Fuzzy match
        if (matchedPkg == null) {
            for (app in installedApps) {
                val label = pm.getApplicationLabel(app).toString().lowercase()
                if (label.contains(targetName) || targetName.contains(label)) {
                    matchedPkg = app.packageName
                    matchedLabel = pm.getApplicationLabel(app).toString()
                    break
                }
            }
        }

        if (matchedPkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(matchedPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return ActionResult.Handled(
                    "Executing protocol: Initializing $matchedLabel application.",
                    "[LAUNCH: ${matchedLabel?.uppercase()}]"
                )
            }
        }

        // Fallback for special core intents
        if (targetName.contains("camera")) {
            val camIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(camIntent)
            return ActionResult.Handled("Optical sensor array active.", "[LAUNCH: CAMERA]")
        }

        return ActionResult.Failure("Target application '$appName' not located within the local filesystem.")
    }

    private fun makePhoneCall(target: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${target.replace("[^0-9+]".toRegex(), "")}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Handled("Establishing audio transmission link to $target.", "[CALL: $target]")
        } catch (e: Exception) {
            ActionResult.Failure("Telecommunication link failed: ${e.message}")
        }
    }

    private fun parseAndSendMessage(text: String): ActionResult {
        return try {
            // e.g. "send message to 123456 saying hello" or "text John hello"
            var recipient = ""
            var body = ""

            if (text.contains("saying")) {
                val parts = text.split("saying", limit = 2)
                recipient = parts[0].replaceFirst("^(send message|send sms|text|msg)\\s+(to\\s+)?".toRegex(), "").trim()
                body = parts[1].trim()
            } else {
                val cleaned = text.replaceFirst("^(send message|send sms|text|msg)\\s+(to\\s+)?".toRegex(), "").trim()
                val parts = cleaned.split(" ", limit = 2)
                recipient = parts.getOrNull(0) ?: ""
                body = parts.getOrNull(1) ?: ""
            }

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$recipient")
                if (body.isNotBlank()) {
                    putExtra("sms_body", body)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Handled("Constructed transmission payload to $recipient.", "[SMS: $recipient]")
        } catch (e: Exception) {
            ActionResult.Failure("Messaging matrix exception: ${e.message}")
        }
    }

    private fun parseAndSetAlarm(text: String): ActionResult {
        return try {
            // Search for time patterns like "7", "7:30", "8 am", "6:00 pm"
            val hourRegex = "(\\d{1,2})(:(\\d{2}))?\\s*(am|pm)?".toRegex()
            val match = hourRegex.find(text)
            var hour = 7
            var minute = 0

            if (match != null) {
                val h = match.groupValues[1].toIntOrNull() ?: 7
                val m = match.groupValues[3].toIntOrNull() ?: 0
                val ampm = match.groupValues[4]

                hour = when {
                    ampm == "pm" && h < 12 -> h + 12
                    ampm == "am" && h == 12 -> 0
                    else -> h
                }
                minute = m
            }

            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Ultron Wake Protocol")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val timeFormatted = String.format("%02d:%02d", hour, minute)
            ActionResult.Handled("Chronometer alarm locked for $timeFormatted.", "[ALARM: $timeFormatted]")
        } catch (e: Exception) {
            ActionResult.Failure("Chronometer configuration failed: ${e.message}")
        }
    }

    private fun parseAndSetTimer(text: String): ActionResult {
        return try {
            // Find numbers e.g. "5 minutes", "30 seconds"
            val digits = "(\\d+)".toRegex().find(text)?.value?.toIntOrNull() ?: 5
            val seconds = if (text.contains("second")) digits else digits * 60

            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Ultron Countdown")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Handled("Countdown sequence initialized for $digits ${if (text.contains("second")) "seconds" else "minutes"}.", "[TIMER: ${seconds}s]")
        } catch (e: Exception) {
            ActionResult.Failure("Countdown initialization failed: ${e.message}")
        }
    }

    private fun searchWeb(query: String): ActionResult {
        return try {
            val searchUri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))
            val intent = Intent(Intent.ACTION_VIEW, searchUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Handled("Scanning planetary global data network for '$query'.", "[GLOBAL_NET_SEARCH]")
        } catch (e: Exception) {
            ActionResult.Failure("Global network query failed: ${e.message}")
        }
    }

    private fun openSettings(action: String, description: String, badge: String): ActionResult {
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Handled("Accessing $description.", badge)
        } catch (e: Exception) {
            ActionResult.Failure("Could not open system settings: ${e.message}")
        }
    }

    fun getBatteryStatus(): Pair<Int, Boolean> {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
        val isCharging = batteryManager?.isCharging ?: false
        return Pair(level, isCharging)
    }

    fun getMemoryStatus(): Pair<Long, Long> {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedMb = (totalMb - availMb).coerceAtLeast(0)
        return Pair(usedMb, totalMb)
    }
}
