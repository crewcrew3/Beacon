package ru.itis.feature.emergency.impl.ui.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.itis.core.ui.R
import ru.itis.core.utils.ExceptionHandler
import ru.itis.core.utils.OperationResult
import ru.itis.feature.emergency.impl.domain.usecase.SendSosMessageUseCase
import ru.itis.feature.emergency.impl.domain.usecase.StartAlarmUseCase
import ru.itis.feature.emergency.impl.domain.usecase.StartFakeCallUseCase
import ru.itis.feature.emergency.impl.domain.usecase.StartIncomingCallUseCase
import ru.itis.feature.emergency.impl.domain.usecase.StopAlarmUseCase
import ru.itis.feature.emergency.impl.domain.usecase.StopFakeCallUseCase
import ru.itis.feature.emergency.impl.ui.emergency.mvi.EmergencyScreenEffect
import ru.itis.feature.emergency.impl.ui.emergency.mvi.EmergencyScreenEvent
import ru.itis.feature.emergency.impl.ui.emergency.mvi.EmergencyScreenState
import ru.itis.navigation.api.EmergencyNavigator
import javax.inject.Inject

@HiltViewModel
internal class EmergencyViewModel @Inject constructor(
    private val startAlarmUseCase: StartAlarmUseCase,
    private val stopAlarmUseCase: StopAlarmUseCase,
    private val startFakeCallUseCase: StartFakeCallUseCase,
    private val stopFakeCallUseCase: StopFakeCallUseCase,
    private val startIncomingCallUseCase: StartIncomingCallUseCase,
    private val sendSosMessageUseCase: SendSosMessageUseCase,
    private val emergencyNavigator: EmergencyNavigator,
    private val exceptionHandler: ExceptionHandler,
) : ViewModel() {

    private val _pageState = MutableStateFlow<EmergencyScreenState>(EmergencyScreenState.Initial)
    val pageState = _pageState.asStateFlow()

    private val _pageEffect = MutableSharedFlow<EmergencyScreenEffect>()
    val pageEffect = _pageEffect.asSharedFlow()

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

            is EmergencyScreenEvent.OnNavigateBack -> emergencyNavigator.back()
            is EmergencyScreenEvent.OnSosLongPress -> sendSos()
            is EmergencyScreenEvent.OnSosSettingsClick -> emergencyNavigator.toSosSettingsScreen()
        }
    }

    private fun sendSos() {
        viewModelScope.launch {
            when (val result = sendSosMessageUseCase()) {
                is OperationResult.Success -> {
                    _pageEffect.emit(EmergencyScreenEffect.Message(R.string.emergency_sos_sent))
                }
                is OperationResult.Error -> {
                    val messageResId = exceptionHandler.getErrorMessage(result.errorType)
                    _pageEffect.emit(EmergencyScreenEffect.Message(messageResId))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAlarmUseCase()
        stopFakeCallUseCase()
    }
}