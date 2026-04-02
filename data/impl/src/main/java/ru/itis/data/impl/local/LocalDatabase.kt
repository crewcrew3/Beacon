package ru.itis.data.impl.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.itis.data.impl.local.dao.UserDao
import ru.itis.data.impl.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
    ],
    version = 1
)
internal abstract class LocalDatabase : RoomDatabase() {
    abstract val userDao: UserDao
}