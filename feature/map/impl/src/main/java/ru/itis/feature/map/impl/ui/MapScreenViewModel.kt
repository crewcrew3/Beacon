package ru.itis.feature.map.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.core.ui.R
import ru.itis.core.utils.ExceptionHandler
import ru.itis.core.utils.OperationResult
import ru.itis.feature.map.impl.domain.usecase.AddIncidentUseCase
import ru.itis.feature.map.impl.domain.usecase.GetVisibleIncidentsUseCase
import ru.itis.feature.map.impl.domain.usecase.VerifyIncidentUseCase
import ru.itis.feature.map.impl.ui.mvi.MapScreenEffect
import ru.itis.feature.map.impl.ui.mvi.MapScreenEvent
import ru.itis.feature.map.impl.ui.mvi.MapScreenState
import ru.itis.navigation.api.BottomBarNavigator
import javax.inject.Inject

@HiltViewModel
internal class MapScreenViewModel @Inject constructor(
    private val bottomBarNavigator: BottomBarNavigator,
    private val addIncidentUseCase: AddIncidentUseCase,
    private val getVisibleIncidentsUseCase: GetVisibleIncidentsUseCase,
    private val verifyIncidentUseCase: VerifyIncidentUseCase,
    private val exceptionHandler: ExceptionHandler,
) : ViewModel() {

    private val _pageState = MutableStateFlow<MapScreenState>(value = MapScreenState.Initial)
    val pageState = _pageState.asStateFlow()

    private val _pageEffect = MutableSharedFlow<MapScreenEffect>()
    val pageEffect = _pageEffect.asSharedFlow()

    fun processEvent(event: MapScreenEvent) {
        when (event) {
            is MapScreenEvent.OnProfileBottomBarClick -> bottomBarNavigator.toProfileScreen()
            is MapScreenEvent.OnMapBoundsChanged -> {
                loadIncidentsInBounds(event.bounds)
            }
            is MapScreenEvent.OnAddIncident -> {
                addNewIncident(
                    latitude = event.latitude,
                    longitude = event.longitude,
                    type = event.type,
                    description = event.description
                )
            }
            is MapScreenEvent.OnVerifyIncident -> {
                processVerification(
                    incidentId = event.incidentId,
                    action = event.action
                )
            }
        }
    }

    /**
     * Загружает инциденты в заданной географической области.
     * Вызывается при изменении позиции камеры карты.
     */
    private fun loadIncidentsInBounds(bounds: DoubleArray) {
        viewModelScope.launch {
            _pageState.value = MapScreenState.Loading

            when (val result = getVisibleIncidentsUseCase(bounds)) {
                is OperationResult.Success -> {
                    _pageState.value = MapScreenState.IncidentsLoaded(result.data)
                }
                is OperationResult.Error -> {
                    val messageResId = exceptionHandler.getErrorMessage(result.errorType)
                    _pageEffect.emit(MapScreenEffect.Message(messageResId))
                }
            }
        }
    }

    /** Добавляет новый инцидент по координатам. */
    private fun addNewIncident(
        latitude: Double,
        longitude: Double,
        type: IncidentType,
        description: String?
    ) {
        viewModelScope.launch {
            when (val result = addIncidentUseCase(latitude, longitude, type, description)) {
                is OperationResult.Success -> {
                    _pageEffect.emit(MapScreenEffect.Message(R.string.toast_msg_incident_added))
                    // Перезагружаем инциденты в текущей области, чтобы отобразить новую метку
                    val currentState = _pageState.value
                    if (currentState is MapScreenState.IncidentsLoaded) {
                        //TODO()
                        // Можно либо перезагрузить все инциденты, либо добавить новый в существующий список
                    }
                }
                is OperationResult.Error -> {
                    val messageResId = exceptionHandler.getErrorMessage(result.errorType)
                    _pageEffect.emit(MapScreenEffect.Message(messageResId))
                }
            }
        }
    }

    /** Обрабатывает голос пользователя за инцидент. */
    private fun processVerification(
        incidentId: Long,
        action: ru.itis.core.domain.model.mark.VerificationActionType
    ) {
        viewModelScope.launch {
            when (val result = verifyIncidentUseCase(incidentId, action)) {
                is OperationResult.Success -> {
                    _pageEffect.emit(MapScreenEffect.Message(R.string.toast_msg_incident_voited))

                    val currentState = _pageState.value
                    if (currentState is MapScreenState.IncidentsLoaded) {
                        //TODO("Возможно понадобиться перезагрузить состояние метки")
                    }
                }
                is OperationResult.Error -> {
                    val messageResId = exceptionHandler.getErrorMessage(result.errorType)
                    _pageEffect.emit(MapScreenEffect.Message(messageResId))
                }
            }
        }
    }
}