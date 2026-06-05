package ru.itis.feature.emergency.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.feature.emergency.impl.data.manager.AlarmManagerImpl
import ru.itis.feature.emergency.impl.data.manager.FakeCallManagerImpl
import ru.itis.feature.emergency.impl.domain.manager.AlarmManager
import ru.itis.feature.emergency.impl.domain.manager.FakeCallManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EmergencyManagerModule {

    @Binds
    @Singleton
    abstract fun bindAlarmManager(impl: AlarmManagerImpl): AlarmManager

    @Binds
    @Singleton
    abstract fun bindFakeCallManager(impl: FakeCallManagerImpl): FakeCallManager
}