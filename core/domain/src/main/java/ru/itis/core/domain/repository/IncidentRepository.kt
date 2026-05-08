package ru.itis.core.domain.repository

import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.VerificationActionType
import ru.itis.core.utils.OperationResult
import ru.itis.core.utils.properties.OtherProperties

/**
 * Репозиторий для работы с инцидентами.
 */
interface IncidentRepository {

    /** Добавление нового инцидента.
     * Возвращает сгенерированный ID новой записи.
     */
    suspend fun addIncident(incident: IncidentModel): OperationResult<Unit>

    /**
     * Получение видимых инцидентов в заданной географической области.
     * Возвращает метки со статусом VERIFIED или PENDING_VERIFICATION.
     */
    suspend fun getVisibleIncidents(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): OperationResult<List<IncidentModel>>

    /**
     * Получение только верифицированных инцидентов для алгоритма маршрутизации.
     */
    suspend fun getVerifiedIncidents(): OperationResult<List<IncidentModel>>

    /**
     * Обработка действия верификации (подтверждение или оспаривание).
     * Автоматически обновляет счётчики и статус инцидента согласно правилам:
     * - confirmCount >= N && disputeCount < K → VERIFIED
     * - disputeCount >= K → ARCHIVED
     */
    suspend fun processVerification(
        incidentId: Long,
        userId: Long,
        action: VerificationActionType,
        confirmThreshold: Int = OtherProperties.CONFIRM_THRESHOLD,
        disputeThreshold: Int = OtherProperties.DISPUTE_THRESHOLD
    ): OperationResult<Unit>
}