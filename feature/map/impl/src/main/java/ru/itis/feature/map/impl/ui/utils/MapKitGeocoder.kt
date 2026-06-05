package ru.itis.feature.map.impl.ui.utils

import android.util.Log
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import com.yandex.runtime.Error
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.itis.core.utils.BusinessErrorCode
import ru.itis.core.utils.OperationResult
import kotlin.coroutines.resume

/**
 * Минимальный геокодер на основе Яндекс.Карт Поиск.
 * Все вызовы API выполняются в Main потоке.
 */
internal class MapKitGeocoder {

    private val searchManager: SearchManager by lazy {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
    }

    /**
     * Преобразует текстовый адрес в координаты.
     * ВАЖНО: должен вызываться из Main потока (т.к. использует MapKit API).
     *
     * @param address текст адреса для геокодинга
     * @param searchBoundingBox ограничивающая область для поиска (оптимизация)
     * @return координаты первого найденного результата или ошибка
     */
    suspend fun geocode(
        address: String,
        searchBoundingBox: BoundingBox? = null,
    ): OperationResult<Point> {
        return suspendCancellableCoroutine { continuation ->
            val options = SearchOptions().apply {
                //ищем топонимы и организации
                searchTypes = SearchType.GEO.value or SearchType.BIZ.value
                resultPageSize = 3
            }

            val searchBoundingBoxSafe = searchBoundingBox?.let { searchBoundingBox } ?: DefaultSearchArea.MOSCOW_REGION

            // Если передана область — используем её, иначе поиск по всей видимой области
            val geometry = Geometry.fromBoundingBox(searchBoundingBoxSafe)

            val listener = object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val point = response.collection.children
                        .firstOrNull()?.obj?.geometry?.firstOrNull()?.point

                    if (point != null) {
                        Log.i("GEOCODER", "Geocoded '$address' -> Point(${point.latitude}, ${point.longitude})")
                        continuation.resume(OperationResult.Success(point))
                    } else {
                        Log.w("GEOCODER", "No results for address: $address")
                        continuation.resume(
                            OperationResult.Error(
                                OperationResult.ErrorType.Business(
                                    BusinessErrorCode.ADDRESS_NOT_FOUND
                                )
                            )
                        )
                    }
                }

                override fun onSearchError(error: Error) {
                    Log.e("GEOCODER", "Geocoding error for '$address': $error")
                    continuation.resume(
                        OperationResult.Error(
                            OperationResult.ErrorType.Unknown(
                                RuntimeException("Search error: $error")
                            )
                        )
                    )
                }
            }

            // Запускаем сессию поиска
            val session = searchManager.submit(address, geometry, options, listener)


            // Отменяем запрос при отмене корутины
            continuation.invokeOnCancellation {
                session.cancel()
            }
        }
    }
}

private object DefaultSearchArea {
    // Москва + ~50 км вокруг
    val MOSCOW_REGION = BoundingBox(
        Point(55.4, 37.0),   // юго-запад
        Point(56.1, 38.0)    // северо-восток
    )
}