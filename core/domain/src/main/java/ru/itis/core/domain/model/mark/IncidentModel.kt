package ru.itis.core.domain.model.mark

import java.time.LocalDateTime

/**
 * Основная сущность инцидента на карте.
 * Используется в UI, репозиториях и алгоритме маршрутизации.
 */
data class IncidentModel(
    val id: Long? = null,                    // ID в БД (null для новых меток)
    val creatorId: Long,                   // ID пользователя, создавшего метку
    val creatorNickname: String,
    val latitude: Double,                    // Широта
    val longitude: Double,                   // Долгота
    val type: IncidentType,                  // Тип инцидента
    val description: String?,                // Описание (опционально)
    val createdAt: LocalDateTime,            // Время создания
    val status: IncidentStatus = IncidentStatus.PENDING_VERIFICATION,
    val confirmCount: Long = 0,               // Количество подтверждений
    val disputeCount: Long = 0                // Количество оспариваний
) {
    // Удобный геттер для координат Yandex MapKit
    //fun toPoint() = com.yandex.mapkit.geometry.Point(latitude, longitude)

    /** Проверка, отображается ли метка пользователю */
    fun isVisible() = status == IncidentStatus.VERIFIED
}