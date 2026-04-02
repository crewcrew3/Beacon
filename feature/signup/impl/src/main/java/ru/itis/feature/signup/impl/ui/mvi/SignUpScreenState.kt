package ru.itis.feature.signup.impl.ui.mvi

internal sealed interface SignUpScreenState {
    data object Initial : SignUpScreenState
}