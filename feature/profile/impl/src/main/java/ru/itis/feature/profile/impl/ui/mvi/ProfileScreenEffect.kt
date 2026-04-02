package ru.itis.feature.profile.impl.ui.mvi

import androidx.annotation.StringRes

internal sealed interface ProfileScreenEffect {
    data class Message(@StringRes val message: Int) : ProfileScreenEffect
}