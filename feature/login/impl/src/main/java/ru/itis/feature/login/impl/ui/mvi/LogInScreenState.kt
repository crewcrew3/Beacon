package ru.itis.feature.login.impl.ui.mvi

internal sealed interface LogInScreenState {
    data object Initial : LogInScreenState
}