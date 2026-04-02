package ru.itis.core.domain.model

data class UserModel(
    val id: Int,
    val nickname: String,
    val phoneNumber: String,
    val imageUrl: String,
)