package ru.itis.feature.emergency.impl.ui.emergency.mvi

import androidx.annotation.StringRes

internal sealed interface EmergencyScreenEffect {
    data class Message(@StringRes val message: Int) : EmergencyScreenEffect
}