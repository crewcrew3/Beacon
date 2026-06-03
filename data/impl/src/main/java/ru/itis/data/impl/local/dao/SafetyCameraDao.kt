package ru.itis.data.impl.local.dao

import androidx.room.*
import ru.itis.data.impl.local.entity.SafetyCameraEntity

@Dao
internal interface SafetyCameraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCameras(cameras: List<SafetyCameraEntity>)

    /**
     * Получение камер в географических границах.
     * Используется простой прямоугольный фильтр для производительности.
     */
    @Query("""
        SELECT * FROM safety_cameras 
        WHERE latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLng AND :maxLng
        ORDER BY latitude, longitude
    """)
    suspend fun getCamerasInBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): List<SafetyCameraEntity>

    @Query("SELECT COUNT(*) FROM safety_cameras")
    suspend fun getCamerasCount(): Long
}