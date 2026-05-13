package ru.itis.feature.map.impl.ui.utils

import ru.itis.core.domain.model.mark.IncidentModel

/**
 * Интерфейс для связи MapScreenFragment с ViewModel из Compose.
 * Fragment вызывает эти методы при пользовательских действиях.
 */
interface MapScreenDelegate {
    fun onMapTapped(latitude: Double, longitude: Double)
    fun onIncidentClicked(incident: IncidentModel)
    fun onMapBoundsChanged(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double)
}