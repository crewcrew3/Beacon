package ru.itis.feature.map.impl.ui.mvi

import androidx.annotation.StringRes
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.IncidentType

internal sealed interface MapScreenEffect {

    /** Вывод сообщения для пользователя в Toast */
    data class Message(@StringRes val message: Int) : MapScreenEffect

    /** Показать диалог добавления инцидента по координатам */
    data class ShowAddIncidentDialog(val latitude: Double, val longitude: Double) : MapScreenEffect

    /** Добавить одну новую метку на карту без полной перерисовки */
    data class IncidentAdded(val incident: IncidentModel) : MapScreenEffect

    /** Обновить статус существующей метки */
    data class IncidentStatusUpdated(val incidentId: Long, val incidentType: IncidentType, val newStatus: IncidentStatus) : MapScreenEffect
}