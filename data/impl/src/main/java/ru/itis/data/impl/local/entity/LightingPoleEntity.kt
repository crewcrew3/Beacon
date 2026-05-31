package ru.itis.data.impl.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Опора наружного освещения из открытого набора данных Москвы.
 * Источник: https://data.apicrafter.ru/packages/datamos-externallighting
 */
@Entity(
    tableName = "lighting_poles",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["status"]) // "active", "maintenance" и т.д.
    ]
)
internal data class LightingPoleEntity(
    @PrimaryKey
    @ColumnInfo(name = "global_id")
    val globalId: Long,

    @ColumnInfo(name = "pillar_number")
    val pillarNumber: String?,

    @ColumnInfo(name = "pillar_type")
    val pillarType: String?,

    @ColumnInfo(name = "lights_number")
    val lightsNumber: Int?,

    @ColumnInfo(name = "status")
    val status: String?,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "district")
    val district: String?,

    @ColumnInfo(name = "imported_at")
    val importedAtMillis: Long = System.currentTimeMillis()
)