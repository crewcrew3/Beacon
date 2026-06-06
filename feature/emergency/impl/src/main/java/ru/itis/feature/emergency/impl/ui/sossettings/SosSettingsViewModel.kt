package ru.itis.feature.emergency.impl.ui.sossettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.itis.core.domain.model.emergency.SosSettingsModel
import ru.itis.core.ui.R
import ru.itis.core.utils.ExceptionHandler
import ru.itis.core.utils.OperationResult
import ru.itis.feature.emergency.impl.domain.usecase.GetSosSettingsUseCase
import ru.itis.feature.emergency.impl.domain.usecase.SaveSosSettingsUseCase
import ru.itis.feature.emergency.impl.ui.sossettings.mvi.ContactItem
import ru.itis.feature.emergency.impl.ui.sossettings.mvi.SosSettingsEffect
import ru.itis.feature.emergency.impl.ui.sossettings.mvi.SosSettingsEvent
import ru.itis.feature.emergency.impl.ui.sossettings.mvi.SosSettingsState
import ru.itis.navigation.api.EmergencyNavigator
import javax.inject.Inject

@HiltViewModel
internal class SosSettingsViewModel @Inject constructor(
    private val getSosSettingsUseCase: GetSosSettingsUseCase,
    private val saveSosSettingsUseCase: SaveSosSettingsUseCase,
    private val emergencyNavigator: EmergencyNavigator,
    private val exceptionHandler: ExceptionHandler,
) : ViewModel() {

    private val _pageState = MutableStateFlow<SosSettingsState>(SosSettingsState.Loading)
    val pageState = _pageState.asStateFlow()

    private val _pageEffect = MutableSharedFlow<SosSettingsEffect>()
    val pageEffect = _pageEffect.asSharedFlow()

    // Текущие значения в памяти для редактирования
    private var currentMessage: String = ""
    private var currentContacts: List<ContactItem> = emptyList()
    private var newContactInput: String = ""

    init {
        // Подписываемся на изменения настроек из базы
        viewModelScope.launch {
            getSosSettingsUseCase().collect { settings ->
                currentMessage = settings.message
                currentContacts = settings.trustedContacts.map { phone ->
                    ContactItem(phone = phone)
                }
                updateState()
            }
        }
    }

    fun processEvent(event: SosSettingsEvent) {
        when (event) {
            is SosSettingsEvent.OnMessageChange -> {
                currentMessage = event.message
                updateState()
            }
            is SosSettingsEvent.OnNewContactInputChange -> {
                // Разрешаем только цифры и символ +
                val filtered = event.input.filter { it.isDigit() || it == '+' }
                newContactInput = filtered
                updateState()
            }
            is SosSettingsEvent.OnAddContact -> addContact()
            is SosSettingsEvent.OnDeleteContact -> deleteContact(event.phoneNumber)
            is SosSettingsEvent.OnSave -> saveSettings()
            is SosSettingsEvent.OnBack -> emergencyNavigator.back()
        }
    }

    private fun addContact() {
        val phone = newContactInput.trim()

        // Валидация российского номера
        if (!isValidRussianPhone(phone)) {
            viewModelScope.launch {
                _pageEffect.emit(SosSettingsEffect.Message(R.string.sos_settings_error_invalid_phone))
            }
            return
        }

        // Проверка на дубликаты
        if (currentContacts.any { it.phone == phone }) {
            viewModelScope.launch {
                _pageEffect.emit(SosSettingsEffect.Message(R.string.sos_settings_error_duplicate_phone))
            }
            return
        }

        currentContacts = currentContacts + ContactItem(phone = phone)
        newContactInput = ""
        updateState()
    }

    private fun deleteContact(phone: String) {
        currentContacts = currentContacts.filter { it.phone != phone }
        updateState()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val settings = SosSettingsModel(
                message = currentMessage,
                trustedContacts = currentContacts.map { it.phone }
            )
            when (val result = saveSosSettingsUseCase(settings)) {
                is OperationResult.Success -> {
                    _pageEffect.emit(SosSettingsEffect.Message(R.string.sos_settings_saved))
                    emergencyNavigator.back()
                }
                is OperationResult.Error -> {
                    val messageResId = exceptionHandler.getErrorMessage(result.errorType)
                    _pageEffect.emit(SosSettingsEffect.Message(messageResId))
                }
            }
        }
    }

    // Валидация российского номера: +7XXXXXXXXXX или 8XXXXXXXXXX
    private fun isValidRussianPhone(phone: String): Boolean {
        val regex = Regex("^(\\+7|8)\\d{10}$")
        return regex.matches(phone)
    }

    private fun updateState() {
        _pageState.value = SosSettingsState.Content(
            message = currentMessage,
            contacts = currentContacts,
            newContactInput = newContactInput
        )
    }
}