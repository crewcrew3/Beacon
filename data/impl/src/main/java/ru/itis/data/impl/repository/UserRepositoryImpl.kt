package ru.itis.data.impl.repository

import ru.itis.core.domain.local.storage.BasicUserDataStorage
import ru.itis.core.domain.model.UserModel
import ru.itis.core.domain.repository.UserRepository
import ru.itis.core.utils.properties.ExceptionCode
import ru.itis.data.impl.local.dao.UserDao
import ru.itis.data.impl.local.mapper.UserEntityToModelMapper
import java.lang.IllegalArgumentException
import javax.inject.Inject

internal class UserRepositoryImpl @Inject constructor(
    private val basicUserDataStorage: BasicUserDataStorage,
    private val userDao: UserDao,
    private val userEntityToModelMapper: UserEntityToModelMapper,
) : UserRepository {

    override suspend fun getCurrentUser(): UserModel {
        val phoneNumber = basicUserDataStorage.getUserPhoneNumber() ?: throw IllegalArgumentException(ExceptionCode.UNAUTHORIZED)
        val user = userDao.getUserByPhoneNumber(phoneNumber)
        return user.let(userEntityToModelMapper::map)
    }
}