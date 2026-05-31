package ru.itis.data.impl.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Камера видеонаблюдения из открытого набора данных Москвы.
 * Источник: https://data.apicrafter.ru/packages/datamos-registeryardcameras
 */
@Entity(
    tableName = "safety_cameras",
    indices = [
        Index(value = ["latitude", "longitude"]), // для гео-запросов
        Index(value = ["district"]) // для фильтрации
    ]
)
internal data class SafetyCameraEntity(
    @PrimaryKey
    @ColumnInfo(name = "global_id")
    val globalId: Long,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "district")
    val district: String?,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    // Дополнительные поля для возможного расширения
    @ColumnInfo(name = "ovd_address")
    val ovdAddress: String?,

    @ColumnInfo(name = "imported_at")
    val importedAtMillis: Long = System.currentTimeMillis()
)