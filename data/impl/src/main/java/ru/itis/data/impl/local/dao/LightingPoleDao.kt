package ru.itis.data.impl.local.dao

import androidx.room.*
import ru.itis.data.impl.local.entity.LightingPoleEntity

@Dao
internal interface LightingPoleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoles(poles: List<LightingPoleEntity>)

    @Query("SELECT COUNT(*) FROM lighting_poles")
    suspend fun getPolesCount(): Long
}