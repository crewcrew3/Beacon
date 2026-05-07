package ru.itis.data.impl.repository

import ru.itis.core.domain.model.mark.*
import ru.itis.core.domain.repository.IncidentRepository
import ru.itis.data.impl.local.dao.IncidentDao
import ru.itis.data.impl.local.dao.VerificationActionDao
import ru.itis.data.impl.local.entity.VerificationActionEntity
import ru.itis.data.impl.local.mapper.IncidentEntityToModelMapper
import ru.itis.data.impl.local.mapper.IncidentModelToEntityMapper
import javax.inject.Inject

internal class IncidentRepositoryImpl @Inject constructor(
    private val incidentDao: IncidentDao,
    private val verificationDao: VerificationActionDao,
    private val entityToModel: IncidentEntityToModelMapper,
    private val modelToEntity: IncidentModelToEntityMapper,
) : IncidentRepository {

    override suspend fun addIncident(incident: IncidentModel): Long {
        val entity = modelToEntity.map(incident)
        return incidentDao.insertIncident(entity)
    }

    override suspend fun getVisibleIncidents(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): List<IncidentModel> {
        val entities = incidentDao.getVisibleIncidentsInBounds(
            minLat = minLat,
            maxLat = maxLat,
            minLng = minLng,
            maxLng = maxLng,
            visibleStatuses = listOf(
                IncidentStatus.VERIFIED.name,
                IncidentStatus.PENDING_VERIFICATION.name
            )
        )
        return entityToModel.mapList(entities)
    }

    override suspend fun getVerifiedIncidents(): List<IncidentModel> {
        val entities = incidentDao.getVerifiedIncidentsForRouting(
            verifiedStatus = IncidentStatus.VERIFIED.name
        )
        return entityToModel.mapList(entities)
    }

    override suspend fun processVerification(
        incidentId: Long,
        userId: Long,
        action: VerificationActionType,
        confirmThreshold: Int,
        disputeThreshold: Int
    ): Boolean {
        // Проверяем, существует ли инцидент
        val incident = incidentDao.getIncidentById(incidentId) ?: return false

        // Сохраняем действие верификации (не проверяем, голосовал ли пользователь, тк можно изменить голос)
        verificationDao.insertVerificationAction(
            VerificationActionEntity(
                incidentId = incidentId,
                userId = userId,
                actionType = action.name
            )
        )

        // Пересчитываем счётчики
        val (newConfirmCount, newDisputeCount) = verificationDao.getVerificationCounts(incidentId)

        // Определяем новый статус согласно алгоритму
        val newStatus = when {
            newDisputeCount >= disputeThreshold -> IncidentStatus.ARCHIVED.name
            newConfirmCount >= confirmThreshold && newDisputeCount < disputeThreshold ->
                IncidentStatus.VERIFIED.name
            else -> incident.status // оставляем текущий
        }

        // Обновляем инцидент в БД
        val updatedIncident = incident.copy(
            confirmCount = newConfirmCount,
            disputeCount = newDisputeCount,
            status = newStatus
        )
        incidentDao.updateIncident(updatedIncident)

        return true
    }
}