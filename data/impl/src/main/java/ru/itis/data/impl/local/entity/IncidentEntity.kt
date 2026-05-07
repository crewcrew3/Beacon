package ru.itis.data.impl.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
internal data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,

    @ColumnInfo(name = "creator_id")
    val creatorId: Long,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    // Храним enum как String для гибкости при изменении списка типов
    @ColumnInfo(name = "incident_type")
    val incidentType: String,

    @ColumnInfo(name = "description")
    val description: String?,

    // Храним LocalDateTime как milliseconds для совместимости с Room
    @ColumnInfo(name = "created_at")
    val createdAtMillis: Long,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "confirm_count")
    val confirmCount: Long = 0,

    @ColumnInfo(name = "dispute_count")
    val disputeCount: Long = 0
)