package ru.itis.core.domain.repository

import ru.itis.core.domain.model.safety.SafetyCameraModel
import ru.itis.core.utils.OperationResult

/**
 * Репозиторий для получения данных о камерах видеонаблюдения из локальной БД.
 */
interface SafetyCameraRepository {
    /**
     * Получение камер в прямоугольной области (bounding box).
     * Используется для фильтрации камер, близких к маршруту.
     */
    suspend fun getCamerasInBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): OperationResult<List<SafetyCameraModel>>
}