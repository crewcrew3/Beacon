package ru.itis.data.impl.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.itis.core.domain.model.emergency.SosSettingsModel
import ru.itis.core.domain.repository.SosSettingsRepository
import ru.itis.core.utils.OperationResult
import ru.itis.data.impl.local.dao.SosSettingsDao
import ru.itis.data.impl.local.dao.TrustedContactDao
import ru.itis.data.impl.local.entity.SosSettingsEntity
import ru.itis.data.impl.local.entity.TrustedContactEntity
import javax.inject.Inject

class SosSettingsRepositoryImpl @Inject constructor(
    private val sosSettingsDao: SosSettingsDao,
    private val trustedContactDao: TrustedContactDao
) : SosSettingsRepository {

    // Объединяем два Flow в один, чтобы получать актуальные настройки
    override fun getSettings(): Flow<SosSettingsModel> {
        return combine(
            sosSettingsDao.getSettings(),
            trustedContactDao.getAllContacts()
        ) { settings, contacts ->
            SosSettingsModel(
                message = settings?.message ?: "",
                trustedContacts = contacts.map { it.phone }
            )
        }
    }

    override suspend fun saveSettings(settings: SosSettingsModel): OperationResult<Unit> {
        return try {
            sosSettingsDao.upsertSettings(
                SosSettingsEntity(id = 1, message = settings.message)
            )

            // Удаляем все контакты и вставляем заново
            trustedContactDao.deleteAllContacts()
            settings.trustedContacts.forEach { phone ->
                trustedContactDao.insertContact(TrustedContactEntity(phone = phone))
            }

            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(
                OperationResult.ErrorType.Unknown(e)
            )
        }
    }
}