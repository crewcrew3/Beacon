package ru.itis.feature.map.impl.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.itis.feature.map.impl.ui.mvi.MapScreenEffect
import ru.itis.feature.map.impl.ui.mvi.MapScreenEvent
import ru.itis.feature.map.impl.ui.mvi.MapScreenState
import ru.itis.navigation.api.BottomBarNavigator
import javax.inject.Inject

@HiltViewModel
internal class MapScreenViewModel @Inject constructor(
    private val bottomBarNavigator: BottomBarNavigator,
) : ViewModel() {

    private val _pageState = MutableStateFlow<MapScreenState>(value = MapScreenState.Initial)
    val pageState = _pageState.asStateFlow()

    private val _pageEffect = MutableSharedFlow<MapScreenEffect>()
    val pageEffect = _pageEffect.asSharedFlow()

    fun processEvent(event: MapScreenEvent) {
        when (event) {
            is MapScreenEvent.OnProfileBottomBarClick -> bottomBarNavigator.toProfileScreen()
        }
    }
}