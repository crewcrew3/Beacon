package ru.itis.feature.signup.impl.ui.mvi

internal sealed interface SignUpScreenEvent {
    data class OnSignUpBtnClick(
        val nickname: String,
        val phoneNumber: String,
        val password: String,
        val repeatPassword: String,
    ) : SignUpScreenEvent

    data class OnFormFieldChanged(
        val nickname: String? = null,
        val phoneNumber: String? = null,
        val password: String? = null,
        val repeatPassword: String? = null
    ) : SignUpScreenEvent

    data object OnBackBtnClick : SignUpScreenEvent
}