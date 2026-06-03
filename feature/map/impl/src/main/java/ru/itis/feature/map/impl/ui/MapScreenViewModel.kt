package ru.itis.feature.map.impl.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.core.domain.model.mark.VerificationActionType
import ru.itis.core.domain.model.route.RouteRequestModel
import ru.itis.core.ui.R
import ru.itis.core.utils.ExceptionHandler
import ru.itis.core.utils.OperationResult
import ru.itis.feature.map.impl.domain.usecase.AddIncidentUseCase
import ru.itis.feature.map.impl.domain.usecase.BuildSafeRouteUseCase
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
    private val buildSafeRouteUseCase: BuildSafeRouteUseCase,
    private val exceptionHandler: ExceptionHandler,
) : ViewModel() {

    private val _pageState = MutableStateFlow<MapScreenState>(value = MapScreenState.Initial)
    val pageState = _pageState.asStateFlow()

    private val _pageEffect = MutableSharedFlow<MapScreenEffect>()
    val pageEffect = _pageEffect.asSharedFlow()

    /**
     * Флаг режима редактирования.
     * true — пользователь может добавлять метки тапом по карте.
     * false — карта в режиме просмотра, тапы игнорируются.
     */
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    /**
     * Текущий выбранный инцидент для отображения в диалоге деталей.
     * null — диалог закрыт.
     */
    private val _selectedIncident = MutableStateFlow<IncidentModel?>(null)
    val selectedIncident = _selectedIncident.asStateFlow()

    /** Флаг: активен ли режим построения маршрута */
    private val _isRouteMode = MutableStateFlow(false)
    val isRouteMode = _isRouteMode.asStateFlow()

    fun processEvent(event: MapScreenEvent) {
        when (event) {
            is MapScreenEvent.OnProfileBottomBarClick -> {
                bottomBarNavigator.toProfileScreen()
            }
            is MapScreenEvent.OnMapBoundsChanged -> {
                Log.i("RENDER_INCIDENT_DEBUG", "Map bounds changed")
                loadIncidentsInBounds(event.bounds)
            }
            is MapScreenEvent.OnAddIncident -> {
                Log.i("ADD_INCIDENT_DEBUG", "ViewModel event add incident worked")
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
            is MapScreenEvent.OnToggleEditMode -> {
                _isEditMode.value = !_isEditMode.value
                Log.i("ADD_INCIDENT_DEBUG", "Edit Mode: ${_isEditMode.value}")
            }
            is MapScreenEvent.OnMapTapped -> {
                Log.i("ADD_INCIDENT_DEBUG", "ViewModel event show add incident dialog worked. Edit Mode: ${_isEditMode.value}")
                // Реагируем на тап по карте только в режиме редактирования
                if (_isEditMode.value) {
                    Log.i("ADD_INCIDENT_DEBUG", "Edit mode is ON")
                    viewModelScope.launch {
                        _pageEffect.emit(
                            MapScreenEffect.ShowAddIncidentDialog(
                                event.latitude,
                                event.longitude
                            )
                        )
                    }
                }
            }
            is MapScreenEvent.OnIncidentClicked -> {
                Log.i("TAP_INCIDENT_DEBUG", "ViewModel received OnIncidentClicked: id=${event.incident.id}")
                _selectedIncident.value = event.incident
            }
            is MapScreenEvent.OnCloseIncidentDialog -> {
                _selectedIncident.value = null
            }
            is MapScreenEvent.OnEnterRouteMode -> {
                _isRouteMode.value = true
                _pageState.value = MapScreenState.RouteMode()
                Log.i("BUILD_ROUTE", "Route mode is ${_isRouteMode.value}. Changed state to ${_pageState.value}")
            }

            is MapScreenEvent.OnRouteStartSelected -> {
                val newPoint = RouteRequestModel.PointData(
                    latitude = event.latitude,
                    longitude = event.longitude,
                    address = event.address
                )
                _pageState.value = (_pageState.value as? MapScreenState.RouteMode)
                    ?.copy(startPoint = newPoint)
                    ?: return
                Log.i("BUILD_ROUTE", "Start point event. Start point is " +
                        "${(_pageState.value as? MapScreenState.RouteMode)?.startPoint}")

                //отрисовать иконку точки старта
                viewModelScope.launch {
                    _pageEffect.emit(
                        MapScreenEffect.RoutePointMarkerAdded(
                            latitude = event.latitude,
                            longitude = event.longitude,
                            isStart = true
                        )
                    )
                }
            }

            is MapScreenEvent.OnRouteEndSelected -> {
                val newPoint = RouteRequestModel.PointData(
                    latitude = event.latitude,
                    longitude = event.longitude,
                    address = event.address
                )
                _pageState.value = (_pageState.value as? MapScreenState.RouteMode)
                    ?.copy(endPoint = newPoint)
                    ?: return
                Log.i("BUILD_ROUTE", "End point event. End point is " +
                        "${(_pageState.value as? MapScreenState.RouteMode)?.endPoint}")

                viewModelScope.launch {
                    _pageEffect.emit(
                        MapScreenEffect.RoutePointMarkerAdded(
                            latitude = event.latitude,
                            longitude = event.longitude,
                            isStart = false
                        )
                    )
                }
            }

            is MapScreenEvent.OnBuildRouteClicked -> {
                Log.i("BUILD_ROUTE", "On BuildRouteClicked event")
                val (start, end) = getCurrentRoutePoints()
                if (start != null && end != null) {
                    Log.i("BUILD_ROUTE", "Start ($start) and end ($end) points -> building route...")
                    buildRoute()
                } else {
                    viewModelScope.launch {
                        _pageEffect.emit(
                            MapScreenEffect.Message(R.string.route_error_points_not_selected)
                        )
                    }
                }
            }

            is MapScreenEvent.OnFinishRouteClicked -> {
                _isRouteMode.value = false
                _pageState.value = MapScreenState.RouteMode()
                viewModelScope.launch {
                    _pageEffect.emit(MapScreenEffect.RouteFinished)
                    // Возвращаемся к загрузке инцидентов
                    loadIncidentsInBounds(doubleArrayOf(55.5, 56.0, 37.3, 38.0)) // дефолтные границы
                }
            }

            is MapScreenEvent.OnAddressGeocoded -> {
                val newPoint = RouteRequestModel.PointData(
                    latitude = event.latitude,
                    longitude = event.longitude,
                    address = event.address
                )
                val current = (_pageState.value as? MapScreenState.RouteMode) ?: return

                _pageState.value = if (event.isStartPoint) current.copy(startPoint = newPoint)
                else current.copy(endPoint = newPoint)

                viewModelScope.launch {
                    _pageEffect.emit(
                        MapScreenEffect.RoutePointMarkerAdded(
                            latitude = event.latitude,
                            longitude = event.longitude,
                            isStart = event.isStartPoint
                        )
                    )
                }

                val (start, end) = getCurrentRoutePoints()
                if (start != null && end != null) buildRoute()
            }
        }
    }

    /**
     * Загружает инциденты в заданной географической области.
     * Вызывается при изменении позиции камеры карты.
     */
    private fun loadIncidentsInBounds(bounds: DoubleArray) {
        Log.i("RENDER_INCIDENT_DEBUG", "ViewModel: loadIncidentsInBounds called with bounds: ${bounds.contentToString()}")
        viewModelScope.launch {
            _pageState.value = MapScreenState.Loading

            when (val result = getVisibleIncidentsUseCase(bounds)) {
                is OperationResult.Success -> {
                    Log.i("RENDER_INCIDENT_DEBUG", "ViewModel: Success! Loaded ${result.data.size} incidents")
                    _pageState.value = MapScreenState.IncidentsLoaded(result.data)
                }
                is OperationResult.Error -> {
                    Log.e("RENDER_INCIDENT_DEBUG", "ViewModel: Error loading incidents: ${result.errorType}")
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

                    val newIncident = result.data

                    // Добавляем новый инцидент в текущий список
                    val currentState = _pageState.value
                    if (currentState is MapScreenState.IncidentsLoaded) {
                        _pageState.value = MapScreenState.IncidentsLoaded(
                            incidents = currentState.incidents + newIncident
                        )
                        // Уведомляем рендерер о добавлении одной метки
                        _pageEffect.emit(MapScreenEffect.IncidentAdded(newIncident))
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
        action: VerificationActionType
    ) {
        viewModelScope.launch {
            when (val result = verifyIncidentUseCase(incidentId, action)) {
                is OperationResult.Success -> {
                    _pageEffect.emit(MapScreenEffect.Message(R.string.toast_msg_incident_voited))

                    val updatedIncidentFromDb = result.data
                    val incidentType = updatedIncidentFromDb.type
                    val actualStatus = updatedIncidentFromDb.status

                    // Находим инцидент в текущем списке и обновляем
                    val currentState = _pageState.value
                    if (currentState is MapScreenState.IncidentsLoaded) {
                        val updatedIncidents = currentState.incidents.map { incident ->
                            if (incident.id == incidentId) updatedIncidentFromDb else incident
                        }
                        _pageState.value = MapScreenState.IncidentsLoaded(updatedIncidents)

                        // Обновляем selectedIncident для диалога
                        if (_selectedIncident.value?.id == incidentId) {
                            _selectedIncident.value = updatedIncidentFromDb.copy()
                        }

                        // Уведомляем рендерер об обновлении статуса
                        _pageEffect.emit(
                            MapScreenEffect.IncidentStatusUpdated(
                                incidentId = incidentId,
                                incidentType = incidentType,
                                newStatus = actualStatus
                            )
                        )
                    }
                }
                is OperationResult.Error -> {
                    val messageResId = exceptionHandler.getErrorMessage(result.errorType)
                    _pageEffect.emit(MapScreenEffect.Message(messageResId))
                }
            }
        }
    }

    /**
     * Основной метод построения безопасного маршрута.
     * Вызывается когда выбраны обе точки.
     */
    private fun buildRoute() {
        val (start, end) = getCurrentRoutePoints()
        if (start == null || end == null) return

        viewModelScope.launch {
            _pageState.value = MapScreenState.RouteMode(isLoading = true)

            val request = RouteRequestModel(startPoint = start, endPoint = end)

            when (val result = buildSafeRouteUseCase(request)) {
                is OperationResult.Success -> {
                    // Успех: обновляем состояние и отправляем эффект для отрисовки
                    _pageState.value = MapScreenState.RouteMode(
                        startPoint = start,
                        endPoint = end,
                        routeResult = result.data,
                        isLoading = false
                    )
                    _pageEffect.emit(MapScreenEffect.RouteBuilt(result.data))
                    _pageEffect.emit(
                        MapScreenEffect.Message(R.string.toast_msg_route_built)
                    )
                }
                is OperationResult.Error -> {
                    // Ошибка
                    _pageState.value = MapScreenState.RouteMode(
                        startPoint = start,
                        endPoint = end,
                        isLoading = false,
                        error = exceptionHandler.getErrorMessage(result.errorType).toString()
                    )
                    val messageResId = exceptionHandler.getErrorMessage(result.errorType)
                    _pageEffect.emit(MapScreenEffect.Message(messageResId))
                }
            }
        }
    }

    /**
     * Вспомогательный метод: получает текущие точки из состояния.
     * Возвращает null, если приложение не в режиме маршрута.
     */
    private fun getCurrentRoutePoints(): Pair<RouteRequestModel.PointData?, RouteRequestModel.PointData?> {
        val state = _pageState.value as? MapScreenState.RouteMode ?: return null to null
        return state.startPoint to state.endPoint
    }
}