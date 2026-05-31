package ru.itis.data.impl.local.dao

import androidx.room.*
import ru.itis.data.impl.local.entity.SafetyCameraEntity

@Dao
internal interface SafetyCameraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCameras(cameras: List<SafetyCameraEntity>)

    @Query("SELECT COUNT(*) FROM safety_cameras")
    suspend fun getCamerasCount(): Long
}