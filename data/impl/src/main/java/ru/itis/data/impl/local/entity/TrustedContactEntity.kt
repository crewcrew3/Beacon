package ru.itis.data.impl.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trusted_contacts")
data class TrustedContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phone: String
)