package ru.itis.feature.map.impl.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ru.itis.feature.map.impl.R
import ru.itis.feature.map.impl.databinding.FragmentMapScreenBinding

class MapScreenFragment : Fragment(R.layout.fragment_map_screen) {

    private var viewBinding: FragmentMapScreenBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding = FragmentMapScreenBinding.bind(view)

        viewBinding?.apply {

        }
    }

    override fun onDestroy() {
        viewBinding = null
        super.onDestroy()
    }
}