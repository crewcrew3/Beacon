package ru.itis.navigation.impl.di

import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.navigation.api.BottomBarNavigator
import ru.itis.navigation.api.ProfileNavigator
import ru.itis.navigation.impl.navigator.BottomBarNavigatorImpl
import ru.itis.navigation.impl.navigator.ProfileNavigatorImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface NavigationBinderModule {

    @Binds
    @Reusable
    fun bindBottomBarNavigatorToImplementation(impl: BottomBarNavigatorImpl): BottomBarNavigator

    @Binds
    @Reusable
    fun bindProfileNavigatorToImplementation(impl: ProfileNavigatorImpl): ProfileNavigator
}