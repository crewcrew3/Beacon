package ru.itis.feature.map.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.itis.feature.map.impl.navigation.mapScreenEntryBuilder

@Module
@InstallIn(ActivityRetainedComponent::class)
object MapScreenNavModule {

    @IntoSet
    @Provides
    fun provideMapScreenEntry(): EntryProviderScope<NavKey>.() -> Unit = {
        mapScreenEntryBuilder()
    }
}