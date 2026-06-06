package ru.itis.feature.emergency.impl.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import ru.itis.core.domain.model.emergency.SosSettingsModel
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.SosSettingsRepository
import javax.inject.Inject

internal class GetSosSettingsUseCase @Inject constructor(
    private val repository: SosSettingsRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<SosSettingsModel> = repository.getSettings()
        .flowOn(dispatcher)
}