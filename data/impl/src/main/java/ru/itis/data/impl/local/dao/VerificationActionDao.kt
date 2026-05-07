package ru.itis.data.impl.local.dao

import androidx.room.*
import ru.itis.data.impl.local.entity.VerificationActionEntity

@Dao
internal interface VerificationActionDao {

    /**
     * Добавление действия верификации.
     * При конфликте (повторный голос) — замена, чтобы можно было изменить мнение.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationAction(action: VerificationActionEntity)

    /**
     * Подсчёт количества подтверждений и оспариваний для инцидента.
     * Возвращает пару (confirmCount, disputeCount).
     */
    @Query("""
        SELECT 
            SUM(CASE WHEN action_type = 'CONFIRM' THEN 1 ELSE 0 END) as confirmCount,
            SUM(CASE WHEN action_type = 'DISPUTE' THEN 1 ELSE 0 END) as disputeCount
        FROM verification_actions 
        WHERE incident_id = :incidentId
    """)
    suspend fun getVerificationCounts(incidentId: Long): Pair<Long, Long>
}