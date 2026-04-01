package ru.itis.feature.map.impl.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.feature.map.impl.R
import ru.itis.feature.map.impl.databinding.FragmentMapScreenBinding

@AndroidEntryPoint
class MapScreenFragment : Fragment(R.layout.fragment_map_screen) {

    private var _viewBinding: FragmentMapScreenBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var mapView: MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        }
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
    }
}