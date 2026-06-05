package ru.itis.feature.emergency.impl.ui.mvi

import androidx.annotation.StringRes

internal sealed interface EmergencyScreenEffect {
    data class Message(@StringRes val message: Int) : EmergencyScreenEffect
}