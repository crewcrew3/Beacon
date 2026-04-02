package ru.itis.data.impl.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.itis.data.impl.local.entity.UserEntity

@Dao
internal interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE user_phone_number = :phoneNumber")
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserEntity?
}