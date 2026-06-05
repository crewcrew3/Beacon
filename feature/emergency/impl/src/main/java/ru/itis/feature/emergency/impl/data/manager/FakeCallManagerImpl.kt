package ru.itis.feature.emergency.impl.data.manager

import android.content.Context
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.itis.feature.emergency.impl.domain.manager.FakeCallManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeCallManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FakeCallManager {

    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun startIncomingCall() {
        // 1. Включаем стандартный рингтон звонка пользователя
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        ringtone?.play()

        // 2. Включаем вибрацию (паттерн: ждать 0мс, вибрировать 500мс, пауза 500мс, повтор)
        vibrator = getVibrator()
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
    }

    override fun startConversation() {
        // Останавливаем рингтон и вибрацию перед началом разговора
        ringtone?.stop()
        ringtone = null
        vibrator?.cancel()

        // Запускаем записанный разговор из res/raw
        val resId = context.resources.getIdentifier("fake_conversation", "raw", context.packageName)
        if (resId != 0) {
            mediaPlayer = MediaPlayer.create(context, resId).apply {
                start()
            }
        }
    }

    override fun stopAll() {
        ringtone?.stop()
        ringtone = null
        vibrator?.cancel()
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
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