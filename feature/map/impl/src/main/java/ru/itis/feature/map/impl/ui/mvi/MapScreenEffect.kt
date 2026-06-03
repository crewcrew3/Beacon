package ru.itis.feature.map.impl.ui.mvi

import androidx.annotation.StringRes
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.core.domain.model.route.SafeRouteResult

internal sealed interface MapScreenEffect {

    /** Вывод сообщения для пользователя в Toast */
    data class Message(@StringRes val message: Int) : MapScreenEffect

    /** Показать диалог добавления инцидента по координатам */
    data class ShowAddIncidentDialog(val latitude: Double, val longitude: Double) : MapScreenEffect

    /** Добавить одну новую метку на карту без полной перерисовки */
    data class IncidentAdded(val incident: IncidentModel) : MapScreenEffect

    /** Обновить статус существующей метки */
    data class IncidentStatusUpdated(val incidentId: Long, val incidentType: IncidentType, val newStatus: IncidentStatus) : MapScreenEffect

    /** Маршрут успешно построен */
    data class RouteBuilt(val result: SafeRouteResult) : MapScreenEffect

    /** Режим маршрута завершён — очистить карту */
    data object RouteFinished : MapScreenEffect

    /**
     * Добавить маркер начальной/конечной точки на карту.
     * Отображается только если точка выбрана тапом.
     */
    data class RoutePointMarkerAdded(
        val latitude: Double,
        val longitude: Double,
        val isStart: Boolean  // true = начальная, false = конечная
    ) : MapScreenEffect
}