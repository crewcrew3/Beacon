package ru.itis.core.domain.model.mark

/**
 * Тип инцидента для категоризации меток.
 * Каждый тип может иметь свою иконку и вес риска при построении маршрута.
 */
enum class IncidentType(
    /** Коэффициент влияния на риск маршрута (0.0 - 1.0) */
    val riskWeight: Float
) {
    POOR_LIGHTING(0.3f),      // Плохое освещение
    HARASSMENT(0.9f),         // Домогательства
    PICKPOCKETING(0.7f),      // Карманная кража
    ROBBERY(1.0f),            // Ограбление
    SUSPICIOUS_PERSON(0.5f),  // Подозрительный человек
    BAD_ROAD(0.2f),           // Плохая проходимость
    OTHER(0.4f)               // Другое
}