package ru.itis.feature.map.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.IncidentRepository
import ru.itis.core.utils.BusinessErrorCode
import ru.itis.core.utils.OperationResult
import javax.inject.Inject

/**
 * UseCase для получения видимых инцидентов в области экрана карты.
 * Используется при изменении области видимости (camera position change).
 */
internal class GetVisibleIncidentsUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {

    /**
     * @param bounds массив из 4 Double: [minLat, maxLat, minLng, maxLng]
     * @return результат с списком инцидентов или ошибкой
     */
    suspend operator fun invoke(bounds: DoubleArray): OperationResult<List<IncidentModel>> {
        return withContext(dispatcher) {
            if (bounds.size != 4) {
                return@withContext OperationResult.Error(
                    OperationResult.ErrorType.Business(BusinessErrorCode.INVALID_BOUNDS)
                )
            }
            incidentRepository.getVisibleIncidents(
                minLat = bounds[0],
                maxLat = bounds[1],
                minLng = bounds[2],
                maxLng = bounds[3]
            )
        }
    }
}