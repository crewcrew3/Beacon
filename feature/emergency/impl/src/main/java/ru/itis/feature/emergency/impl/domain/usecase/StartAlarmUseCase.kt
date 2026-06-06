package ru.itis.feature.emergency.impl.domain.usecase

import ru.itis.feature.emergency.impl.domain.manager.AlarmManager
import javax.inject.Inject

internal class StartAlarmUseCase @Inject constructor(private val alarmManager: AlarmManager) {
    operator fun invoke() = alarmManager.startAlarm()
}