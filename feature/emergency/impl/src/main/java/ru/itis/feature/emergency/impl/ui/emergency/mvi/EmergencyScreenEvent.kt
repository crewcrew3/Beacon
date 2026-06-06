package ru.itis.feature.emergency.impl.ui.emergency.mvi

internal sealed interface EmergencyScreenEvent {
    data object OnNavigateBack : EmergencyScreenEvent

    // Клики по инструментам на главном экране
    data object OnFakeCallClick : EmergencyScreenEvent
    data object OnAlarmClick : EmergencyScreenEvent
    data object OnSosLongPress : EmergencyScreenEvent
    data object OnSosSettingsClick : EmergencyScreenEvent

    // События для фейкового звонка
    data object OnAnswerFakeCall : EmergencyScreenEvent
    data object OnDeclineFakeCall : EmergencyScreenEvent
    data object OnEndFakeCall : EmergencyScreenEvent

    // Событие для остановки сирены
    data object OnStopAlarm : EmergencyScreenEvent
}