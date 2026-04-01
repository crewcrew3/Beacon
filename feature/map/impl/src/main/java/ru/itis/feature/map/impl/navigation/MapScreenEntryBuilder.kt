package ru.itis.feature.map.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ru.itis.feature.map.api.MapScreenNavKey
import ru.itis.feature.map.impl.ui.MapScreenHost

fun EntryProviderScope<NavKey>.mapScreenEntryBuilder() {
    entry<MapScreenNavKey> {
        MapScreenHost()
    }
}