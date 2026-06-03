package ru.itis.data.impl.repository

import ru.itis.core.domain.model.safety.SafetyCameraModel
import ru.itis.core.domain.repository.SafetyCameraRepository
import ru.itis.core.utils.OperationResult
import ru.itis.data.impl.local.dao.SafetyCameraDao
import ru.itis.data.impl.local.mapper.SafetyCameraEntityToModelMapper
import javax.inject.Inject

internal class SafetyCameraRepositoryImpl @Inject constructor(
    private val safetyCameraDao: SafetyCameraDao,
    private val mapper: SafetyCameraEntityToModelMapper
) : SafetyCameraRepository {

    override suspend fun getCamerasInBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): OperationResult<List<SafetyCameraModel>> {
        return try {
            val entities = safetyCameraDao.getCamerasInBounds(minLat, maxLat, minLng, maxLng)
            OperationResult.Success(mapper.mapList(entities))
        } catch (e: Exception) {
            OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }
}