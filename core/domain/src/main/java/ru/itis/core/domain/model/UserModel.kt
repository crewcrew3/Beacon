package ru.itis.core.domain.model

/**
 * Основная сущность юзера.
 * Используется в UI, репозиториях.
 */
data class UserModel(
    val id: Long? = null,
    val nickname: String,
    val phoneNumber: String,
    val imageUrl: String?,
)