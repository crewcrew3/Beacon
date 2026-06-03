package ru.itis.core.domain.model.route

import com.yandex.mapkit.geometry.Point

/**
 * Результат построения безопасного маршрута.
 * @param polyline последовательность координат для отрисовки на карте
 * @param totalDistance общая длина в метрах
 * @param estimatedTime ориентировочное время в минутах
 * @param riskScore агрегированная оценка риска (0.0 - 1.0, где 1.0 = максимально опасно)
 */
data class SafeRouteModel(
    val polyline: List<Point>,
    val totalDistance: Double,
    val estimatedTime: Int,
    val riskScore: Float
)