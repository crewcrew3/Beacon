package ru.itis.feature.profile.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.itis.feature.profile.api.ProfileScreenNavKey
import ru.itis.feature.profile.impl.ui.ProfileScreen

fun EntryProviderScope<NavKey>.profileScreenEntryBuilder() {
    entry<ProfileScreenNavKey> {
        ProfileScreen()
    }
}