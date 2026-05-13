package ru.itis.core.utils

import javax.inject.Inject
import javax.inject.Singleton
import ru.itis.core.ui.R

@Singleton
class ExceptionHandler @Inject constructor() {

    /** Сопоставляет ошибкам сообщение, видное пользователю */
    fun getErrorMessage(errorType: OperationResult.ErrorType): Int {
        return when (errorType) {
            is OperationResult.ErrorType.Business -> {
                when (errorType.code) {
                    BusinessErrorCode.USER_ALREADY_EXISTS -> R.string.exception_msg_user_already_exists
                    BusinessErrorCode.WRONG_CREDENTIALS -> R.string.exception_msg_auth_wrong_credentials
                    BusinessErrorCode.UNAUTHORIZED -> R.string.exception_msg_unauthorized
                    BusinessErrorCode.USER_NOT_FOUND -> R.string.exception_msg_user_not_found
                    BusinessErrorCode.USER_ID_NOT_FOUND -> R.string.exception_msg_get_data
                    BusinessErrorCode.INCIDENT_NOT_FOUND -> R.string.exception_msg_incident_not_found
                    BusinessErrorCode.INVALID_BOUNDS -> R.string.exception_msg_get_data
                }
            }
            is OperationResult.ErrorType.Network -> R.string.exception_msg_network
            //is OperationResult.ErrorType.Validation -> R.string.exception_msg_validation
            is OperationResult.ErrorType.Unknown -> R.string.exception_msg_common
        }
    }
}