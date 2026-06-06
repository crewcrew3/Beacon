package ru.itis.data.impl.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.itis.data.impl.local.entity.TrustedContactEntity

@Dao
interface TrustedContactDao {
    @Query("SELECT * FROM trusted_contacts")
    fun getAllContacts(): Flow<List<TrustedContactEntity>>

    @Query("SELECT phone FROM trusted_contacts")
    suspend fun getAllPhones(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContactEntity)

    @Query("DELETE FROM trusted_contacts WHERE phone = :phone")
    suspend fun deleteByPhone(phone: String)

    @Query("DELETE FROM trusted_contacts")
    suspend fun deleteAllContacts()
}