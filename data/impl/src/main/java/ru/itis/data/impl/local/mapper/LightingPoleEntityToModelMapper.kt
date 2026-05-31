package ru.itis.data.impl.local.mapper

import ru.itis.core.domain.model.safety.LightingPoleModel
import ru.itis.data.impl.local.entity.LightingPoleEntity
import javax.inject.Inject

/**
 * Маппер для конвертации LightingPoleEntity (Room) в LightingPoleModel (Domain).
 */
internal class LightingPoleEntityToModelMapper @Inject constructor() {
    fun map(input: LightingPoleEntity): LightingPoleModel {
        return LightingPoleModel(
            globalId = input.globalId,
            pillarType = input.pillarType,
            pillarNumber = input.pillarNumber,
            lightsNumber = input.lightsNumber,
            status = input.status,
            district = input.district,
            latitude = input.latitude,
            longitude = input.longitude,
        )
    }

    fun mapList(entities: List<LightingPoleEntity>): List<LightingPoleModel> = entities.map(::map)
}