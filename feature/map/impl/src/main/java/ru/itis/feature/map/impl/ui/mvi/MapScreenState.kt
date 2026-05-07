package ru.itis.feature.map.impl.ui.mvi

import ru.itis.core.domain.model.mark.IncidentModel

internal sealed interface MapScreenState {
    data object Initial : MapScreenState
    data object Loading : MapScreenState
    data class IncidentsLoaded(val incidents: List<IncidentModel>) : MapScreenState
}