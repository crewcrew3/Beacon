package ru.itis.core.domain.model.safety

/**
 * Типы "островков безопасности" — места, где пользователь может почувствовать себя безопаснее.
 */
enum class SafetyPlaceType {
    POLICE,      // Отделение полиции
    PHARMACY,    // Аптека
    ATM,         // Банкомат
    SHOP,        // Магазин 24/7
    METRO,       // Станция метро
    PARK         // Парк/сквер
}