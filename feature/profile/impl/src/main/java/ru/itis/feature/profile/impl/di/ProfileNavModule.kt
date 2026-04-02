package ru.itis.feature.profile.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.itis.feature.profile.impl.navigation.profileScreenEntryBuilder

@Module
@InstallIn(ActivityRetainedComponent::class)
object ProfileNavModule {

    @IntoSet
    @Provides
    fun provideProfileEntry(): EntryProviderScope<NavKey>.() -> Unit = {
        profileScreenEntryBuilder()
    }
}