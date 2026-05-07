package ru.itis.data.impl.repository

import ru.itis.core.domain.storage.BasicUserDataStorage
import ru.itis.core.domain.model.UserModel
import ru.itis.core.domain.repository.UserRepository
import ru.itis.data.impl.local.dao.UserDao
import ru.itis.data.impl.local.mapper.UserEntityToModelMapper
import javax.inject.Inject

internal class UserRepositoryImpl @Inject constructor(
    private val basicUserDataStorage: BasicUserDataStorage,
    private val userDao: UserDao,
    private val userEntityToModelMapper: UserEntityToModelMapper,
) : UserRepository {

    override suspend fun getCurrentUser(): UserModel? {
        val phoneNumber = basicUserDataStorage.getUserPhoneNumber() ?: return null
        val user = userDao.getUserByPhoneNumber(phoneNumber)
        return user?.let(userEntityToModelMapper::map)
    }
}