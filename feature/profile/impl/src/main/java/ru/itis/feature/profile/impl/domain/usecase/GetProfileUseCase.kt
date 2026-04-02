package ru.itis.feature.profile.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.core.domain.model.UserModel
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.UserRepository
import javax.inject.Inject

internal class GetProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(): UserModel {
        return withContext(dispatcher) {
            userRepository.getCurrentUser()
        }
    }
}