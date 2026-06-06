package ru.itis.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.itis.core.domain.model.emergency.SosSettingsModel
import ru.itis.core.utils.OperationResult

interface SosSettingsRepository {
    fun getSettings(): Flow<SosSettingsModel>
    suspend fun saveSettings(settings: SosSettingsModel): OperationResult<Unit>
}