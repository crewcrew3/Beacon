package ru.itis.core.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import ru.itis.core.domain.qualifiers.IoDispatchers

@Module
@InstallIn(SingletonComponent::class)
class DomainModule {

    @Provides
    @IoDispatchers
    fun provideIoDispatchers(): CoroutineDispatcher = Dispatchers.IO
}