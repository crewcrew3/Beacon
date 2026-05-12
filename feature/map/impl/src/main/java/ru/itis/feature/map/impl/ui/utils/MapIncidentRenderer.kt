package ru.itis.feature.map.impl.ui.utils

import android.content.Context
import android.graphics.PointF
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

    /**
     * Очищает все существующие метки инцидентов и отрисовывает новые.
     * Вызывается при получении обновлённого списка из ViewModel.
     */
    fun renderIncidents(incidents: List<IncidentModel>) {
        // Удаляем все предыдущие объекты
        mapObjects.clear()

        incidents.forEach { incident ->
            // Пропускаем архивированные - они не должны отображаться
            if (incident.status == IncidentStatus.ARCHIVED) return@forEach

            val placemark = mapObjects.addPlacemark().apply {
                geometry = Point(incident.latitude, incident.longitude)

                // Устанавливаем иконку в зависимости от типа инцидента и статуса
                setIcon(getIconForIncident(incident.type, incident.status))

                // Настраиваем стиль: привязка к низу иконки, масштаб
                setIconStyle(
                    IconStyle().apply {
                        anchor = PointF(0.5f, 1.0f)
                        scale = 0.8f
                    }
                )

                // Сохраняем ID инцидента в тег для последующего получения при клике
                userData = incident.id
            }

            placemark.addTapListener { _, _ ->
                onIncidentClicked(incident)
                // Возвращаем true, чтобы событие не передавалось дальше (например, не срабатывал клик по карте)
                true
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
                        R.drawable.ic_lighting_warning
                    } else {
                        //R.drawable.ic_lighting_warning_gray
                        R.drawable.ic_placeholder //временно
                    }
                }
                IncidentType.HARASSMENT -> {
                    if (status == IncidentStatus.VERIFIED) {
                        R.drawable.ic_harassment
                    } else {
                        //R.drawable.ic_harassment_gray
                        R.drawable.ic_placeholder
                    }
                }
                IncidentType.PICKPOCKETING -> {
                    if (status == IncidentStatus.VERIFIED) {
                        R.drawable.ic_pickpocket
                    } else {
                        R.drawable.ic_placeholder
                        //R.drawable.ic_pickpocket_gray
                    }
                }
                IncidentType.ROBBERY -> {
                    if (status == IncidentStatus.VERIFIED) {
                        R.drawable.ic_robbery
                    } else {
                        R.drawable.ic_placeholder
                        //R.drawable.ic_robbery_gray
                    }
                }
                IncidentType.SUSPICIOUS_PERSON -> {
                    if (status == IncidentStatus.VERIFIED) {
                        R.drawable.ic_suspicious
                    } else {
                        R.drawable.ic_placeholder
                        //R.drawable.ic_suspicious_gray
                    }
                }
                IncidentType.BAD_ROAD -> {
                    if (status == IncidentStatus.VERIFIED) {
                        R.drawable.ic_bad_road
                    } else {
                        R.drawable.ic_placeholder
                        //R.drawable.ic_bad_road_gray
                    }
                }
                IncidentType.OTHER -> {
                    if (status == IncidentStatus.VERIFIED) {
                        R.drawable.ic_other
                    } else {
                        R.drawable.ic_placeholder
                        //R.drawable.ic_other_gray
                    }
                }
            }
            ImageProvider.fromResource(context, drawableResId)
        }
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

        val placemark = mapObjects.addPlacemark().apply {
            geometry = Point(incident.latitude, incident.longitude)
            setIcon(getIconForIncident(incident.type, incident.status))
            setIconStyle(
                IconStyle().apply {
                    anchor = PointF(0.5f, 1.0f)
                    scale = 0.8f
                }
            )
            userData = incident.id
        }

        placemark.addTapListener { _, _ ->
            onIncidentClicked(incident)
            true
        }
    }
}