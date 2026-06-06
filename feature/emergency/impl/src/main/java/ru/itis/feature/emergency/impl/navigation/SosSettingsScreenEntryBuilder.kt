package ru.itis.feature.emergency.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.itis.feature.emergency.api.SosSettingsScreenNavKey
import ru.itis.feature.emergency.impl.ui.sossettings.SosSettingsScreen

fun EntryProviderScope<NavKey>.sosSettingsScreenEntryBuilder() {
    entry<SosSettingsScreenNavKey> {
        SosSettingsScreen()
    }
}