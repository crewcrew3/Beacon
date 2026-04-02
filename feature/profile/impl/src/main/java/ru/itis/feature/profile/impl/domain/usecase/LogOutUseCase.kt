package ru.itis.feature.profile.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.AuthRepository
import javax.inject.Inject

internal class LogOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        return withContext(dispatcher) {
            authRepository.logOut()
        }
    }
}