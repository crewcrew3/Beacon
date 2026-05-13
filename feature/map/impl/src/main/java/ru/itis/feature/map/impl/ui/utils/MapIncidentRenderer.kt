package ru.itis.feature.map.impl.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
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
import com.yandex.mapkit.map.MapObjectTapListener

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

    fun clearListeners() {
        tapListeners.clear()
    }
}