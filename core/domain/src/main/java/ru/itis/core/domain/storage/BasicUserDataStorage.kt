package ru.itis.core.domain.local.storage

interface BasicUserDataStorage {
    suspend fun saveUserPhoneNumber(phoneNumber: String)
    suspend fun getUserPhoneNumber(): String?
    suspend fun clearUserDataOnLogOut()
}