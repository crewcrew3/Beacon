package ru.itis.feature.map.impl.domain.usecase

import android.util.Log
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.runtime.Error
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.route.RouteRequestModel
import ru.itis.core.domain.model.route.RouteSafetyOverlay
import ru.itis.core.domain.model.route.SafeRouteModel
import ru.itis.core.domain.model.route.SafeRouteResult
import ru.itis.core.domain.model.safety.LightingPoleModel
import ru.itis.core.domain.model.safety.SafetyCameraModel
import ru.itis.core.domain.model.safety.SafetyPlaceModel
import ru.itis.core.domain.qualifiers.IoDispatchers
import ru.itis.core.domain.repository.IncidentRepository
import ru.itis.core.domain.repository.LightingPoleRepository
import ru.itis.core.domain.repository.SafetyCameraRepository
import ru.itis.core.domain.repository.SafetyPlaceRepository
import ru.itis.core.utils.BusinessErrorCode
import ru.itis.core.utils.OperationResult
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.cos
import kotlin.math.sin

/**
 * UseCase для построения безопасного маршрута между двумя точками.
 *
 * Алгоритм:
 * 1. Запрашиваем НЕСКОЛЬКО альтернативных маршрутов у Яндекс.Карт
 * 2. Для каждого маршрута рассчитываем композитный скор безопасности:
 *    - Учитываем инциденты (отрицательный фактор)
 *    - Учитываем освещение, камеры, безопасные места (положительные факторы)
 * 3. Применяем формулу: totalScore = WEIGHT_DISTANCE * dist + WEIGHT_SAFETY * risk
 * 4. Возвращаем маршрут с НАИЛУЧШИМ (минимальным) скором
 *
 * Приоритет: безопасность ~70%, расстояние ~30%
 */
