package ru.itis.feature.map.impl.ui.mvi

internal sealed interface MapScreenEvent {
    data object OnProfileBottomBarClick : MapScreenEvent
}