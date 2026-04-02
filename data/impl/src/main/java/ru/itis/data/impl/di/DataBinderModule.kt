package ru.itis.data.impl.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.core.domain.local.storage.BasicUserDataStorage
import ru.itis.core.domain.repository.AuthRepository
import ru.itis.core.domain.repository.UserRepository
import ru.itis.data.impl.local.storage.BasicUserDataStorageImpl
import ru.itis.data.impl.repository.AuthRepositoryImpl
import ru.itis.data.impl.repository.UserRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface DataBinderModule {

    @Binds
    @Reusable
    fun bindUserRepositoryToImplementation(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Reusable
    fun bindAuthRepositoryToImplementation(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Reusable
    fun bindBasicUserDataStorageToImplementation(impl: BasicUserDataStorageImpl): BasicUserDataStorage
}