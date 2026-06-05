package ru.itis.core.utils

sealed class OperationResult<out T> {
    data class Success<T>(val data: T) : OperationResult<T>()
    data class Error(val errorType: ErrorType) : OperationResult<Nothing>()

    sealed class ErrorType {
        //data class Validation(val fieldErrors: Map<String, String>) : ErrorType()
        data class Business(val code: BusinessErrorCode) : ErrorType()
        data class Network(val message: String?) : ErrorType()
        data class Unknown(val throwable: Throwable?) : ErrorType()
    }
}

enum class BusinessErrorCode {
    // Auth errors
    USER_ALREADY_EXISTS,
    WRONG_CREDENTIALS,
    UNAUTHORIZED,
    USER_NOT_FOUND,
    USER_ID_NOT_FOUND,

    // Incident errors
    INCIDENT_NOT_FOUND,
    INVALID_BOUNDS,

    //Map
    ADDRESS_NOT_FOUND,
}