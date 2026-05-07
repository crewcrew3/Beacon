package ru.itis.data.impl.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.itis.data.impl.local.dao.IncidentDao
import ru.itis.data.impl.local.dao.UserDao
import ru.itis.data.impl.local.dao.VerificationActionDao
import ru.itis.data.impl.local.entity.IncidentEntity
import ru.itis.data.impl.local.entity.UserEntity
import ru.itis.data.impl.local.entity.VerificationActionEntity

@Database(
    entities = [
        UserEntity::class,
        IncidentEntity::class,
        VerificationActionEntity::class,
    ],
    version = 1
)
internal abstract class LocalDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val incidentDao: IncidentDao
    abstract val verificationActionDao: VerificationActionDao
}