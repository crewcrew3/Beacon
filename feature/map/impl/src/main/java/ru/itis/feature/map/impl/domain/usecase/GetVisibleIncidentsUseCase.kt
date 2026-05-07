package ru.itis.feature.map.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.IncidentRepository
import ru.itis.core.utils.properties.ExceptionCode
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
     * @return список инцидентов, которые нужно отобразить на карте
     */
    suspend operator fun invoke(bounds: DoubleArray): List<IncidentModel> {
        return withContext(dispatcher) {
            require(bounds.size == 4) { ExceptionCode.BOUNDS_ERROR }
            incidentRepository.getVisibleIncidents(
                minLat = bounds[0],
                maxLat = bounds[1],
                minLng = bounds[2],
                maxLng = bounds[3]
            )
        }
    }
}