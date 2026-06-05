package ru.itis.feature.emergency.impl.ui.mvi

internal sealed interface EmergencyScreenState {
    data object Initial : EmergencyScreenState //начальный
    data object FakeCallIncoming : EmergencyScreenState //входящий звонок
    data object FakeCallActive : EmergencyScreenState // активный звонок
    data object AlarmActive : EmergencyScreenState //активная тревога
}