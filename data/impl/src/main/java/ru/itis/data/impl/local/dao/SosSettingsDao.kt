package ru.itis.data.impl.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itis.data.impl.local.entity.SosSettingsEntity

@Dao
interface SosSettingsDao {
    @Query("SELECT * FROM sos_settings WHERE id = 1")
    fun getSettings(): Flow<SosSettingsEntity?>

    @androidx.room.Upsert
    suspend fun upsertSettings(settings: SosSettingsEntity)
}