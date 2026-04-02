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
import ru.itis.feature.profile.impl.domain.usecase.CheckIsUserAuthUseCase
import ru.itis.feature.profile.impl.domain.usecase.GetProfileUseCase
import ru.itis.feature.profile.impl.domain.usecase.LogOutUseCase
import ru.itis.feature.profile.impl.ui.mvi.ProfileScreenEffect
import ru.itis.feature.profile.impl.ui.mvi.ProfileScreenEvent
import ru.itis.feature.profile.impl.ui.mvi.ProfileScreenState
import ru.itis.navigation.api.BottomBarNavigator
import ru.itis.navigation.api.ProfileNavigator

import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val checkIsUserAuthUseCase: CheckIsUserAuthUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val profileNavigator: ProfileNavigator,
    private val bottomBarNavigator: BottomBarNavigator,
    private val exceptionHandler: ExceptionHandler,
) : ViewModel() {

    private val _pageState = MutableStateFlow<ProfileScreenState>(value = ProfileScreenState.Initial)
    val pageState = _pageState.asStateFlow()

    private val _pageEffect = MutableSharedFlow<ProfileScreenEffect>()
    val pageEffect = _pageEffect.asSharedFlow()

    fun processEvent(event: ProfileScreenEvent) {
        when (event) {
            is ProfileScreenEvent.OnInitProfile -> {
                checkIsUserAuth()
            }
            is ProfileScreenEvent.OnLogOutTabClick -> logOutUser()

            is ProfileScreenEvent.OnLogInBtnClick -> profileNavigator.toLogInScreen()
            is ProfileScreenEvent.OnSignUpBtnClick -> profileNavigator.toSighUpScreen()
            is ProfileScreenEvent.OnMapBottomBarClick -> bottomBarNavigator.toMapScreen()
        }
    }

    private fun checkIsUserAuth() {
        viewModelScope.launch {
            runCatching {
                checkIsUserAuthUseCase()
            }.onSuccess { isAuth ->
                if (isAuth) {
                    getUserProfile()
                } else {
                    _pageState.value = ProfileScreenState.Unauthorized
                }
            }.onFailure { exception ->
                val messageResId = exceptionHandler.handleExceptionMessage(exception.message)
                _pageEffect.emit(
                    ProfileScreenEffect.Message(
                        message = messageResId
                    )
                )
            }
        }
    }

    private fun getUserProfile() {
        viewModelScope.launch {
            runCatching {
                _pageState.value = ProfileScreenState.Loading
                delay(4000) //ну чтобы шиммеры красиво туда-сюда типа профиль загружается
                getProfileUseCase()
            }.onSuccess { result ->
                _pageState.value = ProfileScreenState.ProfileResult(result = result)
            }.onFailure { exception ->
                val messageResId = exceptionHandler.handleExceptionMessage(exception.message)
                _pageEffect.emit(
                    ProfileScreenEffect.Message(
                        message = messageResId
                    )
                )
            }
        }
    }

    private fun logOutUser() {
        viewModelScope.launch {
            runCatching {
                logOutUseCase()
            }.onSuccess {
                _pageEffect.emit(
                    ProfileScreenEffect.Message(R.string.toast_msg_logout_successful)
                )
                profileNavigator.back()
            }.onFailure { exception ->
                val messageResId = exceptionHandler.handleExceptionMessage(exception.message)
                _pageEffect.emit(
                    ProfileScreenEffect.Message(
                        message = messageResId
                    )
                )
            }
        }
    }
}