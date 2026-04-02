package ru.itis.core.domain.repository

interface AuthRepository {
    suspend fun logIn(phoneNumber: String, password: String)
    suspend fun signUp(
        nickname: String,
        phoneNumber: String,
        password: String,
        repeatPassword: String
    )
    suspend fun logOut()
    suspend fun isUserAuth(): Boolean
}