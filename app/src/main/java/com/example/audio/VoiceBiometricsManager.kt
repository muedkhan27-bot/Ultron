package com.example.audio

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class MasterVoiceProfile(
    val isEnrolled: Boolean = false,
    val creatorName: String = "Creator",
    val pitchHz: Float = 145f,
    val pitchVariance: Float = 25f,
    val averageRms: Float = 0.42f,
    val spectralCentroid: Float = 1850f,
    val voiceprintHash: String = "VPRINT-SYS-DEFAULT",
    val enrolledDate: String = "Unenrolled",
    val isBiometricLockEnabled: Boolean = false
)

data class BiometricVerificationResult(
    val isMatch: Boolean,
    val confidencePercent: Float,
    val message: String
)

class VoiceBiometricsManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ultron_voice_biometrics_prefs", Context.MODE_PRIVATE)

    private val _masterProfile = MutableStateFlow(loadProfile())
    val masterProfile: StateFlow<MasterVoiceProfile> = _masterProfile.asStateFlow()

    private val _lastVerificationResult = MutableStateFlow<BiometricVerificationResult?>(null)
    val lastVerificationResult: StateFlow<BiometricVerificationResult?> = _lastVerificationResult.asStateFlow()

    // Temporary storage during multi-step calibration
    private val calibrationRmsSamples = mutableListOf<Float>()
    private val calibrationPitches = mutableListOf<Float>()

    fun loadProfile(): MasterVoiceProfile {
        val isEnrolled = prefs.getBoolean("is_enrolled", false)
        if (!isEnrolled) {
            return MasterVoiceProfile(isEnrolled = false)
        }
        return MasterVoiceProfile(
            isEnrolled = true,
            creatorName = prefs.getString("creator_name", "Creator") ?: "Creator",
            pitchHz = prefs.getFloat("pitch_hz", 145f),
            pitchVariance = prefs.getFloat("pitch_variance", 25f),
            averageRms = prefs.getFloat("average_rms", 0.42f),
            spectralCentroid = prefs.getFloat("spectral_centroid", 1850f),
            voiceprintHash = prefs.getString("voiceprint_hash", "VPRINT-0x7F9B") ?: "VPRINT-0x7F9B",
            enrolledDate = prefs.getString("enrolled_date", "Active") ?: "Active",
            isBiometricLockEnabled = prefs.getBoolean("is_biometric_lock_enabled", false)
        )
    }

    fun saveProfile(profile: MasterVoiceProfile) {
        prefs.edit().apply {
            putBoolean("is_enrolled", profile.isEnrolled)
            putString("creator_name", profile.creatorName)
            putFloat("pitch_hz", profile.pitchHz)
            putFloat("pitch_variance", profile.pitchVariance)
            putFloat("average_rms", profile.averageRms)
            putFloat("spectral_centroid", profile.spectralCentroid)
            putString("voiceprint_hash", profile.voiceprintHash)
            putString("enrolled_date", profile.enrolledDate)
            putBoolean("is_biometric_lock_enabled", profile.isBiometricLockEnabled)
            apply()
        }
        _masterProfile.value = profile
    }

    fun setBiometricLock(enabled: Boolean) {
        val current = _masterProfile.value
        val updated = current.copy(isBiometricLockEnabled = enabled)
        saveProfile(updated)
    }

    fun clearCalibrationBuffer() {
        calibrationRmsSamples.clear()
        calibrationPitches.clear()
    }

    fun recordCalibrationSample(rmsList: List<Float>, estimatedPitch: Float) {
        if (rmsList.isNotEmpty()) {
            calibrationRmsSamples.addAll(rmsList)
        }
        if (estimatedPitch > 50f && estimatedPitch < 600f) {
            calibrationPitches.add(estimatedPitch)
        }
    }

    fun completeEnrollment(creatorName: String): MasterVoiceProfile {
        val avgRms = if (calibrationRmsSamples.isNotEmpty()) {
            calibrationRmsSamples.average().toFloat()
        } else {
            0.45f
        }

        val avgPitch = if (calibrationPitches.isNotEmpty()) {
            calibrationPitches.average().toFloat()
        } else {
            140f
        }

        val hashSuffix = (System.currentTimeMillis() % 0xFFFF).toString(16).uppercase()
        val formattedDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())

        val newProfile = MasterVoiceProfile(
            isEnrolled = true,
            creatorName = if (creatorName.isNotBlank()) creatorName.trim() else "Creator",
            pitchHz = avgPitch,
            pitchVariance = 55f, // Realistic tolerance window for natural pitch changes
            averageRms = avgRms,
            spectralCentroid = 1750f + (avgPitch * 2.2f),
            voiceprintHash = "VPRINT-0x$hashSuffix-ULTRON",
            enrolledDate = formattedDate,
            isBiometricLockEnabled = false // Default to unlocked so user voice always works immediately
        )

        saveProfile(newProfile)
        clearCalibrationBuffer()
        return newProfile
    }

    fun deleteProfile() {
        prefs.edit().clear().apply()
        _masterProfile.value = MasterVoiceProfile(isEnrolled = false, isBiometricLockEnabled = false)
        _lastVerificationResult.value = null
        clearCalibrationBuffer()
    }

    /**
     * Verifies speaker against enrolled Master Voiceprint.
     * Computes similarity between current acoustic RMS cadence & pitch frequency and the enrolled baseline.
     */
    fun verifySpeaker(rmsList: List<Float>, pitchEstimate: Float): BiometricVerificationResult {
        val profile = _masterProfile.value
        if (!profile.isEnrolled || !profile.isBiometricLockEnabled) {
            // When lock is disabled (default), accept speaker with 100% authorization
            val result = BiometricVerificationResult(
                isMatch = true,
                confidencePercent = 100f,
                message = if (profile.isEnrolled) "MASTER VOICE: ${profile.creatorName.uppercase()} (AUTHORIZED)" else "VOICE RECOGNIZED (OPEN RECOGNITION)"
            )
            _lastVerificationResult.value = result
            return result
        }

        // Calculate acoustic similarity
        val currentRmsAvg = if (rmsList.isNotEmpty()) rmsList.average().toFloat() else 0.40f
        val currentPitch = if (pitchEstimate > 50f && pitchEstimate < 600f) pitchEstimate else profile.pitchHz

        val pitchDiff = abs(currentPitch - profile.pitchHz)
        val pitchScore = max(10f, 100f - (pitchDiff / max(30f, profile.pitchVariance)) * 30f)

        val rmsDiff = abs(currentRmsAvg - profile.averageRms)
        val rmsScore = max(20f, 100f - (rmsDiff * 50f))

        val combinedConfidence = (pitchScore * 0.60f + rmsScore * 0.40f).coerceIn(40f, 99.8f)
        val isMatch = combinedConfidence >= 50f

        val result = BiometricVerificationResult(
            isMatch = isMatch,
            confidencePercent = combinedConfidence,
            message = if (isMatch) "MASTER VOICE MATCHED (${profile.creatorName.uppercase()})" else "UNAUTHORIZED VOICE (MISMATCH)"
        )
        _lastVerificationResult.value = result
        return result
    }
}
