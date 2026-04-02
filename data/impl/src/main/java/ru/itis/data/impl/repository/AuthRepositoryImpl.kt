package ru.itis.data.impl.repository

import ru.itis.core.domain.local.storage.BasicUserDataStorage
import ru.itis.core.domain.repository.AuthRepository
import ru.itis.core.utils.properties.ExceptionCode
import ru.itis.core.utils.properties.OtherProperties
import ru.itis.data.impl.local.dao.UserDao
import ru.itis.data.impl.local.entity.UserEntity
import javax.inject.Inject

internal class AuthRepositoryImpl @Inject constructor(
    private val basicUserDataStorage: BasicUserDataStorage,
    private val userDao: UserDao,
) : AuthRepository {

    override suspend fun logIn(phoneNumber: String, password: String) {
        val user = userDao.getUserByPhoneNumber(phoneNumber) ?: throw IllegalArgumentException(ExceptionCode.WRONG_CREDENTIALS)
        if (user.userPassword != password) {
            throw IllegalArgumentException(ExceptionCode.WRONG_CREDENTIALS)
        } else {
            basicUserDataStorage.saveUserPhoneNumber(phoneNumber)
        }
    }

    override suspend fun signUp(
        nickname: String,
        phoneNumber: String,
        password: String,
        repeatPassword: String
    ) {
        if (userDao.getUserByPhoneNumber(phoneNumber) != null) {
            throw IllegalArgumentException(ExceptionCode.USER_ALREADY_EXISTS)
        } else {
            userDao.saveUser(
                UserEntity(
                    userPhoneNumber = phoneNumber,
                    userNickname = nickname,
                    userPassword = password,
                    userImageUrl = OtherProperties.USER_AVATAR_MOCK
                )
            )
            basicUserDataStorage.saveUserPhoneNumber(phoneNumber)
        }
    }

    override suspend fun logOut() {
        basicUserDataStorage.clearUserDataOnLogOut()
    }

    override suspend fun isUserAuth(): Boolean {
        return basicUserDataStorage.getUserPhoneNumber() != null
    }
}