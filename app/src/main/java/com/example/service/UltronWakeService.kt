package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.audio.UltronSoundSynth
import com.example.audio.UltronVoiceManager

class UltronWakeService : Service() {

    private var voiceManager: UltronVoiceManager? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "ultron_wake_channel_v2"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_WAKE_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_WAKE_SERVICE"
        const val ACTION_TRIGGER_VOICE = "ACTION_TRIGGER_VOICE"
        const val EXTRA_WAKE_TRIGGERED = "EXTRA_WAKE_TRIGGERED"

        fun startService(context: Context) {
            val intent = Intent(context, UltronWakeService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, UltronWakeService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TRIGGER_VOICE -> {
                launchMainActivityOnWake()
            }
            ACTION_START, null -> {
                val notification = createNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                startListening()
            }
        }
        return START_STICKY
    }

    private fun startListening() {
        if (voiceManager == null) {
            voiceManager = UltronVoiceManager(
                context = this,
                onWakeWordDetected = { isMasterVerified, confidence ->
                    wakeDeviceAndAlert()
                    UltronSoundSynth.playWakeUpSound()
                    launchMainActivityOnWake()
                },
                onCommandRecognized = { command, isMasterVerified, confidence ->
                    wakeDeviceAndAlert()
                    UltronSoundSynth.playActionSound()
                    launchMainActivityWithCommand(command)
                }
            )
        }
        voiceManager?.startWakeWordListening()
    }

    private fun wakeDeviceAndAlert() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "ultron:acoustic_wake_lock"
            ).apply {
                acquire(5000)
            }
        } catch (_: Exception) {}
    }

    private fun launchMainActivityOnWake() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra(EXTRA_WAKE_TRIGGERED, true)
        }

        // Show immediate high-priority wake pop-up notification with Fullscreen intent
        val pendingIntent = PendingIntent.getActivity(
            this,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val wakeNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ULTRON AWAKENED")
            .setContentText("Acoustic voiceprint authenticated. Ready for your command.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID + 1, wakeNotification)

        try {
            startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun launchMainActivityWithCommand(command: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("EXTRA_VOICE_COMMAND", command)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            2002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val commandNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ultron Executing Voice Directive")
            .setContentText("\"$command\"")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID + 2, commandNotification)

        try {
            startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ultron Acoustic Consciousness",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Monitors acoustic spectrum for 'Ultron wake up' activation phrase from home screen and background."
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_WAKE_TRIGGERED, true)
        }
        val triggerPending = PendingIntent.getActivity(
            this,
            1,
            triggerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ultron Acoustic Matrix Active")
            .setContentText("Say 'Ultron wake up' or tap to speak from home screen")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_btn_speak_now, "TALK TO ULTRON", triggerPending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    override fun onDestroy() {
        voiceManager?.destroy()
        voiceManager = null
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
