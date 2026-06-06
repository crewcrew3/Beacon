package ru.itis.navigation.impl.navigator

import ru.itis.feature.emergency.api.EmergencyScreenNavKey
import ru.itis.feature.emergency.api.SosSettingsScreenNavKey
import ru.itis.navigation.api.EmergencyNavigator
import ru.itis.navigation.impl.BackStackHolder
import javax.inject.Inject

class EmergencyNavigatorImpl @Inject constructor(
    private val backStackHolder: BackStackHolder
) : EmergencyNavigator {
    override fun toEmergencyScreen() {
        backStackHolder.backStack?.add(EmergencyScreenNavKey)
    }

    override fun toSosSettingsScreen() {
        backStackHolder.backStack?.add(SosSettingsScreenNavKey)
    }

    override fun back() {
        backStackHolder.backStack?.removeLastOrNull()
    }
}