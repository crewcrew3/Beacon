package ru.itis.feature.map.impl.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CircleMapObject
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectVisitor
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolygonMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.runtime.image.ImageProvider
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.core.ui.R
import androidx.core.graphics.createBitmap
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.LineStyle
import com.yandex.mapkit.map.MapObjectTapListener
import ru.itis.core.domain.model.route.RouteSafetyOverlay
import ru.itis.core.ui.theme.ColorsCustom

/**
 * Метод для отрисовки коллекции инцидентов на карте.
 * Использует один ImageProvider для всех меток одного типа (это эффективнее).
 * Разные типы инцидентов отображаются разными иконками.
 */
internal class MapIncidentRenderer(
    private val context: Context,
    private val mapObjects: MapObjectCollection,
    private val onIncidentClicked: (IncidentModel) -> Unit
) {

    // Маркеры точек маршрута (начальная/конечная)
    private var startPointMarker: PlacemarkMapObject? = null
    private var endPointMarker: PlacemarkMapObject? = null

    // Кэш иконок для маркеров маршрута
    private val routePointIconCache = mutableMapOf<RoutePointType, ImageProvider>()

    enum class RoutePointType {
        START,  // начальная точка
        END     // конечная точка
    }

    // Полилиния маршрута
    private var routePolyline: PolylineMapObject? = null

    // Маркеры безопасности (для очистки)
    private val safetyMarkerIds = mutableSetOf<String>()

    // Кэш иконок для объектов безопасности
    private val safetyIconCache = mutableMapOf<SafetyOverlayType, ImageProvider>()

    private enum class SafetyOverlayType {
        SAFETY_PLACE,   // островок безопасности
        SAFETY_CAMERA,  // камера
        LIGHTING_POLE   // опора освещения
    }

    // Кэш ImageProvider для каждого типа инцидента
    private val iconCache = mutableMapOf<String, ImageProvider>()

    // Храним слушатели тапов с сильными ссылками, ключ = incident.id
    private val tapListeners = mutableMapOf<Long, MapObjectTapListener>()

    /**
     * Очищает все существующие метки инцидентов и отрисовывает новые.
     * Вызывается при получении обновлённого списка из ViewModel.
     */
    fun renderIncidents(incidents: List<IncidentModel>) {
        Log.i("RENDER_INCIDENT_DEBUG", "Renderer: renderIncidents called with ${incidents.size} incidents")
        // Удаляем все предыдущие объекты и кэш слушателей
        mapObjects.clear()
        tapListeners.clear()

        incidents.forEach { incident ->
            // Пропускаем архивированные - они не должны отображаться
            if (incident.status == IncidentStatus.ARCHIVED) return@forEach
            incident.id?.let { incidentId ->
                Log.i(
                    "RENDER_INCIDENT_DEBUG",
                    "Renderer: Adding placemark #${incidentId} at (${incident.latitude}, ${incident.longitude}), status=${incident.status}"
                )

                val tapListener = MapObjectTapListener { mapObject, point ->
                    Log.i("TAP_INCIDENT_DEBUG", "Placemark tapped: id=$incidentId, type=${incident.type}")
                    onIncidentClicked(incident)
                    true // событие обработано, не передавать дальше
                }

                tapListeners[incidentId] = tapListener

                val placemark = mapObjects.addPlacemark().apply {
                    geometry = Point(incident.latitude, incident.longitude)
                    setIcon(getIconForIncident(incident.type, incident.status))
                    setIconStyle(
                        IconStyle().apply {
                            anchor = PointF(0.5f, 1.0f)
                            scale = 0.8f
                        }
                    )
                    // Сохраняем ID инцидента в тег для последующего получения при клике
                    userData = incidentId
                }

                placemark.addTapListener(tapListener)
            }
        }
    }

    /**
     * Возвращает или создаёт ImageProvider для заданного типа инцидента.
     */
    private fun getIconForIncident(type: IncidentType, status: IncidentStatus): ImageProvider {
        // Формируем уникальный ключ для кэша: "TYPE_STATUS"
        val cacheKey = "${type.name}_${status.name}"

        return iconCache.getOrPut(cacheKey) {
            val drawableResId = when (type) {
                IncidentType.POOR_LIGHTING -> {
                    if (status == IncidentStatus.VERIFIED) {
                        //R.drawable.ic_lighting_warning_verified
                        R.drawable.ic_placeholder
                    } else {
                        R.drawable.ic_lighting_warning
                    }
                }
                IncidentType.HARASSMENT -> {
                    if (status == IncidentStatus.VERIFIED) {
                        //R.drawable.ic_harassment_verified
                        R.drawable.ic_placeholder
                    } else {
                        R.drawable.ic_harassment
                    }
                }
                IncidentType.PICKPOCKETING -> {
                    if (status == IncidentStatus.VERIFIED) {
                        //R.drawable.ic_pickpocket_verified
                        R.drawable.ic_placeholder
                    } else {
                        R.drawable.ic_pickpocket
                    }
                }
                IncidentType.ROBBERY -> {
                    if (status == IncidentStatus.VERIFIED) {
                        //R.drawable.ic_robbery_verified
                        R.drawable.ic_placeholder
                    } else {
                        R.drawable.ic_robbery
                    }
                }
                IncidentType.SUSPICIOUS_PERSON -> {
                    if (status == IncidentStatus.VERIFIED) {
                        //R.drawable.ic_suspicious_verified
                        R.drawable.ic_placeholder
                    } else {
                        R.drawable.ic_suspicious
                    }
                }
                IncidentType.BAD_ROAD -> {
                    if (status == IncidentStatus.VERIFIED) {
                        //R.drawable.ic_bad_road_verified
                        R.drawable.ic_placeholder
                    } else {
                        R.drawable.ic_bad_road
                    }
                }
                IncidentType.OTHER -> {
                    if (status == IncidentStatus.VERIFIED) {
                        //R.drawable.ic_other_verified
                        R.drawable.ic_placeholder
                    } else {
                        R.drawable.ic_other
                    }
                }
            }
            // Конвертируем в Bitmap и создаём ImageProvider
            val bitmap = drawableToBitmap(drawableResId)
            ImageProvider.fromBitmap(bitmap)
        }
    }

    /**
     * Конвертирует Drawable (PNG/XML) в Bitmap для использования в ImageProvider.
     */
    private fun drawableToBitmap(drawableResId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableResId)
            ?: throw IllegalArgumentException("Drawable not found: $drawableResId")

        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96

        drawable.setBounds(0, 0, width, height)

        val bitmap = createBitmap(width, height)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.draw(canvas)

        return bitmap
    }

    /**
     * Обновляет визуальный стиль существующей метки после изменения статуса.
     */
    fun updateIncidentStatus(incidentId: Long, incidentType: IncidentType, newStatus: IncidentStatus) {
        // Флаг для остановки обхода после нахождения нужной метки
        var found = false

        mapObjects.traverse(object : MapObjectVisitor {
            override fun onPlacemarkVisited(placemark: PlacemarkMapObject) {
                // Проверяем, что userData содержит нужный ID и что это ещё не та метка, которую ищем
                val userDataId = placemark.userData as? Long

                if (!found && placemark.userData == incidentId) {
                    // Заменяем иконку на соответствующую новому статусу
                    placemark.setIcon(getIconForIncident(incidentType, newStatus))
                    found = true
                }
            }
            // Остальные типы объектов на карте нам не интересны — оставляем пустые реализации
            override fun onPolylineVisited(polyline: PolylineMapObject) {}
            override fun onPolygonVisited(polygon: PolygonMapObject) {}
            override fun onCircleVisited(circle: CircleMapObject) {}
            override fun onCollectionVisitStart(collection: MapObjectCollection): Boolean = true
            override fun onCollectionVisitEnd(collection: MapObjectCollection) {}
            override fun onClusterizedCollectionVisitStart(collection: ClusterizedPlacemarkCollection): Boolean = true
            override fun onClusterizedCollectionVisitEnd(collection: ClusterizedPlacemarkCollection) {}
        })
    }

    /**
     * Добавляет одну новую метку на карту без перерисовки всех остальных.
     */
    fun addSingleIncident(incident: IncidentModel) {
        // Не добавляем архивированные
        if (incident.status == IncidentStatus.ARCHIVED) return

        incident.id?.let { incidentId ->
            val tapListener = MapObjectTapListener { mapObject, point ->
                Log.i("TAP_INCIDENT_DEBUG", "New placemark tapped: id=$incidentId")
                onIncidentClicked(incident)
                true
            }

            tapListeners[incidentId] = tapListener

            val placemark = mapObjects.addPlacemark().apply {
                geometry = Point(incident.latitude, incident.longitude)
                setIcon(getIconForIncident(incident.type, incident.status))
                setIconStyle(
                    IconStyle().apply {
                        anchor = PointF(0.5f, 1.0f)
                        scale = 0.8f
                    }
                )
                userData = incidentId
            }

            placemark.addTapListener(tapListener)
        }
    }

    /**
     * Отрисовывает безопасный маршрут на карте.
     * @param polyline список координат маршрута
     * @param riskScore оценка риска (0.0-1.0) для цвета линии
     */
    fun drawSafeRoute(polyline: List<Point>, riskScore: Float) {
        // Удаляем предыдущий маршрут если есть
        routePolyline?.let { mapObjects.remove(it) }

        if (polyline.size < 2) return

        // Цвет линии зависит от риска: зелёный (безопасно) -> жёлтый -> красный (опасно)
        val routeColor = when {
            riskScore < 0.3f -> ColorsCustom.SafeRouteSafe
            riskScore < 0.6f -> ColorsCustom.SafeRouteMid
            else -> ColorsCustom.SafeRouteDanger
        }

        routePolyline = mapObjects.addPolyline().apply {
            geometry = Polyline(polyline)
            setStrokeColor(routeColor.copy(alpha = 0.7f).toArgb())
            style = LineStyle().apply {
                strokeWidth = 8f
                outlineColor = routeColor.toArgb()
            }
        }
    }

    /**
     * Отрисовывает объекты безопасности вдоль маршрута.
     */
    fun drawSafetyOverlay(overlay: RouteSafetyOverlay) {
        // Очищаем предыдущие маркеры безопасности
        clearSafetyMarkers()

        // Рисуем островки безопасности
        overlay.safetyPlaces.forEach { place ->
            val markerId = "safety_place_${place.id}"
            addSafetyMarker(
                latitude = place.latitude,
                longitude = place.longitude,
                type = SafetyOverlayType.SAFETY_PLACE,
                userData = markerId
            )
            safetyMarkerIds.add(markerId)
        }

        // Рисуем камеры
        overlay.safetyCameras.forEach { camera ->
            val markerId = "safety_camera_${camera.globalId}"
            addSafetyMarker(
                latitude = camera.latitude,
                longitude = camera.longitude,
                type = SafetyOverlayType.SAFETY_CAMERA,
                userData = markerId
            )
            safetyMarkerIds.add(markerId)
        }

        // Рисуем опоры освещения
        overlay.lightingPoles.forEach { pole ->
            val markerId = "lighting_pole_${pole.globalId}"
            addSafetyMarker(
                latitude = pole.latitude,
                longitude = pole.longitude,
                type = SafetyOverlayType.LIGHTING_POLE,
                userData = markerId
            )
            safetyMarkerIds.add(markerId)
        }
    }

    /**
     * Вспомогательный метод для добавления маркера безопасности.
     */
    private fun addSafetyMarker(
        latitude: Double,
        longitude: Double,
        type: SafetyOverlayType,
        userData: String
    ) {
        val placemark = mapObjects.addPlacemark().apply {
            geometry = Point(latitude, longitude)
            setIcon(getSafetyIcon(type))
            setIconStyle(
                IconStyle().apply {
                    anchor = PointF(0.5f, 1.0f)
                    scale = 0.8f
                }
            )
            this.userData = userData
        }
    }

    /**
     * Возвращает или создаёт ImageProvider для типа объекта безопасности.
     */
    private fun getSafetyIcon(type: SafetyOverlayType): ImageProvider {
        return safetyIconCache.getOrPut(type) {
            val drawableResId = when (type) {
                SafetyOverlayType.SAFETY_PLACE -> R.drawable.ic_safety_place
                SafetyOverlayType.SAFETY_CAMERA -> R.drawable.ic_safety_camera
                SafetyOverlayType.LIGHTING_POLE -> R.drawable.ic_lighting_pole
            }
            val bitmap = drawableToBitmap(drawableResId)
            ImageProvider.fromBitmap(bitmap)
        }
    }

    /**
     * Отрисовывает маркер начальной или конечной точки маршрута.
     */
    fun drawRoutePointMarker(
        latitude: Double,
        longitude: Double,
        type: RoutePointType,
    ) {

        // Удаляем предыдущий маркер этого типа
        when (type) {
            RoutePointType.START -> startPointMarker?.let { mapObjects.remove(it) }
            RoutePointType.END -> endPointMarker?.let { mapObjects.remove(it) }
        }

        val marker = mapObjects.addPlacemark().apply {
            geometry = Point(latitude, longitude)
            setIcon(getRoutePointIcon(type))
            setIconStyle(
                IconStyle().apply {
                    anchor = PointF(0.5f, 1.0f)
                    scale = 1.0f
                }
            )
            userData = "route_point_${type.name}"
        }

        when (type) {
            RoutePointType.START -> startPointMarker = marker
            RoutePointType.END -> endPointMarker = marker
        }
    }

    /**
     * Возвращает или создаёт ImageProvider для типа точки маршрута.
     */
    private fun getRoutePointIcon(type: RoutePointType): ImageProvider {
        return routePointIconCache.getOrPut(type) {
            val drawableResId = when (type) {
                RoutePointType.START -> R.drawable.ic_route_start
                RoutePointType.END -> R.drawable.ic_route_end
            }
            val bitmap = drawableToBitmap(drawableResId)
            ImageProvider.fromBitmap(bitmap)
        }
    }

    /**
     * Очищает маркеры точек маршрута.
     */
    fun clearRoutePointMarkers() {
        startPointMarker?.let { mapObjects.remove(it) }
        endPointMarker?.let { mapObjects.remove(it) }
        startPointMarker = null
        endPointMarker = null
    }

    /**
     * Очищает только маркеры безопасности (по userData-тегу).
     */
    private fun clearSafetyMarkers() {
        mapObjects.traverse(object : MapObjectVisitor {
            override fun onPlacemarkVisited(placemark: PlacemarkMapObject) {
                val userData = placemark.userData as? String
                if (userData != null && safetyMarkerIds.contains(userData)) {
                    mapObjects.remove(placemark)
                }
            }
            override fun onPolylineVisited(polyline: PolylineMapObject) {}
            override fun onPolygonVisited(polygon: PolygonMapObject) {}
            override fun onCircleVisited(circle: CircleMapObject) {}
            override fun onCollectionVisitStart(collection: MapObjectCollection): Boolean = true
            override fun onCollectionVisitEnd(collection: MapObjectCollection) {}
            override fun onClusterizedCollectionVisitStart(collection: ClusterizedPlacemarkCollection): Boolean = true
            override fun onClusterizedCollectionVisitEnd(collection: ClusterizedPlacemarkCollection) {}
        })
        safetyMarkerIds.clear()
    }

    /**
     * Очищает маршрут и все объекты безопасности.
     */
    fun clearRouteAndSafetyOverlay() {
        routePolyline?.let { mapObjects.remove(it) }
        routePolyline = null
        clearSafetyMarkers()
        clearRoutePointMarkers()
    }

    fun clearListeners() {
        tapListeners.clear()
    }
}