package ru.itis.data.impl.local.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import ru.itis.core.domain.local.storage.BasicUserDataStorage
import ru.itis.core.utils.properties.PrefProperties
import javax.inject.Inject

internal class BasicUserDataStorageImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : BasicUserDataStorage {
    private companion object PreferencesKeys {
        val USER_PHONE_NUMBER = stringPreferencesKey(PrefProperties.USER_PHONE_NUMBER)
    }

    override suspend fun saveUserPhoneNumber(phoneNumber: String) {
        dataStore.edit { prefs ->
            prefs[USER_PHONE_NUMBER] = phoneNumber
        }
    }

    override suspend fun getUserPhoneNumber(): String? {
        val prefs = dataStore.data.first()
        return prefs[USER_PHONE_NUMBER]
    }

    override suspend fun clearUserDataOnLogOut() {
        dataStore.edit { prefs ->
            prefs.remove(USER_PHONE_NUMBER)
        }
    }
}