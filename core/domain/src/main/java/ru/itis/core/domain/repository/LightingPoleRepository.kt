package ru.itis.core.domain.repository

import ru.itis.core.domain.model.safety.LightingPoleModel
import ru.itis.core.utils.OperationResult

/**
 * Репозиторий для получения данных об опорах освещения из локальной БД.
 */
interface LightingPoleRepository {
    /**
     * Получение опор освещения в прямоугольной области.
     */
    suspend fun getLightingPolesInBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): OperationResult<List<LightingPoleModel>>
}