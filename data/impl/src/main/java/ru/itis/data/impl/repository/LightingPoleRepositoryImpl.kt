package ru.itis.data.impl.repository

import ru.itis.core.domain.model.safety.LightingPoleModel
import ru.itis.core.domain.repository.LightingPoleRepository
import ru.itis.core.utils.OperationResult
import ru.itis.data.impl.local.dao.LightingPoleDao
import ru.itis.data.impl.local.mapper.LightingPoleEntityToModelMapper
import javax.inject.Inject

internal class LightingPoleRepositoryImpl @Inject constructor(
    private val lightingPoleDao: LightingPoleDao,
    private val mapper: LightingPoleEntityToModelMapper
) : LightingPoleRepository {

    override suspend fun getLightingPolesInBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): OperationResult<List<LightingPoleModel>> {
        return try {
            val entities = lightingPoleDao.getActivePolesInBounds(minLat, maxLat, minLng, maxLng)
            OperationResult.Success(mapper.mapList(entities))
        } catch (e: Exception) {
            OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }
}