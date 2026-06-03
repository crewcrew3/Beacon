package ru.itis.core.domain.model.route

/**
 * Запрос на построение безопасного маршрута.
 * @param startPoint координаты начальной точки
 * @param endPoint координаты конечной точки
 */
data class RouteRequestModel(
    val startPoint: PointData,
    val endPoint: PointData
) {
    data class PointData(
        val latitude: Double,
        val longitude: Double,
        val address: String? = null // опционально, для отображения в UI
    )
}