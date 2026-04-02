package ru.itis.navigation.impl.navigator

import ru.itis.feature.login.api.LoginNavKey
import ru.itis.feature.signup.api.SignUpNavKey
import ru.itis.navigation.api.ProfileNavigator
import ru.itis.navigation.impl.BackStackHolder
import javax.inject.Inject

class ProfileNavigatorImpl @Inject constructor(
    private val backStackHolder: BackStackHolder
) : ProfileNavigator {
    override fun toLogInScreen() {
        backStackHolder.backStack?.add(LoginNavKey)
    }

    override fun toSighUpScreen() {
        backStackHolder.backStack?.add(SignUpNavKey)
    }

    override fun back() {
        backStackHolder.backStack?.removeLastOrNull()
    }
}