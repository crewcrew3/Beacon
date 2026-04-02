package ru.itis.core.utils

import ru.itis.core.utils.properties.ExceptionCode
import javax.inject.Inject
import javax.inject.Singleton
import ru.itis.core.ui.R

@Singleton
class ExceptionHandler @Inject constructor() {
    fun handleExceptionMessage(exceptionMessage: String?) : Int {
        return when (exceptionMessage) {

            ExceptionCode.GET_PROFILE_ERROR -> R.string.exception_msg_profile

            ExceptionCode.WRONG_CREDENTIALS -> R.string.exception_msg_auth_wrong_credentials
            ExceptionCode.USER_ALREADY_EXISTS -> R.string.exception_msg_user_already_exists

            ExceptionCode.UNAUTHORIZED -> R.string.exception_msg_unauthorized

            else -> R.string.exception_msg_common
        }
    }
}