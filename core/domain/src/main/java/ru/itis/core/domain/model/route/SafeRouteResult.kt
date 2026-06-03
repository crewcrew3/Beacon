package ru.itis.core.domain.model.route

/**
 * Полный результат построения безопасного маршрута.
 * Содержит как сам маршрут, так и данные для визуализации.
 */
data class SafeRouteResult(
    val route: SafeRouteModel,                    // маршрут для отрисовки линии
    val safetyOverlay: RouteSafetyOverlay? = null // иконки вдоль маршрута (может быть null)
)