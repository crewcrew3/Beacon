package ru.itis.feature.signup.impl.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet
import ru.itis.feature.signup.impl.navigation.signUpEntryBuilder

@Module
@InstallIn(ActivityRetainedComponent::class)
object SignUpNavModule {

    @IntoSet
    @Provides
    fun provideSignUpEntry(): EntryProviderScope<NavKey>.() -> Unit = {
        signUpEntryBuilder()
    }
}
