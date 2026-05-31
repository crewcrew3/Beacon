package ru.itis.core.domain.model.safety

data class LightingPoleModel(
    val globalId: Long,
    val pillarNumber: String?,
    val pillarType: String?,
    val lightsNumber: Int?,
    val status: String?,
    val latitude: Double,
    val longitude: Double,
    val district: String?
) {
    fun toPoint() = com.yandex.mapkit.geometry.Point(latitude, longitude)
}