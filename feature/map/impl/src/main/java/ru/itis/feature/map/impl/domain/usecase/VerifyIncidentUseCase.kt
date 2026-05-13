package ru.itis.feature.map.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.VerificationActionType
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.IncidentRepository
import ru.itis.core.domain.repository.UserRepository
import ru.itis.core.utils.BusinessErrorCode
import ru.itis.core.utils.OperationResult
import ru.itis.core.utils.properties.OtherProperties
import javax.inject.Inject

/**
 * UseCase для обработки действия верификации (подтверждение/оспаривание).
 */
internal class VerifyIncidentUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val userRepository: UserRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {

    /**
     * @param incidentId ID инцидента для верификации
     * @param action тип действия: CONFIRM или DISPUTE
     * @param confirmThreshold порог подтверждений для перехода в VERIFIED
     * @param disputeThreshold порог оспариваний для перехода в ARCHIVED
     * Возвращает актуальный статус инцидента после обработки голоса.
     */
    suspend operator fun invoke(
        incidentId: Long,
        action: VerificationActionType,
        confirmThreshold: Int = OtherProperties.CONFIRM_THRESHOLD,
        disputeThreshold: Int = OtherProperties.DISPUTE_THRESHOLD
    ): OperationResult<IncidentModel> {
        return withContext(dispatcher) {
            when (val currentUserResult = userRepository.getCurrentUser()) {
                is OperationResult.Success -> {
                    val currentUser = currentUserResult.data
                    val userId = currentUser.id ?: return@withContext OperationResult.Error(
                        OperationResult.ErrorType.Business(BusinessErrorCode.USER_ID_NOT_FOUND)
                    )

                    incidentRepository.processVerification(
                        incidentId = incidentId,
                        userId = userId,
                        action = action,
                        confirmThreshold = confirmThreshold,
                        disputeThreshold = disputeThreshold
                    )

                    incidentRepository.getIncidentById(incidentId)
                }

                is OperationResult.Error -> {
                    return@withContext OperationResult.Error(currentUserResult.errorType)
                }
            }
        }
    }
}