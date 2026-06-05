package ru.itis.feature.emergency.impl.domain.manager

// Управление сиреной, вспышкой и вибрацией
interface AlarmManager {
    fun startAlarm()
    fun stopAlarm()
}