package ru.itis.feature.signup.impl.ui.mvi

import androidx.annotation.StringRes

internal sealed interface SignUpScreenEffect {
    data class Message(@StringRes val message: Int) : SignUpScreenEffect
    data class FormErrors(
        val isNicknameError: Boolean,
        val isPhoneNumberError: Boolean,
        val isPasswordError: Boolean,
        val isRepeatPasswordError: Boolean,
    ) : SignUpScreenEffect
}