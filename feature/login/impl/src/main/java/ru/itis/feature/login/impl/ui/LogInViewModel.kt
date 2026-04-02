package ru.itis.feature.login.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.core.ui.R
import ru.itis.core.ui.model.AuthFormState
import ru.itis.core.utils.ExceptionHandler
import ru.itis.feature.login.impl.domain.usecase.LogInUserUseCase
import ru.itis.feature.login.impl.ui.mvi.LogInScreenEffect
import ru.itis.feature.login.impl.ui.mvi.LogInScreenEvent
import ru.itis.feature.login.impl.ui.mvi.LogInScreenState
import ru.itis.navigation.api.ProfileNavigator
import javax.inject.Inject

@HiltViewModel
internal class LogInViewModel @Inject constructor(
    private val logInUserUseCase: LogInUserUseCase,
    private val profileNavigator: ProfileNavigator,
    private val exceptionHandler: ExceptionHandler,
) : ViewModel() {

    private val _pageState = MutableStateFlow<LogInScreenState>(value = LogInScreenState.Initial)
    val pageState = _pageState.asStateFlow()

    private val _formState = MutableStateFlow(value = AuthFormState())
    val formState = _formState.asStateFlow()

    private val _pageEffect = MutableSharedFlow<LogInScreenEffect>()
    val pageEffect = _pageEffect.asSharedFlow()

    fun processEvent(event: LogInScreenEvent) {
        when (event) {
            is LogInScreenEvent.OnLogInBtnClick -> logInUser(
                phoneNumber = event.phoneNumber,
                password = event.password,
            )

            is LogInScreenEvent.OnFormFieldChanged -> {
                _formState.update { state ->
                    state.copy(
                        password = event.password ?: state.password,
                        phoneNumber = event.phoneNumber ?: state.phoneNumber,
                    )
                }
            }
            is LogInScreenEvent.OnBackBtnClick -> profileNavigator.back()
        }
    }

    private fun logInUser(
        phoneNumber: String,
        password: String,
    ) {
        viewModelScope.launch {
            runCatching {
                logInUserUseCase(
                    phoneNumber = phoneNumber,
                    password = password,
                )
            }.onSuccess {
                _pageEffect.emit(
                    LogInScreenEffect.Message(
                        message = R.string.toast_msg_login_successful
                    )
                )
                profileNavigator.back()
            }.onFailure { exception ->
                val messageResId = exceptionHandler.handleExceptionMessage(exception.message)
                _pageEffect.emit(
                    LogInScreenEffect.Message(
                        message = messageResId
                    )
                )
            }
        }
    }
}