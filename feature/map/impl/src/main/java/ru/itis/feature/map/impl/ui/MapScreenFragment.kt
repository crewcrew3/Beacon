package ru.itis.feature.map.impl.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.map.Map
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.feature.map.impl.R
import ru.itis.feature.map.impl.databinding.FragmentMapScreenBinding
import ru.itis.feature.map.impl.ui.mvi.MapScreenEffect
import ru.itis.feature.map.impl.ui.mvi.MapScreenEvent
import ru.itis.feature.map.impl.ui.mvi.MapScreenState
import ru.itis.feature.map.impl.ui.utils.MapIncidentRenderer
import ru.itis.feature.map.impl.ui.utils.MapScreenDelegate

@AndroidEntryPoint
class MapScreenFragment : Fragment(R.layout.fragment_map_screen) {

    /**
     * Делегат для передачи событий в ViewModel.
     * Устанавливается из Compose перед добавлением фрагмента.
     */
    lateinit var delegate: MapScreenDelegate

    private var _viewBinding: FragmentMapScreenBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var mapView: MapView
    private lateinit var incidentRenderer: MapIncidentRenderer

    //Сильная ссылка на слушатель предотвращает его удаление сборщиком мусора.
    private lateinit var mapInputListener: InputListener
    private lateinit var mapCameraListener: CameraListener
    private var isListenerRegistered = false

    /**
     * Метод для обновления списка инцидентов из Compose.
     * Вызывается, когда ViewModel получает новые данные.
     */
    fun renderIncidents(incidents: List<IncidentModel>) {
        if (::incidentRenderer.isInitialized) {
            incidentRenderer.renderIncidents(incidents)
        }
    }

    /** Метод для добавления одной метки из Compose. */
    fun addIncident(incident: IncidentModel) {
        if (::incidentRenderer.isInitialized) {
            incidentRenderer.addSingleIncident(incident)
        }
    }

    /** Метод для обновления статуса метки из Compose.*/
    fun updateIncidentStatus(incidentId: Long, incidentType: IncidentType, newStatus: IncidentStatus) {
        if (::incidentRenderer.isInitialized) {
            incidentRenderer.updateIncidentStatus(incidentId, incidentType, newStatus)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("MAP_DEBUG","MapFragment init")
        _viewBinding = FragmentMapScreenBinding.bind(view)

        viewBinding.apply {
            mapView = this.mapview
            mapView.mapWindow.map.move(
                CameraPosition(
                    Point(55.751225, 37.62954), //target
                    10.0f, //zoom
                    150.0f, //azimuth
                    0f //tilt
                )
            )

            // Инициализируем рендерер с коллбеком для клика по метке
            incidentRenderer = MapIncidentRenderer(
                context = requireContext(),
                mapObjects = mapView.mapWindow.map.mapObjects,
                onIncidentClicked = { incident ->
                    delegate.onIncidentClicked(incident)
                }
            )

            setupMapInputListener()
            setupMapCameraListener()
            loadInitialIncidents()
        }
    }

    private fun setupMapCameraListener() {
        mapCameraListener = CameraListener { map, cameraPosition, cameraUpdateReason, finished ->
            // Загружаем инциденты только когда камера остановилась (чтобы не спамить запросами)
            if (finished) {
                val visibleRegion = map.visibleRegion
                Log.i("RENDER_INCIDENT_DEBUG", "Camera stopped. Visible region: " +
                        "lat[${visibleRegion.bottomLeft.latitude}..${visibleRegion.topRight.latitude}], " +
                        "lng[${visibleRegion.bottomLeft.longitude}..${visibleRegion.topRight.longitude}]")
                // Отправляем границы видимой области в ViewModel
                delegate.onMapBoundsChanged(
                    minLat = visibleRegion.bottomLeft.latitude,
                    maxLat = visibleRegion.topRight.latitude,
                    minLng = visibleRegion.bottomLeft.longitude,
                    maxLng = visibleRegion.topRight.longitude
                )
            }
        }

        // Добавляем слушатель на карту
        mapView.mapWindow.map.addCameraListener(mapCameraListener)
        Log.i("MAP_DEBUG", "CameraListener successfully registered")
    }

    private fun setupMapInputListener() {
        if (isListenerRegistered) return

        mapInputListener = object : InputListener {
            override fun onMapTap(map: Map, point: Point) {
                Log.i("MAP_DEBUG", "Short tap on map on ${point.latitude}, ${point.longitude}")
                delegate.onMapTapped(point.latitude, point.longitude)
            }
            override fun onMapLongTap(map: Map, point: Point) {
                Log.i("MAP_DEBUG", "Long tap on map on ${point.latitude}, ${point.longitude}")
            }
        }

        // Флаг isListenerRegistered защищает от повторной регистрации при переходах между экранами.
        // mapView.post {} откладывает вызов до завершения layout-фазы View.
        mapView.post {
            mapView.mapWindow.map.addInputListener(mapInputListener)
            isListenerRegistered = true
            Log.i("MAP_DEBUG", "InputListener successfully registered")
        }
    }

    private fun loadInitialIncidents() {
        // Дефолтные границы (примерно Москва и область)
        val defaultBounds = doubleArrayOf(
            55.5,   // minLat
            56.0,   // maxLat
            37.3,   // minLng
            38.0    // maxLng
        )
        Log.i("RENDER_INCIDENT_DEBUG", "Sending initial bounds: $defaultBounds")
        delegate.onMapBoundsChanged(
            minLat = defaultBounds[0],
            maxLat = defaultBounds[1],
            minLng = defaultBounds[2],
            maxLng = defaultBounds[3]
        )
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
        isListenerRegistered = false
        // Очищаем кэш слушателей в рендерере
        if (::incidentRenderer.isInitialized) {
            incidentRenderer.clearListeners()
        }
        mapView.mapWindow.map.removeCameraListener(mapCameraListener)
    }
}