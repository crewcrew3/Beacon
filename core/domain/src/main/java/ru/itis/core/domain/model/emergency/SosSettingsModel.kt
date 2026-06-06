package ru.itis.core.domain.model.emergency

data class SosSettingsModel(
    val message: String,
    val trustedContacts: List<String>
)