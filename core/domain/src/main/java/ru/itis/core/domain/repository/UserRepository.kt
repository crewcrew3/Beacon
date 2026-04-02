package ru.itis.core.domain.repository

import ru.itis.core.domain.model.UserModel

interface UserRepository {
    suspend fun getCurrentUser(): UserModel
}