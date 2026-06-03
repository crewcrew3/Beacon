package ru.itis.data.impl.repository

import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.search.BusinessObjectMetadata
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.mapkit.uri.UriObjectMetadata
import com.yandex.runtime.Error
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.itis.core.domain.model.safety.SafetyPlaceModel
import ru.itis.core.domain.model.safety.SafetyPlaceType
import ru.itis.core.domain.repository.SafetyPlaceRepository
import ru.itis.core.utils.OperationResult
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.cos

/**
 * Реализация репозитория для "островков безопасности".
 * ВАЖНО: данные из Яндекс.Карт НЕ кэшируются и НЕ сохраняются.
 * Каждый запрос - прямой вызов API.
 */
internal class SafetyPlaceRepositoryImpl @Inject constructor() : SafetyPlaceRepository {

    // Создаём SearchManager один раз, но не храним результаты
    private val searchManager: SearchManager by lazy {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
    }

    override suspend fun getSafetyPlacesAlongRoute(
        routePolyline: List<Point>,
        radius: Float
    ): OperationResult<List<SafetyPlaceModel>> {
        return try {
            // Выполняем поиск для каждого типа безопасного места
            // Яндекс API не позволяет искать по кастомным категориям,
            // поэтому используем текстовые запросы
            val results = mutableListOf<SafetyPlaceModel>()

            // Поиск для каждого типа безопасного места
            results += searchByQuery("полиция", routePolyline, radius, SafetyPlaceType.POLICE)
            results += searchByQuery("аптека круглосуточно", routePolyline, radius, SafetyPlaceType.PHARMACY)
            results += searchByQuery("банкомат", routePolyline, radius, SafetyPlaceType.ATM)
            results += searchByQuery("магазин круглосуточно", routePolyline, radius, SafetyPlaceType.SHOP)
            results += searchByQuery("метро", routePolyline, radius, SafetyPlaceType.METRO)
            results += searchByQuery("парк сквер", routePolyline, radius, SafetyPlaceType.PARK)

            OperationResult.Success(results)
        } catch (e: Exception) {
            OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }
    }

    /**
     * Вспомогательный метод для выполнения одного поискового запроса.
     * Возвращает результаты, преобразованные в SafetyPlaceModel.
     */
    private suspend fun searchByQuery(
        query: String,
        routePolyline: List<Point>,
        radiusMeters: Float,
        type: SafetyPlaceType
    ): List<SafetyPlaceModel> {
        if (routePolyline.isEmpty()) return emptyList()

        return suspendCancellableCoroutine { continuation ->
            //параметры поиска
            val options = SearchOptions().apply {
                searchTypes = SearchType.BIZ.value or SearchType.GEO.value
                resultPageSize = 20 // ограничиваем количество результатов
            }

            // Создаём полилинию для поиска вдоль маршрута
            val polylineGeometry = Polyline(routePolyline)

            // 2. Создаём ограничивающий BoundingBox (для оптимизации поиска)
            val bbox = createMapKitBoundingBox(routePolyline, radiusMeters)
            val boundingGeometry = Geometry.fromBoundingBox(bbox)

            // Запускаем сессию поиска.
            /*При выполнении поиска вдоль маршрута MapKit SDK
            сортирует поисковые результаты в зависимости от
            расстояния найденных объектов до переданной
            полилинии и возвращает ближайшие*/
            val session = searchManager.submit(
                query,
                polylineGeometry,  // поиск вдоль полилинии
                boundingGeometry, // ограничивающая область (Geometry)
                options,
                object : Session.SearchListener {
                    override fun onSearchResponse(response: Response) {
                        val places = response.collection.children.mapNotNull { geoObject ->
                            geoObject.obj?.let { obj ->
                                obj.geometry.firstOrNull()?.point?.let { point ->

                                    val uriMetadata = obj.metadataContainer.getItem(
                                        UriObjectMetadata::class.java
                                    )
                                    val uniqueId = uriMetadata?.uris?.firstOrNull()?.toString()
                                        ?: "coord_${point.latitude}_${point.longitude}" // детерминированная строка из координат

                                    val businessMetadata = obj.metadataContainer.getItem(
                                        BusinessObjectMetadata::class.java
                                    )
                                    val toponymMetadata = obj.metadataContainer.getItem(
                                        ToponymObjectMetadata::class.java
                                    )

                                    SafetyPlaceModel(
                                        id = uniqueId,
                                        name = obj.name
                                            ?: businessMetadata?.name
                                            ?: toponymMetadata?.formerName
                                            ?: "Без названия",
                                        type = type,
                                        latitude = point.latitude,
                                        longitude = point.longitude,
                                        address = businessMetadata?.address?.formattedAddress
                                            ?: toponymMetadata?.address?.formattedAddress
                                    )
                                }
                            }
                        }
                        continuation.resume(places)
                    }

                    override fun onSearchError(error: Error) {
                        // В случае ошибки возвращаем пустой список, а не прерываем весь процесс
                        continuation.resume(emptyList())
                    }
                }
            )

            // Отменяем запрос, если корутина отменена
            continuation.invokeOnCancellation {
                session.cancel()
            }
        }
    }

    /**
     * Вычисляет bounding box вокруг полилинии маршрута с заданным радиусом.
     * Упрощённая реализация: берём min/max координаты всех точек + добавляем padding.
     */
    private fun createMapKitBoundingBox(polyline: List<Point>, radiusMeters: Float): BoundingBox {
        var minLat = Double.MAX_VALUE
        var maxLat = -Double.MAX_VALUE
        var minLng = Double.MAX_VALUE
        var maxLng = -Double.MAX_VALUE

        polyline.forEach { point ->
            minLat = minOf(minLat, point.latitude)
            maxLat = maxOf(maxLat, point.latitude)
            minLng = minOf(minLng, point.longitude)
            maxLng = maxOf(maxLng, point.longitude)
        }

        // Конвертируем метры в градусы (приблизительно: 1° ≈ 111 км по широте)
        val latPadding = (radiusMeters / 111_000) * 2
        val lngPadding = (radiusMeters / (111_000 * cos(Math.toRadians((minLat + maxLat) / 2)))) * 2

        val southWest = Point(minLat - latPadding, minLng - lngPadding)
        val northEast = Point(maxLat + latPadding, maxLng + lngPadding)

        return BoundingBox(southWest, northEast)
    }
}