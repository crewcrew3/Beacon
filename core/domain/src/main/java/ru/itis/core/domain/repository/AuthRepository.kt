package ru.itis.core.domain.repository

import ru.itis.core.utils.OperationResult

/**
 * Репозиторий для работы с авторизацией.
 */
interface AuthRepository {

    /** Вход в систему.*/
    suspend fun logIn(phoneNumber: String, password: String): OperationResult<Unit>

    /** Регистрация.*/
    suspend fun signUp(
        nickname: String,
        phoneNumber: String,
        password: String,
        repeatPassword: String
    ): OperationResult<Unit>

    /** Выход из системы.*/
    suspend fun logOut(): OperationResult<Unit>

    /** Проверка авторизован ли пользователь.*/
    suspend fun isUserAuth(): Boolean
}