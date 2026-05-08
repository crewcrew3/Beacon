package ru.itis.data.impl.local.dao

import androidx.room.*
import ru.itis.data.impl.local.entity.UserEntity

@Dao
internal interface UserDao {

    /** Сохранение нового пользователя */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)

    /** Получение пользователя по номеру телефона */
    @Query("SELECT * FROM users WHERE user_phone_number = :phoneNumber")
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserEntity?
}