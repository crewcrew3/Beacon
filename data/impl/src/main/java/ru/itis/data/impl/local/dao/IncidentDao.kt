package ru.itis.data.impl.local.dao

import androidx.room.*
import ru.itis.data.impl.local.entity.IncidentEntity

@Dao
internal interface IncidentDao {

    /**
     * Вставка инцидента в БД.
     * onConflict = REPLACE гарантирует, что при повторной вставке с тем же ID данные обновятся.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    /**
     * Обновление существующего инцидента по объекту.
     * Используется для изменения статусов и счётчиков верификации.
     */
    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    /**
     * Получение всех инцидентов, которые должны отображаться на карте пользователю.
     * Фильтр: статус VERIFIED или PENDING_VERIFICATION.
     * Гео-фильтр по прямоугольной области (для оптимизации загрузки).
     */
    @Query("""
        SELECT * FROM incidents 
        WHERE status IN (:visibleStatuses)
        AND latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLng AND :maxLng
        ORDER BY created_at DESC
    """)
    suspend fun getVisibleIncidentsInBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        visibleStatuses: List<String>
    ): List<IncidentEntity>

    /** Получение только верифицированных инцидентов для построения безопасного маршрута. */
    @Query("""
        SELECT * FROM incidents 
        WHERE status = :verifiedStatus
        ORDER BY created_at DESC
    """)
    suspend fun getVerifiedIncidentsForRouting(verifiedStatus: String): List<IncidentEntity>

    /** Получение инцидента по ID. */
    @Query("SELECT * FROM incidents WHERE id = :incidentId")
    suspend fun getIncidentById(incidentId: Long): IncidentEntity?
}