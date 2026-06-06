package ru.itis.feature.profile.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.itis.core.ui.R
import ru.itis.core.utils.ExceptionHandler
import ru.itis.core.utils.OperationResult
import ru.itis.feature.profile.impl.domain.usecase.CheckIsUserAuthUseCase
import ru.itis.feature.profile.impl.domain.usecase.GetProfileUseCase
import ru.itis.feature.profile.impl.domain.usecase.LogOutUseCase
import ru.itis.feature.profile.impl.ui.mvi.ProfileScreenEffect
import ru.itis.feature.profile.impl.ui.mvi.ProfileScreenEvent
import ru.itis.feature.profile.impl.ui.mvi.ProfileScreenState
import ru.itis.navigation.api.BottomBarNavigator
import ru.itis.navigation.api.EmergencyNavigator
import ru.itis.navigation.api.ProfileNavigator

import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val checkIsUserAuthUseCase: CheckIsUserAuthUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val profileNavigator: ProfileNavigator,
    private val bottomBarNavigator: BottomBarNavigator,
    private val emergencyNavigator: EmergencyNavigator,
    private val exceptionHandler: ExceptionHandler,
) : ViewModel() {

    private val _pageState = MutableStateFlow<ProfileScreenState>(value = ProfileScreenState.Initial)
    val pageState = _pageState.asStateFlow()

    private val _pageEffect = MutableSharedFlow<ProfileScreenEffect>()
    val pageEffect = _pageEffect.asSharedFlow()

    fun processEvent(event: ProfileScreenEvent) {
        when (event) {
            is ProfileScreenEvent.OnInitProfile -> {
                getUserProfile()
            }
            is ProfileScreenEvent.OnLogOutTabClick -> logOutUser()

            is ProfileScreenEvent.OnLogInBtnClick -> profileNavigator.toLogInScreen()
            is ProfileScreenEvent.OnSignUpBtnClick -> profileNavigator.toSighUpScreen()
            is ProfileScreenEvent.OnMapBottomBarClick -> bottomBarNavigator.toMapScreen()
            is ProfileScreenEvent.OnEmergencyToolsClick -> emergencyNavigator.toEmergencyScreen()
        }
    }

    private fun getUserProfile() {
        viewModelScope.launch {
            _pageState.value = ProfileScreenState.Loading
            delay(2000) // чтобы шиммеры красиво туда-сюда типа профиль загружается
            if (checkIsUserAuthUseCase()) {
                when (val result = getProfileUseCase()) {
                    is OperationResult.Success -> {
                        _pageState.value = ProfileScreenState.ProfileResult(result = result.data)
                    }

                    is OperationResult.Error -> {
                        val messageResId = exceptionHandler.getErrorMessage(result.errorType)
                        _pageEffect.emit(
                            ProfileScreenEffect.Message(message = messageResId)
                        )
                        // В случае ошибки показываем неавторизованное состояние
                        _pageState.value = ProfileScreenState.Unauthorized
                    }
                }
            } else {
                _pageState.value = ProfileScreenState.Unauthorized
            }
        }
    }

    private fun logOutUser() {
        viewModelScope.launch {
            when (val result = logOutUseCase()) {
                is OperationResult.Success -> {
                    _pageEffect.emit(
                        ProfileScreenEffect.Message(R.string.toast_msg_logout_successful)
                    )
                    profileNavigator.back()
                }
                is OperationResult.Error -> {
                    val messageResId = exceptionHandler.getErrorMessage(result.errorType)
                    _pageEffect.emit(
                        ProfileScreenEffect.Message(message = messageResId)
                    )
                }
            }
        }
    }
}