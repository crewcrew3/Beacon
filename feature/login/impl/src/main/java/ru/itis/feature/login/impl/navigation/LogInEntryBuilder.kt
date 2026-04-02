package ru.itis.feature.login.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.itis.feature.login.api.LoginNavKey
import ru.itis.feature.login.impl.ui.LogInScreen

fun EntryProviderScope<NavKey>.logInEntryBuilder() {
    entry<LoginNavKey> {
        LogInScreen()
    }
}