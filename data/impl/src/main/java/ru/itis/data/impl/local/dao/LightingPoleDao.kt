package ru.itis.data.impl.local.dao

import androidx.room.*
import ru.itis.data.impl.local.entity.LightingPoleEntity

@Dao
internal interface LightingPoleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoles(poles: List<LightingPoleEntity>)

    /**
     * Получение активных опор освещения в географических границах.
     * Фильтруем по status = 'active', чтобы не показывать неработающие.
     */
    @Query("""
        SELECT * FROM lighting_poles 
        WHERE latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLng AND :maxLng
        AND status = 'active'
        ORDER BY latitude, longitude
    """)
    suspend fun getActivePolesInBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): List<LightingPoleEntity>

    @Query("SELECT COUNT(*) FROM lighting_poles")
    suspend fun getPolesCount(): Long
}