package ru.itis.core.utils

object ValidatorHelper {

    private const val REGEX_FOR_PHONE_NUMBER = "^7\\d{10}$"

    fun validatePhoneNumber(phoneNumber: String): Boolean {
        val regex = Regex(REGEX_FOR_PHONE_NUMBER)
        return regex.matches(phoneNumber)
    }

    fun validateNickname(firstName: String): Boolean {
        return (firstName.length <= 25) && (firstName.isNotBlank())
    }

    fun validatePassword(password: String): Boolean {
        return (8 <= password.length) && (password.length <= 100) && (password.isNotBlank())
    }
}