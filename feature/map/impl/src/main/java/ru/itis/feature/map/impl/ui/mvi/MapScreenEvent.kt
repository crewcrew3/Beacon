package ru.itis.feature.map.impl.ui.mvi

import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.core.domain.model.mark.VerificationActionType

internal sealed interface MapScreenEvent {

    /** Пользователь перешел на экран профиля */
    data object OnProfileBottomBarClick : MapScreenEvent

    /** Запрос обновления инцидентов при изменении области карты */
    data class OnMapBoundsChanged(val bounds: DoubleArray) : MapScreenEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnMapBoundsChanged

            return bounds.contentEquals(other.bounds)
        }

        override fun hashCode(): Int {
            return bounds.contentHashCode()
        }
    }

    /** Пользователь добавил новую метку */
    data class OnAddIncident(
        val latitude: Double,
        val longitude: Double,
        val type: IncidentType,
        val description: String?
    ) : MapScreenEvent

    /** Пользователь проголосовал за инцидент */
    data class OnVerifyIncident(
        val incidentId: Long,
        val action: VerificationActionType
    ) : MapScreenEvent

    /** Переключение режима редактирования карты */
    data object OnToggleEditMode : MapScreenEvent

    /** Тап по карте (только в режиме редактирования) */
    data class OnMapTapped(val latitude: Double, val longitude: Double) : MapScreenEvent

    /** Клик по метке инцидента */
    data class OnIncidentClicked(val incident: IncidentModel) : MapScreenEvent

    /** Закрытие диалога с деталями инцидента */
    data object OnCloseIncidentDialog : MapScreenEvent
}