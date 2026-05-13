package ru.itis.data.impl.repository

import ru.itis.core.domain.storage.BasicUserDataStorage
import ru.itis.core.domain.model.UserModel
import ru.itis.core.domain.repository.UserRepository
import ru.itis.core.utils.BusinessErrorCode
import ru.itis.core.utils.OperationResult
import ru.itis.data.impl.local.dao.UserDao
import ru.itis.data.impl.local.mapper.UserEntityToModelMapper
import javax.inject.Inject

internal class UserRepositoryImpl @Inject constructor(
    private val basicUserDataStorage: BasicUserDataStorage,
    private val userDao: UserDao,
    private val userEntityToModelMapper: UserEntityToModelMapper,
) : UserRepository {

    override suspend fun getCurrentUser(): OperationResult<UserModel> {
        try {
            val phoneNumber =
                basicUserDataStorage.getUserPhoneNumber() ?: return OperationResult.Error(
                    OperationResult.ErrorType.Business(BusinessErrorCode.UNAUTHORIZED)
                )

            val user =
                userDao.getUserByPhoneNumber(phoneNumber) ?: return OperationResult.Error(
                OperationResult.ErrorType.Business(BusinessErrorCode.USER_NOT_FOUND)
                )

            val userModel = userEntityToModelMapper.map(user)

            return OperationResult.Success(userModel)
        } catch (e: Exception) {
            return OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }
}