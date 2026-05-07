package ru.itis.core.domain.repository

/**
 * Репозиторий для работы с авторизацией.
 */
interface AuthRepository {

    /** Вход в систему.*/
    suspend fun logIn(phoneNumber: String, password: String)

    /** Регистрация.*/
    suspend fun signUp(
        nickname: String,
        phoneNumber: String,
        password: String,
        repeatPassword: String
    )

    /** Выход из системы.*/
    suspend fun logOut()

    /** Проверка авторизован ли пользователь.*/
    suspend fun isUserAuth(): Boolean
}