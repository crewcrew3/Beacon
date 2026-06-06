package ru.itis.feature.emergency.impl.ui.sossettings.mvi

internal sealed interface SosSettingsEvent {
    data class OnMessageChange(val message: String) : SosSettingsEvent
    data class OnNewContactInputChange(val input: String) : SosSettingsEvent
    data object OnAddContact : SosSettingsEvent
    data class OnDeleteContact(val phoneNumber: String) : SosSettingsEvent
    data object OnSave : SosSettingsEvent
    data object OnBack : SosSettingsEvent
}