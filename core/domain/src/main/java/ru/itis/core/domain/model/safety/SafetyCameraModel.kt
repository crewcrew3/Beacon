package ru.itis.core.domain.model.safety

data class SafetyCameraModel(
    val globalId: Long,
    val address: String,
    val district: String?,
    val latitude: Double,
    val longitude: Double,
    val ovdAddress: String?
) {
    fun toPoint() = com.yandex.mapkit.geometry.Point(latitude, longitude)
}