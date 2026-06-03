package ru.itis.core.domain.model.safety

data class SafetyPlaceModel(
    val id: String,
    val name: String,
    val type: SafetyPlaceType,
    val latitude: Double,
    val longitude: Double,
    val address: String?
) {
    fun toPoint() = com.yandex.mapkit.geometry.Point(latitude, longitude)
}