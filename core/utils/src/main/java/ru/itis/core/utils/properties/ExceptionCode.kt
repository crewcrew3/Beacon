package ru.itis.core.utils.properties

object ExceptionCode {

    const val GET_PROFILE_ERROR = "get_profile_error"
    const val GET_INCIDENT_ERROR = "get_incident_error"

    const val WRONG_CREDENTIALS = "wrong_credentials_error"
    const val USER_ALREADY_EXISTS = "user_already_exists"

    const val UNAUTHORIZED = "unauthorized_error"

    const val BOUNDS_ERROR = "Bounds must contain 4 values: [minLat, maxLat, minLng, maxLng]"

    const val UNKNOWN_ERROR = "unauthorized_error"
}