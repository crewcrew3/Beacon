package ru.itis.feature.emergency.impl.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.itis.feature.emergency.impl.domain.usecase.StartAlarmUseCase
import ru.itis.feature.emergency.impl.domain.usecase.StartFakeCallUseCase
import ru.itis.feature.emergency.impl.domain.usecase.StartIncomingCallUseCase
import ru.itis.feature.emergency.impl.domain.usecase.StopAlarmUseCase
import ru.itis.feature.emergency.impl.domain.usecase.StopFakeCallUseCase
import ru.itis.feature.emergency.impl.ui.mvi.EmergencyScreenEvent
import ru.itis.feature.emergency.impl.ui.mvi.EmergencyScreenState
import javax.inject.Inject

@HiltViewModel
internal class EmergencyViewModel @Inject constructor(
    private val startAlarmUseCase: StartAlarmUseCase,
    private val stopAlarmUseCase: StopAlarmUseCase,
    private val startFakeCallUseCase: StartFakeCallUseCase,
    private val stopFakeCallUseCase: StopFakeCallUseCase,
    private val startIncomingCallUseCase: StartIncomingCallUseCase,
) : ViewModel() {

    private val _pageState = MutableStateFlow<EmergencyScreenState>(EmergencyScreenState.Initial)
    val pageState = _pageState.asStateFlow()

    fun processEvent(event: EmergencyScreenEvent) {
        when (event) {
            is EmergencyScreenEvent.OnFakeCallClick -> {
                startIncomingCallUseCase()
                _pageState.value = EmergencyScreenState.FakeCallIncoming
            }
            is EmergencyScreenEvent.OnAlarmClick -> {
                startAlarmUseCase()
                _pageState.value = EmergencyScreenState.AlarmActive
            }
            is EmergencyScreenEvent.OnAnswerFakeCall -> {
                startFakeCallUseCase()
                _pageState.value = EmergencyScreenState.FakeCallActive
            }
            is EmergencyScreenEvent.OnDeclineFakeCall -> {
                stopFakeCallUseCase()
                _pageState.value = EmergencyScreenState.Initial
            }
            is EmergencyScreenEvent.OnEndFakeCall -> {
                stopFakeCallUseCase()
                _pageState.value = EmergencyScreenState.Initial
            }
            is EmergencyScreenEvent.OnStopAlarm -> {
                stopAlarmUseCase()
                _pageState.value = EmergencyScreenState.Initial
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAlarmUseCase()
        stopFakeCallUseCase()
    }
}