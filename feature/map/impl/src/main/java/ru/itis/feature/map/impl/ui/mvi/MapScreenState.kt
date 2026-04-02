package ru.itis.feature.map.impl.ui.mvi

internal sealed interface MapScreenState {
    data object Initial : MapScreenState
    data object Loading : MapScreenState
}