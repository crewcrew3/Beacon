package ru.itis.data.impl.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_settings")
data class SosSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Всегда 1, так как настройки одни на всех
    val message: String
)