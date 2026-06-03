package ru.itis.feature.map.impl.ui.mvi

import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.route.RouteRequestModel
import ru.itis.core.domain.model.route.SafeRouteResult

internal sealed interface MapScreenState {
    data object Initial : MapScreenState
    data object Loading : MapScreenState
    data class IncidentsLoaded(val incidents: List<IncidentModel>) : MapScreenState
    data class RouteMode(
        val startPoint: RouteRequestModel.PointData? = null,
        val endPoint: RouteRequestModel.PointData? = null,
        val routeResult: SafeRouteResult? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MapScreenState
}