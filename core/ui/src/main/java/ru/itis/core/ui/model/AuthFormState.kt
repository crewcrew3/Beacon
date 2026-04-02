package ru.itis.core.ui.model

data class AuthFormState(
    val nickname: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val repeatPassword: String = ""
)