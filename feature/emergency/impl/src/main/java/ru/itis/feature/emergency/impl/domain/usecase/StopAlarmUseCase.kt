package ru.itis.feature.emergency.impl.domain.usecase

import ru.itis.feature.emergency.impl.domain.manager.AlarmManager
import javax.inject.Inject

internal class StopAlarmUseCase @Inject constructor(private val alarmManager: AlarmManager) {
    operator fun invoke() = alarmManager.stopAlarm()
}