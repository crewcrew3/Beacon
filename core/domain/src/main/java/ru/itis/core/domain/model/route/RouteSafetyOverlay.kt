package ru.itis.core.domain.model.route

import ru.itis.core.domain.model.safety.LightingPoleModel
import ru.itis.core.domain.model.safety.SafetyCameraModel
import ru.itis.core.domain.model.safety.SafetyPlaceModel

/**
 * Контейнер для всех объектов безопасности, отображаемых вдоль маршрута.
 */
data class RouteSafetyOverlay(
    val safetyPlaces: List<SafetyPlaceModel>,    // из Яндекс API (без кэша!)
    val safetyCameras: List<SafetyCameraModel>,  // из локальной БД
    val lightingPoles: List<LightingPoleModel>   // из локальной БД
)