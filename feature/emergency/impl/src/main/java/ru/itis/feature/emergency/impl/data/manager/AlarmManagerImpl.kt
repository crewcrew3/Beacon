package ru.itis.feature.emergency.impl.data.manager

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.itis.feature.emergency.impl.domain.manager.AlarmManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmManager {

    private var mediaPlayer: MediaPlayer? = null
    // Отдельный скоуп для мигания вспышкой, чтобы не зависеть от lifecycle
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var flashJob: Job? = null
    private var isFlashOn = false

    override fun startAlarm() {
        // 1. Берем звук
        val resId = context.resources.getIdentifier("alarm_sound", "raw", context.packageName)
        if (resId != 0) {
            mediaPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true // Зацикливаем, чтобы сирена не прекращалась
                start()
            }
        }

        // 2. Вспышка: мигаем камерой
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]

        flashJob = scope.launch {
            while (true) {
                try {
                    cameraManager.setTorchMode(cameraId, !isFlashOn)
                    isFlashOn = !isFlashOn
                    delay(200) // Интервал мигания
                } catch (e: Exception) {
                    break // Если камера недоступна, выходим из цикла
                }
            }
        }

        // 3. Вибрация: непрерывная вибрация
        val vibrator = getVibrator()
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
    }

    override fun stopAlarm() {
        // Останавливаем звук
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        // Останавливаем вспышку
        flashJob?.cancel()
        flashJob = null
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: Exception) { /* ignore */ }

        // Останавливаем вибрацию
        getVibrator().cancel()
    }

    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}