package ru.itis.feature.emergency.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.core.domain.model.emergency.SosSettingsModel
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.SosSettingsRepository
import ru.itis.core.utils.OperationResult
import javax.inject.Inject

internal class SaveSosSettingsUseCase @Inject constructor(
    private val repository: SosSettingsRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(settings: SosSettingsModel): OperationResult<Unit> = withContext(dispatcher) {
        repository.saveSettings(settings)
    }
}