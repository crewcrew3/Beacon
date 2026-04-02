package ru.itis.navigation.impl.navigator

import ru.itis.feature.map.api.MapScreenNavKey
import ru.itis.feature.profile.api.ProfileScreenNavKey
import ru.itis.navigation.api.BottomBarNavigator
import ru.itis.navigation.impl.BackStackHolder
import javax.inject.Inject

class BottomBarNavigatorImpl @Inject constructor(
    private val backStackHolder: BackStackHolder
) : BottomBarNavigator {

    override fun toMapScreen() {
        backStackHolder.backStack?.add(MapScreenNavKey)
    }

    override fun toProfileScreen() {
        backStackHolder.backStack?.add(ProfileScreenNavKey)
    }
}