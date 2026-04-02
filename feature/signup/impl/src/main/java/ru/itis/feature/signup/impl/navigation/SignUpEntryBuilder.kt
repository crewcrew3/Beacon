package ru.itis.feature.signup.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.itis.feature.signup.api.SignUpNavKey
import ru.itis.feature.signup.impl.ui.SignUpScreen

fun EntryProviderScope<NavKey>.signUpEntryBuilder() {
    entry<SignUpNavKey> {
        SignUpScreen()
    }
}