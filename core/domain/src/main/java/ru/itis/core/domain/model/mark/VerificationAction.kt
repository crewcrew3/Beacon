package ru.itis.core.domain.model.mark

import java.time.LocalDateTime

/**
 * Действие верификации от пользователя.
 * Используется для накопления статистики по метке.
 */
data class VerificationAction(
    val incidentId: Long,
    val userId: String,
    val action: VerificationActionType,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class VerificationActionType {
    CONFIRM,  // Подтверждение инцидента
    DISPUTE   // Оспаривание инцидента
}