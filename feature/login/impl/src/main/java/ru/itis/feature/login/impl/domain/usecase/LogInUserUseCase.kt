package ru.itis.feature.login.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.AuthRepository
import javax.inject.Inject

internal class LogInUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        phoneNumber: String,
        password: String,
    ) {
        withContext(dispatcher) {
            authRepository.logIn(
                phoneNumber = phoneNumber,
                password = password,
            )
        }
    }
}