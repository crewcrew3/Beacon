package ru.itis.core.domain.repository

import ru.itis.core.domain.model.UserModel

/**
 * Репозиторий для работы с пользователем и его данными
 */
interface UserRepository {

    /** Получение текущего пользователя, авторизованного в системе на данный момент. */
    suspend fun getCurrentUser(): UserModel?
}