package ru.itis.data.impl.local.mapper

import ru.itis.core.domain.model.mark.*
import ru.itis.data.impl.local.entity.IncidentEntity
import java.time.ZoneId
import javax.inject.Inject


/**
 * Маппер для конвертации IncidentModel (Domain) в IncidentEntity (Room).
 */
internal class IncidentModelToEntityMapper @Inject constructor() {

    fun map(input: IncidentModel): IncidentEntity {
        return IncidentEntity(
            id = input.id,
            creatorId = input.creatorId,
            latitude = input.latitude,
            longitude = input.longitude,
            incidentType = input.type.name,
            description = input.description,
            createdAtMillis = input.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            status = input.status.name,
            confirmCount = input.confirmCount,
            disputeCount = input.disputeCount
        )
    }
}