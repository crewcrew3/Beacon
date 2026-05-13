package ru.itis.data.impl.local.mapper

import ru.itis.core.domain.model.UserModel
import ru.itis.data.impl.local.entity.UserEntity
import javax.inject.Inject

/**
 * Маппер для конвертации UserEntity (Room) в UserModel (Domain).
 */
internal class UserEntityToModelMapper @Inject constructor() {

    fun map(input: UserEntity): UserModel {
        return UserModel(
            id = input.id,
            nickname = input.userNickname,
            phoneNumber = input.userPhoneNumber,
            imageUrl = input.userImageUrl
        )
    }
}