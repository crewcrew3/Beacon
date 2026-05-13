package ru.itis.core.domain.repository

import ru.itis.core.domain.model.UserModel
import ru.itis.core.utils.OperationResult

/**
 * Репозиторий для работы с пользователем и его данными
 */
interface UserRepository {

    /** Получение текущего пользователя, авторизованного в системе на данный момент. */
    suspend fun getCurrentUser(): OperationResult<UserModel>
}