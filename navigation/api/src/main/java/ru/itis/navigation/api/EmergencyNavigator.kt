package ru.itis.navigation.api

interface EmergencyNavigator {
    fun toEmergencyScreen()
    fun toSosSettingsScreen()
    fun back()
}