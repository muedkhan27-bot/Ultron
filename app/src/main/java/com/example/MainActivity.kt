package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.UltronWakeService
import com.example.ui.screens.UltronMainScreen
import com.example.ui.theme.UltronTheme
import com.example.viewmodel.UltronViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: UltronViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (audioGranted && viewModel.telemetry.value.isWakeWordActive) {
            viewModel.voiceManager.startWakeWordListening()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestRequiredPermissions()
        handleIncomingIntent(intent)

        setContent {
            val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
            UltronTheme(hudTheme = telemetry.activeTheme) {
                UltronMainScreen(
                    viewModel = viewModel,
                    onRequestPermissions = { requestRequiredPermissions() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(UltronWakeService.EXTRA_WAKE_TRIGGERED, false) == true) {
            viewModel.handleWakeWordTriggered()
        }
        val voiceCommand = intent?.getStringExtra("EXTRA_VOICE_COMMAND")
        if (!voiceCommand.isNullOrBlank()) {
            viewModel.processUserPrompt(voiceCommand)
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.telemetry.value.isWakeWordActive &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.voiceManager.startWakeWordListening()
        }
    }
}
