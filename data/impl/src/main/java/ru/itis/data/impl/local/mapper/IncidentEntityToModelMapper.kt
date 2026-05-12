package ru.itis.data.impl.local.mapper

import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.data.impl.local.entity.IncidentEntity
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Маппер для конвертации IncidentEntity (Room) в IncidentModel (Domain).
 */
internal class IncidentEntityToModelMapper @Inject constructor() {

    fun map(input: IncidentEntity): IncidentModel {
        return IncidentModel(
            id = input.id,
            creatorId = input.creatorId,
            creatorNickname = input.creatorNickname,
            latitude = input.latitude,
            longitude = input.longitude,
            type = IncidentType.valueOf(input.incidentType),
            description = input.description,
            createdAt = Instant.ofEpochMilli(input.createdAtMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
            status = IncidentStatus.valueOf(input.status),
            confirmCount = input.confirmCount,
            disputeCount = input.disputeCount
        )
    }

    fun mapList(entities: List<IncidentEntity>): List<IncidentModel> = entities.map(::map)
}