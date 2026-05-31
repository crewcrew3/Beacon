package ru.itis.data.impl.local.mapper

import ru.itis.core.domain.model.safety.SafetyCameraModel
import ru.itis.data.impl.local.entity.SafetyCameraEntity
import javax.inject.Inject

/**
 * Маппер для конвертации SafetyCameraEntity (Room) в SafetyCameraModel (Domain).
 */
internal class SafetyCameraEntityToModelMapper @Inject constructor() {
    fun map(input: SafetyCameraEntity): SafetyCameraModel {
        return SafetyCameraModel(
            globalId = input.globalId,
            address = input.address,
            district = input.district,
            latitude = input.latitude,
            longitude = input.longitude,
            ovdAddress = input.ovdAddress
        )
    }

    fun mapList(entities: List<SafetyCameraEntity>): List<SafetyCameraModel> = entities.map(::map)
}