internal class BuildSafeRouteUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val safetyPlaceRepository: SafetyPlaceRepository,
    private val safetyCameraRepository: SafetyCameraRepository,
    private val lightingPoleRepository: LightingPoleRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher,
) {

    private lateinit var currentRouteListener: DrivingSession.DrivingRouteListener
    private var currentSession: DrivingSession? = null

    // Коэффициенты для расчёта итогового скора: безопасность важнее расстояния
    private companion object {
        const val WEIGHT_SAFETY = 0.7f      // 70% вес безопасности
        const val WEIGHT_DISTANCE = 0.3f    // 30% вес расстояния

        // Радиусы для поиска объектов вдоль маршрута (в метрах)
        const val INCIDENT_SEARCH_RADIUS = 100f    // инциденты учитываем в радиусе 100м
        const val SAFETY_OBJECTS_RADIUS = 150f     // безопасные объекты — 150м

        // Пороговые значения для нормализации
        const val MAX_ROUTE_DISTANCE_METERS = 10000f  // 10 км — условный максимум для нормализации
    }

    private data class RouteData(
        val polyline: List<Point>,
        val distance: Double,
        val time: Double
    )

    suspend operator fun invoke(
        request: RouteRequestModel,
    ): OperationResult<SafeRouteResult> {

        val routesData = try {
            fetchRoutesOnMainThread(request)
        } catch (e: Exception) {
            Log.e("BUILD_ROUTE", "Failed to fetch routes", e)
            return OperationResult.Error(OperationResult.ErrorType.Unknown(e))
        }

        if (routesData.isEmpty()) {
            return OperationResult.Error(
                OperationResult.ErrorType.Business(BusinessErrorCode.INVALID_BOUNDS)
            )
        }

        return withContext(dispatcher) {
            try {
                Log.i("BUILD_ROUTE", "Processing routes on thread: ${Thread.currentThread().name}")

                // 4. Для каждого маршрута параллельно считаем скор безопасности
                val routeScores = coroutineScope {
                    routesData.map { routeData ->
                        async { calculateRouteSafetyScore(routeData) }
                    }.awaitAll()
                }

                // 5. Выбираем маршрут с НАИЛУЧШИМ (минимальным) скором
                val bestScore = routeScores.minByOrNull { it.totalScore }
                val bestRouteData = bestScore?.routeData ?: routesData.first()

                val bestRouteModel = SafeRouteModel(
                    polyline = bestRouteData.polyline,
                    totalDistance = bestRouteData.distance,
                    estimatedTime = (bestRouteData.time / 60).toInt(),
                    riskScore = routeScores.firstOrNull { it.routeData == bestRouteData }?.riskScore ?: 0.5f
                )

                val overlay = bestScore?.let { score ->
                    RouteSafetyOverlay(
                        safetyPlaces = score.safetyPlaces,
                        safetyCameras = score.safetyCameras,
                        lightingPoles = score.lightingPoles
                    )
                }

                // 6. Возвращаем результат
                OperationResult.Success(
                    SafeRouteResult(
                        route = bestRouteModel,
                        safetyOverlay = overlay
                    )
                )

            } catch (e: Exception) {
                Log.e("BUILD_ROUTE", "!!! FATAL ERROR in BuildSafeRouteUseCase !!!", e)
                OperationResult.Error(OperationResult.ErrorType.Unknown(e))
            } finally {
                currentSession = null
            }
        }
    }

    private suspend fun fetchRoutesOnMainThread(request: RouteRequestModel): List<RouteData> {
        return withContext(Dispatchers.Main) {
            Log.i("BUILD_ROUTE", "Fetching routes on thread: ${Thread.currentThread().name}")

            // 1. Создаём точки для запроса маршрута
            val points = listOf(
                RequestPoint(
                    Point(request.startPoint.latitude, request.startPoint.longitude),
                    RequestPointType.WAYPOINT, null, null, null
                ),
                RequestPoint(
                    Point(request.endPoint.latitude, request.endPoint.longitude),
                    RequestPointType.WAYPOINT, null, null, null
                )
            )

            // 2. Запрашиваем НЕСКОЛЬКО альтернативных маршрутов
            val drivingOptions = DrivingOptions().apply {
                routesCount = 3
            }

            val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.ONLINE)
            Log.i("BUILD_ROUTE", "DrivingRouter created: $drivingRouter")

            // 3. Получаем список маршрутов от Яндекс.Карт
            val routes = suspendCancellableCoroutine { continuation ->

                //создаем лисенер
                currentRouteListener = object : DrivingSession.DrivingRouteListener {
                    override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                        Log.i("BUILD_ROUTE", "OnDrivingRoutes called. Received ${routes.size} routes")
                        continuation.resume(routes.toList())
                        currentSession = null
                    }

                    override fun onDrivingRoutesError(error: Error) {
                        Log.i("BUILD_ROUTE", "Error on receiving routes from yandex maps")
                        continuation.resumeWithException(RuntimeException("Routing error: $error"))
                        currentSession = null
                    }
                }
                Log.i("BUILD_ROUTE", "Listener created $currentRouteListener")

                try {
                    currentSession = drivingRouter.requestRoutes(
                        points,
                        drivingOptions,
                        VehicleOptions(),
                        currentRouteListener
                    )
                    Log.i("BUILD_ROUTE", "Driving router session created: $currentSession")

                    continuation.invokeOnCancellation {
                        currentSession?.cancel()
                        currentSession = null
                    }
                } catch (e: Exception) {
                    Log.e("BUILD_ROUTE", "Failed to request routes", e)
                    continuation.resumeWithException(e)
                    currentSession = null
                }
            }

            Log.i("BUILD_ROUTE", "Extracting route data on MAIN thread")
            routes.map { route ->
                RouteData(
                    polyline = route.geometry.points.map { point ->
                        Point(point.latitude, point.longitude)
                    },
                    distance = route.metadata.weight.distance.value,
                    time = route.metadata.weight.time.value
                )
            }
        }
    }

    /**
     * Рассчитывает композитный скор безопасности для одного маршрута.
     * Формула: totalScore = WEIGHT_DISTANCE * normalizedDistance + WEIGHT_SAFETY * normalizedRisk
     */
    private suspend fun calculateRouteSafetyScore(routeData: RouteData): RouteSafetyScore {
        Log.i("BUILD_ROUTE", "Processing route: ${routeData.polyline.size} points, thread: ${Thread.currentThread().name}")

        val polyline = routeData.polyline
        if (polyline.isEmpty()) {
            return RouteSafetyScore(routeData, Float.MAX_VALUE, 1.0f)
        }

        // Вычисляем bounding box для параллельных запросов
        val bbox = calculateBoundingBox(polyline, SAFETY_OBJECTS_RADIUS)
        Log.i("BUILD_ROUTE", "Calculated bounding box: ${bbox.southWest}, ${bbox.northEast}")

        // Загружаем все данные параллельно для производительности
        val incidentsDeferred = coroutineScope {
            Log.i("BUILD_ROUTE", "Loading incidents...")
            async {
                incidentRepository.getVisibleIncidents(
                    minLat = bbox.southWest.latitude,
                    maxLat = bbox.northEast.latitude,
                    minLng = bbox.southWest.longitude,
                    maxLng = bbox.northEast.longitude,
                )
            }
        }
        Log.i("BUILD_ROUTE", "Incidents deferred loaded")

        val safetyPlacesDeferred = coroutineScope {
            Log.i("BUILD_ROUTE", "Loading safety places...")
            async {
                withContext(Dispatchers.Main) {
                    safetyPlaceRepository.getSafetyPlacesAlongRoute(polyline, SAFETY_OBJECTS_RADIUS)
                }
            }
        }
        Log.i("BUILD_ROUTE", "Safety places deferred loaded")

        val camerasDeferred = coroutineScope {
            Log.i("BUILD_ROUTE", "Loading cameras...")
            async {
                safetyCameraRepository.getCamerasInBounds(
                    minLat = bbox.southWest.latitude,
                    maxLat = bbox.northEast.latitude,
                    minLng = bbox.southWest.longitude,
                    maxLng = bbox.northEast.longitude,
                )
            }
        }
        Log.i("BUILD_ROUTE", "Cameras deferred loaded")

        val polesDeferred = coroutineScope {
            Log.i("BUILD_ROUTE", "Loading poles...")
            async {
                lightingPoleRepository.getLightingPolesInBounds(
                    minLat = bbox.southWest.latitude,
                    maxLat = bbox.northEast.latitude,
                    minLng = bbox.southWest.longitude,
                    maxLng = bbox.northEast.longitude,
                )
            }
        }
        Log.i("BUILD_ROUTE", "Poles deferred loaded")

        val incidents = (incidentsDeferred.await() as? OperationResult.Success)?.data ?: emptyList()
        val safetyPlaces = (safetyPlacesDeferred.await() as? OperationResult.Success)?.data ?: emptyList()
        val cameras = (camerasDeferred.await() as? OperationResult.Success)?.data ?: emptyList()
        val lightingPoles = (polesDeferred.await() as? OperationResult.Success)?.data ?: emptyList()
        Log.i("BUILD_ROUTE", "incidents size: ${incidents.size}, safety places size: ${safetyPlaces.size}, cameras size: ${cameras.size}, poles size: ${lightingPoles.size}")

        // Рассчитываем компоненты риска
        val incidentRisk = calculateIncidentRisk(polyline, incidents)    // 0.0 (безопасно) .. 1.0 (опасно)
        val safetyBonus = calculateSafetyBonus(polyline, safetyPlaces, cameras, lightingPoles) // 0.0 .. 0.5

        // Итоговый риск: инциденты уменьшаем бонусами от безопасных объектов
        val rawRisk = (incidentRisk - safetyBonus).coerceIn(0.0f, 1.0f)

        // Нормализуем расстояние: 0.0 (короткий) .. 1.0 (очень длинный)
        val normalizedDistance = (routeData.distance / MAX_ROUTE_DISTANCE_METERS).coerceIn(0.0, 1.0)

        // Применяем веса
        val totalScore = WEIGHT_DISTANCE * normalizedDistance + WEIGHT_SAFETY * rawRisk
        Log.i("BUILD_ROUTE", "incidentRisk: $incidentRisk, safetyBonus: $safetyBonus, rawRisk: $rawRisk, normalizedDistance: $normalizedDistance, totalScore: $totalScore")

        return RouteSafetyScore(
            routeData = routeData,
            totalScore = totalScore.toFloat(),
            riskScore = rawRisk,
            safetyPlaces = safetyPlaces,
            safetyCameras = cameras,
            lightingPoles = lightingPoles
        )
    }

    /**
     * Рассчитывает риск от инцидентов вдоль маршрута.
     * Учитывает тип инцидента и его статус.
     */
    private fun calculateIncidentRisk(
        polyline: List<Point>,
        incidents: List<IncidentModel>
    ): Float {
        if (incidents.isEmpty()) return 0.0f

        var totalRisk = 0.0f
        var consideredCount = 0

        incidents.forEach { incident ->
            if (isPointNearPolyline(incident.latitude, incident.longitude, polyline, INCIDENT_SEARCH_RADIUS)) {
                // Вес риска зависит от типа инцидента
                val typeWeight = incident.type.riskWeight
                // Статус влияет на достоверность: VERIFIED = 1.0, PENDING = 0.5
                val statusWeight = when (incident.status) {
                    IncidentStatus.VERIFIED -> 1.0f
                    IncidentStatus.PENDING_VERIFICATION -> 0.5f
                    else -> 0.0f
                }
                totalRisk += typeWeight * statusWeight
                consideredCount++
            }
        }

        return if (consideredCount > 0) {
            (totalRisk / consideredCount).coerceIn(0.0f, 1.0f)
        } else 0.0f
    }

    /**
     * Рассчитывает "бонус безопасности" от наличия безопасных объектов вдоль маршрута.
     * Возвращает значение 0.0 .. 0.5 (максимальный бонус снижает риск наполовину).
     */
    private fun calculateSafetyBonus(
        polyline: List<Point>,
        safetyPlaces: List<SafetyPlaceModel>,
        cameras: List<SafetyCameraModel>,
        lightingPoles: List<LightingPoleModel>
    ): Float {
        var bonus = 0.0f

        // Безопасные места: +0.02 за каждый в радиусе
        safetyPlaces.forEach { place ->
            if (isPointNearPolyline(place.latitude, place.longitude, polyline, SAFETY_OBJECTS_RADIUS)) {
                bonus += 0.02f
            }
        }

        // Камеры: +0.015 за каждую
        cameras.forEach { camera ->
            if (isPointNearPolyline(camera.latitude, camera.longitude, polyline, SAFETY_OBJECTS_RADIUS)) {
                bonus += 0.015f
            }
        }

        // Активные опоры освещения: +0.01 за каждую
        lightingPoles.forEach { pole ->
            if (pole.status == "active" &&
                isPointNearPolyline(pole.latitude, pole.longitude, polyline, SAFETY_OBJECTS_RADIUS)) {
                bonus += 0.01f
            }
        }

        // Ограничиваем максимальный бонус
        return bonus.coerceAtMost(0.5f)
    }

    /**
     * Проверяет, находится ли точка в заданном радиусе от полилинии.
     * Упрощённая проверка: расстояние до ближайшей точки полилинии.
     */
    private fun isPointNearPolyline(
        lat: Double,
        lng: Double,
        polyline: List<Point>,
        radiusMeters: Float
    ): Boolean {
        val minDistance = polyline.minOf { routePoint ->
            calculateDistanceMeters(Point(lat, lng), routePoint)
        }
        return minDistance <= radiusMeters
    }

    /**
     * Расчёт расстояния между двумя точками в метрах (формула гаверсинусов).
     */
    private fun calculateDistanceMeters(p1: Point, p2: Point): Float {
        val earthRadius = 6371000f
        val dLat = Math.toRadians(p2.latitude - p1.latitude).toFloat()
        val dLng = Math.toRadians(p2.longitude - p1.longitude).toFloat()
        val a = sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                cos(Math.toRadians(p1.latitude).toFloat()) *
                cos(Math.toRadians(p2.latitude).toFloat()) *
                sin(dLng / 2) * kotlin.math.sin(dLng / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }

    /**
     * Вычисляет BoundingBox вокруг полилинии с учётом радиуса.
     * Учитывает искажение долготы в зависимости от широты.
     */
    private fun calculateBoundingBox(polyline: List<Point>, radiusMeters: Float): BoundingBox {
        Log.i("BUILD_ROUTE", "Calculate bounding box...")
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

        // Конвертация метров в градусы
        val latPadding = (radiusMeters / 111_000) * 2
        val avgLat = (minLat + maxLat) / 2
        val lngPadding = (radiusMeters / (111_000 * cos(Math.toRadians(avgLat)))) * 2

        return BoundingBox(
            Point(minLat - latPadding, minLng - lngPadding),
            Point(maxLat + latPadding, maxLng + lngPadding)
        )
    }

    /**
     * Внутренняя модель для хранения промежуточных расчётов.
     */
    private data class RouteSafetyScore(
        val routeData: RouteData,
        val totalScore: Float,  // чем меньше, тем лучше
        val riskScore: Float,
        val safetyPlaces: List<SafetyPlaceModel> = emptyList(),
        val safetyCameras: List<SafetyCameraModel> = emptyList(),
        val lightingPoles: List<LightingPoleModel> = emptyList()
    )
}