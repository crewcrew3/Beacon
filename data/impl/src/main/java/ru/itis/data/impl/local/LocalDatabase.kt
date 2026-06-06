package ru.itis.data.impl.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.itis.data.impl.local.dao.IncidentDao
import ru.itis.data.impl.local.dao.LightingPoleDao
import ru.itis.data.impl.local.dao.SafetyCameraDao
import ru.itis.data.impl.local.dao.SosSettingsDao
import ru.itis.data.impl.local.dao.TrustedContactDao
import ru.itis.data.impl.local.dao.UserDao
import ru.itis.data.impl.local.dao.VerificationActionDao
import ru.itis.data.impl.local.entity.IncidentEntity
import ru.itis.data.impl.local.entity.LightingPoleEntity
import ru.itis.data.impl.local.entity.SafetyCameraEntity
import ru.itis.data.impl.local.entity.SosSettingsEntity
import ru.itis.data.impl.local.entity.TrustedContactEntity
import ru.itis.data.impl.local.entity.UserEntity
import ru.itis.data.impl.local.entity.VerificationActionEntity

@Database(
    entities = [
        UserEntity::class,
        IncidentEntity::class,
        VerificationActionEntity::class,
        LightingPoleEntity::class,
        SafetyCameraEntity::class,
        SosSettingsEntity::class,
        TrustedContactEntity::class,
    ],
    version = 2
)
internal abstract class LocalDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val incidentDao: IncidentDao
    abstract val verificationActionDao: VerificationActionDao
    abstract val lightingPoleDao: LightingPoleDao
    abstract val safetyCameraDao: SafetyCameraDao
    abstract val sosSettingsDao: SosSettingsDao
    abstract val trustedContactDao: TrustedContactDao
}