package ru.itis.feature.map.impl.ui.mvi

import androidx.annotation.StringRes

internal sealed interface MapScreenEffect {
    data class Message(@StringRes val message: Int) : MapScreenEffect
}