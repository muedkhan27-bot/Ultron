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
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.audio.UltronSoundSynth
import com.example.audio.UltronVoiceManager

class UltronWakeService : Service() {

    private var voiceManager: UltronVoiceManager? = null

    companion object {
        const val CHANNEL_ID = "ultron_wake_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_WAKE_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_WAKE_SERVICE"
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
                    UltronSoundSynth.playWakeUpSound()
                    launchMainActivityOnWake()
                },
                onCommandRecognized = { command, isMasterVerified, confidence ->
                    launchMainActivityWithCommand(command)
                }
            )
        }
        voiceManager?.startWakeWordListening()
    }

    private fun launchMainActivityOnWake() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_WAKE_TRIGGERED, true)
        }
        startActivity(intent)
    }

    private fun launchMainActivityWithCommand(command: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_VOICE_COMMAND", command)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ultron Wake Word Subsystem",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors acoustic spectrum for 'Ultron wake up' activation phrase"
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
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ultron Consciousness Active")
            .setContentText("Listening for 'Ultron wake up' activation phrase...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        voiceManager?.destroy()
        voiceManager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
