package ru.itis.data.impl.repository

import ru.itis.core.domain.storage.BasicUserDataStorage
import ru.itis.core.domain.repository.AuthRepository
import ru.itis.core.utils.BusinessErrorCode
import ru.itis.core.utils.OperationResult
import ru.itis.core.utils.properties.OtherProperties
import ru.itis.data.impl.local.dao.UserDao
import ru.itis.data.impl.local.entity.UserEntity
import javax.inject.Inject

internal class AuthRepositoryImpl @Inject constructor(
    private val basicUserDataStorage: BasicUserDataStorage,
    private val userDao: UserDao,
) : AuthRepository {

    override suspend fun logIn(phoneNumber: String, password: String): OperationResult<Unit> {
        try {
            val user = userDao.getUserByPhoneNumber(phoneNumber)
            if (user == null || user.userPassword != password) {
                return OperationResult.Error(
                    OperationResult.ErrorType.Business(BusinessErrorCode.WRONG_CREDENTIALS)
                )
            }
            basicUserDataStorage.saveUserPhoneNumber(phoneNumber)

            return OperationResult.Success(Unit)
        } catch (e: Exception) {
            return OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }

    override suspend fun signUp(
        nickname: String,
        phoneNumber: String,
        password: String,
        repeatPassword: String
    ): OperationResult<Unit> {
        try {
            if (userDao.getUserByPhoneNumber(phoneNumber) != null) {
                return OperationResult.Error(
                    OperationResult.ErrorType.Business(BusinessErrorCode.USER_ALREADY_EXISTS)
                )
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

                return OperationResult.Success(Unit)
            }
        } catch (e: Exception) {
            return OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }

    override suspend fun logOut(): OperationResult<Unit> {
        return try {
            basicUserDataStorage.clearUserDataOnLogOut()
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }

    override suspend fun isUserAuth(): Boolean {
        return basicUserDataStorage.getUserPhoneNumber() != null
    }
}