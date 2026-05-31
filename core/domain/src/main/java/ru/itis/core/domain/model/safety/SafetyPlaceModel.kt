package ru.itis.core.domain.model.safety

data class SafetyPlaceModel(
    val id: Long,
    val name: String,
    val type: SafetyPlaceType,
    val latitude: Double,
    val longitude: Double,
    val address: String?
) {
    fun toPoint() = com.yandex.mapkit.geometry.Point(latitude, longitude)
}