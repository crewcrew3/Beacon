package ru.itis.feature.emergency.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.itis.feature.emergency.api.EmergencyScreenNavKey
import ru.itis.feature.emergency.impl.ui.emergency.EmergencyScreen

fun EntryProviderScope<NavKey>.emergencyScreenEntryBuilder() {
    entry<EmergencyScreenNavKey> {
        EmergencyScreen()
    }
}