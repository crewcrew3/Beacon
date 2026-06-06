package ru.itis.feature.emergency.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.itis.feature.emergency.impl.navigation.sosSettingsScreenEntryBuilder

@Module
@InstallIn(ActivityRetainedComponent::class)
object SosSettingsNavModule {

    @IntoSet
    @Provides
    fun provideSosSettingsEntry(): EntryProviderScope<NavKey>.() -> Unit = {
        sosSettingsScreenEntryBuilder()
    }
}