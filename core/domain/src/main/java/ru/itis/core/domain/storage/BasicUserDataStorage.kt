package ru.itis.core.domain.storage

/**
 * Временное локальное хранилище пользовательских данных.
 * Цели: контроль авторизации, хранение персональных настроек (в перспективе).
 */
interface BasicUserDataStorage {

    /** Сохранение номера телефона текущего пользователя во временном локальном хранилище. */
    suspend fun saveUserPhoneNumber(phoneNumber: String)

    /** Получение номера телефона текущего пользователя из временного локального хранилища */
    suspend fun getUserPhoneNumber(): String?

    /** Удаление определенных пользовательских данных из локального хранилища при выходе из системы */
    suspend fun clearUserDataOnLogOut()
}