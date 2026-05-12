package ru.itis.data.impl.repository

import ru.itis.core.domain.model.mark.*
import ru.itis.core.domain.repository.IncidentRepository
import ru.itis.core.utils.BusinessErrorCode
import ru.itis.core.utils.OperationResult
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

    override suspend fun addIncident(incident: IncidentModel): OperationResult<IncidentModel> {
        return try {
            val entity = modelToEntity.map(incident)
            val generatedId = incidentDao.insertIncident(entity)
            val savedIncident = incident.copy(id = generatedId)
            OperationResult.Success(savedIncident)
        } catch (e: Exception) {
            OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }

    override suspend fun getVisibleIncidents(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): OperationResult<List<IncidentModel>> {
        return try {
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
            OperationResult.Success(entityToModel.mapList(entities))
        } catch (e: Exception) {
            OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }

    override suspend fun getVerifiedIncidents(): OperationResult<List<IncidentModel>> {
        return try {
            val entities = incidentDao.getVerifiedIncidentsForRouting(
                verifiedStatus = IncidentStatus.VERIFIED.name
            )
            OperationResult.Success(entityToModel.mapList(entities))
        } catch (e: Exception) {
            OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }

    override suspend fun processVerification(
        incidentId: Long,
        userId: Long,
        action: VerificationActionType,
        confirmThreshold: Int,
        disputeThreshold: Int
    ): OperationResult<Unit> {
        try {
            // Проверяем, существует ли инцидент
            val incident = incidentDao.getIncidentById(incidentId) ?: return OperationResult.Error(
                OperationResult.ErrorType.Business(BusinessErrorCode.INCIDENT_NOT_FOUND)
            )

            // Сохраняем действие верификации (не проверяем, голосовал ли пользователь, тк можно изменить голос)
            verificationDao.insertVerificationAction(
                VerificationActionEntity(
                    incidentId = incidentId,
                    userId = userId,
                    actionType = action.name
                )
            )

            // Пересчитываем счётчики
            val counts = verificationDao.getVerificationCounts(incidentId)
            val newConfirmCount = counts.confirmCount
            val newDisputeCount = counts.disputeCount

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
            return OperationResult.Success(Unit)
        } catch (e: Exception) {
            return OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }

    override suspend fun getIncidentById(incidentId: Long): OperationResult<IncidentModel> {
        return try {
            val entity = incidentDao.getIncidentById(
                incidentId = incidentId
            ) ?: return OperationResult.Error(OperationResult.ErrorType.Business(BusinessErrorCode.INCIDENT_NOT_FOUND))
            OperationResult.Success(entityToModel.map(entity))
        } catch (e: Exception) {
            OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }
}