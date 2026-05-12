package ru.itis.feature.map.impl.ui.old

import ru.itis.feature.map.impl.ui.MapScreenViewModel

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.map.Map
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.feature.map.impl.R
import ru.itis.feature.map.impl.databinding.FragmentMapScreenBinding
import ru.itis.feature.map.impl.ui.mvi.MapScreenEffect
import ru.itis.feature.map.impl.ui.mvi.MapScreenEvent
import ru.itis.feature.map.impl.ui.mvi.MapScreenState
import ru.itis.feature.map.impl.ui.utils.MapIncidentRenderer

@AndroidEntryPoint
class MapScreenFragmentOld : Fragment(R.layout.fragment_map_screen) {

    private val viewModel: MapScreenViewModel by viewModels()

    private var _viewBinding: FragmentMapScreenBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var mapView: MapView
    private lateinit var incidentRenderer: MapIncidentRenderer


    // Сильная ссылка на слушатель предотвращает его удаление сборщиком мусора.
    // Native-часть MapKit может терять weak-ссылки на inline-объекты.
    private lateinit var mapInputListener: InputListener
    private var isListenerRegistered = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("VM_DEBUG", "ViewModel hashCode: ${viewModel.hashCode()}")
        Log.i("MAP_DEBUG","MapScreen init")

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

            //Инициализируем слушатель как поле класса, чтобы он не попал в GC.
            mapInputListener = object : InputListener {
                override fun onMapTap(map: Map, point: Point) {
                    Log.i("MAP_DEBUG", "Short tap on map on ${point.latitude}, ${point.longitude}")
                    viewModel.processEvent(
                        MapScreenEvent.OnMapTapped(point.latitude, point.longitude)
                    )
                }

                override fun onMapLongTap(map: Map, point: Point) {
                    Log.i("MAP_DEBUG", "Long tap on map on ${point.latitude}, ${point.longitude}")
                }
            }

            // Инициализируем рендерер с коллбеком для клика по метке
            incidentRenderer = MapIncidentRenderer(
                context = requireContext(),
                mapObjects = mapView.mapWindow.map.mapObjects,
                onIncidentClicked = { incident ->
                    viewModel.processEvent(MapScreenEvent.OnIncidentClicked(incident))
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pageEffect.collect { effect ->
                    when (effect) {
                        is MapScreenEffect.IncidentAdded -> {
                            incidentRenderer.addSingleIncident(effect.incident)
                        }

                        is MapScreenEffect.IncidentStatusUpdated -> {
                            incidentRenderer.updateIncidentStatus(
                                incidentId = effect.incidentId,
                                incidentType = effect.incidentType,
                                newStatus = effect.newStatus
                            )
                        }
                        // Остальные эффекты обрабатываются в MapScreenHost
                        else -> {}
                    }
                }
            }
        }

        // Подписка на состояние для первоначальной отрисовки
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pageState.collect { state ->
                    if (state is MapScreenState.IncidentsLoaded) {
                        incidentRenderer.renderIncidents(state.incidents)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
//        Регистрация слушателя после onStart() гарантирует, что MapKit завершил инициализацию.
//        Флаг isListenerRegistered защищает от повторной регистрации при переходах между экранами.
//        mapView.post {} откладывает вызов до завершения layout-фазы View.
        if (!isListenerRegistered) {
            mapView.post {
                mapView.mapWindow.map.addInputListener(mapInputListener)
                isListenerRegistered = true
                Log.i("MAP_DEBUG", "InputListener successfully registered")
            }
        }
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
        isListenerRegistered = false
    }
}