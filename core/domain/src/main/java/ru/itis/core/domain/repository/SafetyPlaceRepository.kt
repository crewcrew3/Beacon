package ru.itis.core.domain.repository

import ru.itis.core.domain.model.safety.SafetyPlaceModel
import ru.itis.core.utils.OperationResult
import com.yandex.mapkit.geometry.Point

/**
 * Репозиторий для получения "островков безопасности" из Яндекс Карт.
 * ВАЖНО: данные не кешируются и не сохраняются
 */
interface SafetyPlaceRepository {
    /**
     * Поиск безопасных мест в радиусе от полилинии маршрута.
     * @param routePolyline геометрия маршрута для поиска "вдоль пути"
     * @param radius радиус поиска в метрах
     * @return список SafetyPlaceModel или ошибка
     */
    suspend fun getSafetyPlacesAlongRoute(
        routePolyline: List<Point>,
        radius: Float = 300f // 300 метров от маршрута
    ): OperationResult<List<SafetyPlaceModel>>
}