package ru.itis.feature.emergency.impl.ui.sossettings.mvi

internal sealed interface SosSettingsState {
    data object Loading : SosSettingsState
    data class Content(
        val message: String,
        val contacts: List<ContactItem>,
        val newContactInput: String
    ) : SosSettingsState
}

data class ContactItem(
    val phone: String
)