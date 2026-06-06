package ru.itis.feature.emergency.impl.ui.sossettings.mvi

import androidx.annotation.StringRes

internal sealed interface SosSettingsEffect {
    data class Message(@StringRes val message: Int) : SosSettingsEffect
}