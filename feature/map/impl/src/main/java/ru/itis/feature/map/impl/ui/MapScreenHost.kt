package ru.itis.feature.map.impl.ui

import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.core.ui.BaseScreen
import ru.itis.core.ui.component.settings.BottomBarSettings
import ru.itis.feature.map.impl.ui.mvi.MapScreenEffect
import ru.itis.feature.map.impl.ui.mvi.MapScreenEvent
import ru.itis.feature.map.impl.ui.mvi.MapScreenState

@Composable
internal fun MapScreenHost() {
    val context = LocalContext.current
    val fragmentManager = (context as AppCompatActivity).supportFragmentManager
    val containerId = remember { View.generateViewId() }

    val viewModel: MapScreenViewModel = hiltViewModel()
    val pageState by viewModel.pageState.collectAsState(initial = MapScreenState.Initial)

    LaunchedEffect(Unit) {
        if (fragmentManager.findFragmentById(containerId) == null) {
            fragmentManager.beginTransaction()
                .replace(containerId, MapScreenFragment::class.java, null)
                .commit()
        }

        viewModel.pageEffect.collect { effect ->
            when (effect) {
                is MapScreenEffect.Message -> Toast.makeText(context, context.getText(effect.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    BaseScreen(
        bottomBarSettings = BottomBarSettings(
            onMapClick = { }, //мы уже тут
            onProfileClick = {
                viewModel.processEvent(
                    MapScreenEvent.OnProfileBottomBarClick
                )
            },
        )
    ) {
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply { id = containerId }
            },
        )
    }
}