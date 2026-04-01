package ru.itis.navigation.impl

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackStackHolder @Inject constructor() {
    var backStack: NavBackStack<NavKey>? = null
}