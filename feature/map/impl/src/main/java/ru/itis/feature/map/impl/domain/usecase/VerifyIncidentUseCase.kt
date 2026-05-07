package ru.itis.feature.map.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.core.domain.model.mark.VerificationActionType
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.IncidentRepository
import ru.itis.core.domain.repository.UserRepository
import ru.itis.core.utils.properties.ExceptionCode
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
     */
    suspend operator fun invoke(
        incidentId: Long,
        action: VerificationActionType,
        confirmThreshold: Int = OtherProperties.CONFIRM_THRESHOLD,
        disputeThreshold: Int = OtherProperties.DISPUTE_THRESHOLD
    ) {
        withContext(dispatcher) {
            val currentUser = userRepository.getCurrentUser() ?: throw IllegalArgumentException(ExceptionCode.UNAUTHORIZED)
            incidentRepository.processVerification(
                incidentId = incidentId,
                userId = requireNotNull(currentUser.id) { ExceptionCode.UNKNOWN_ERROR },
                action = action,
                confirmThreshold = confirmThreshold,
                disputeThreshold = disputeThreshold
            )
        }
    }
}