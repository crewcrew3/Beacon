package ru.itis.feature.map.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.IncidentRepository
import ru.itis.core.domain.repository.UserRepository
import ru.itis.core.utils.properties.ExceptionCode
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UseCase для добавления нового инцидента на карту.
 * Возвращает ID новой записи
 */
internal class AddIncidentUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val userRepository: UserRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {

    /**
     * @param latitude широта места инцидента
     * @param longitude долгота места инцидента
     * @param type тип инцидента из предустановленного списка
     * @param description опциональное текстовое описание
     */
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        type: IncidentType,
        description: String? = null
    ): Long {
        return withContext(dispatcher) {
            // Получаем ID текущего пользователя из хранилища
            val currentUser = userRepository.getCurrentUser() ?: throw IllegalArgumentException(ExceptionCode.UNAUTHORIZED)

            val newIncident = IncidentModel(
                creatorId = requireNotNull(currentUser.id) { ExceptionCode.UNKNOWN_ERROR },
                latitude = latitude,
                longitude = longitude,
                type = type,
                description = description,
                createdAt = LocalDateTime.now(),
                status = IncidentStatus.PENDING_VERIFICATION
            )
            incidentRepository.addIncident(newIncident)
        }
    }
}