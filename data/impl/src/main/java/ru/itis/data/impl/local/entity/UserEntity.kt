package ru.itis.data.impl.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(
        value = ["user_phone_number"],
        unique = true
    )]
)
internal data class UserEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    var id: Int = 0,

    @ColumnInfo(name = "user_phone_number")
    var userPhoneNumber: String,

    @ColumnInfo(name = "user_nickname")
    var userNickname: String,

    @ColumnInfo(name = "user_image_url")
    var userImageUrl: String?,

    @ColumnInfo(name = "user_password")
    var userPassword: String,
)