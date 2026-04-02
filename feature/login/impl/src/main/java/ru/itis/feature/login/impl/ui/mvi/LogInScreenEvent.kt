package ru.itis.feature.login.impl.ui.mvi

internal sealed interface LogInScreenEvent {
    data class OnLogInBtnClick(
        val phoneNumber: String,
        val password: String,
    ) : LogInScreenEvent

    data class OnFormFieldChanged(
        val phoneNumber: String? = null,
        val password: String? = null,
    ) : LogInScreenEvent

    data object OnBackBtnClick : LogInScreenEvent
}