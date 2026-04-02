package ru.itis.data.impl.local.mapper

import ru.itis.core.domain.model.UserModel
import ru.itis.core.utils.properties.ExceptionCode
import ru.itis.data.impl.local.entity.UserEntity
import javax.inject.Inject

internal class UserEntityToModelMapper @Inject constructor() {
    fun map(input: UserEntity?): UserModel {
        val entity = requireNotNull(input) { ExceptionCode.GET_PROFILE_ERROR }
        return UserModel(
            id = entity.id,
            nickname = entity.userNickname,
            phoneNumber = entity.userPhoneNumber,
            imageUrl = entity.userImageUrl ?: ""
        )
    }
}