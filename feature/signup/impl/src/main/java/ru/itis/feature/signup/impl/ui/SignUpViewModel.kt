package ru.itis.feature.signup.impl.ui

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
import ru.itis.core.utils.ValidatorHelper
import ru.itis.feature.signup.impl.domain.usecase.SignUpUserUseCase
import ru.itis.feature.signup.impl.ui.mvi.SignUpScreenEffect
import ru.itis.feature.signup.impl.ui.mvi.SignUpScreenEvent
import ru.itis.feature.signup.impl.ui.mvi.SignUpScreenState
import ru.itis.navigation.api.ProfileNavigator
import javax.inject.Inject

@HiltViewModel
internal class SignUpViewModel @Inject constructor(
    private val signUpUserUseCase: SignUpUserUseCase,
    private val profileNavigator: ProfileNavigator,
    private val exceptionHandler: ExceptionHandler,
) : ViewModel() {

    private val _pageState = MutableStateFlow<SignUpScreenState>(value = SignUpScreenState.Initial)
    val pageState = _pageState.asStateFlow()

    private val _formState = MutableStateFlow(value = AuthFormState())
    val formState = _formState.asStateFlow()

    private val _pageEffect = MutableSharedFlow<SignUpScreenEffect>()
    val pageEffect = _pageEffect.asSharedFlow()

    fun processEvent(event: SignUpScreenEvent) {
        when (event) {
            is SignUpScreenEvent.OnSignUpBtnClick -> registerUser(
                nickname = event.nickname,
                phoneNumber = event.phoneNumber,
                password = event.password,
                repeatPassword = event.repeatPassword
            )

            is SignUpScreenEvent.OnFormFieldChanged -> {
                _formState.update { state ->
                    state.copy(
                        nickname = event.nickname ?: state.nickname,
                        password = event.password ?: state.password,
                        phoneNumber = event.phoneNumber ?: state.phoneNumber,
                        repeatPassword = event.repeatPassword ?: state.repeatPassword,
                    )
                }
            }
            is SignUpScreenEvent.OnBackBtnClick -> profileNavigator.back()
        }
    }

    private fun registerUser(
        nickname: String,
        phoneNumber: String,
        password: String,
        repeatPassword: String
    ) {
        viewModelScope.launch {
            if (validateRegisterForm(
                    nickname = nickname,
                    phoneNumber = phoneNumber,
                    password = password,
                    repeatPassword = repeatPassword,
                )
            ) {
                runCatching {
                    signUpUserUseCase(
                        nickname = nickname,
                        phoneNumber = phoneNumber,
                        password = password,
                        repeatPassword = repeatPassword
                    )
                }.onSuccess {
                    _pageEffect.emit(
                        SignUpScreenEffect.Message(
                            message = R.string.toast_msg_signup_successful
                        )
                    )
                    profileNavigator.back()
                }.onFailure { exception ->
                    val messageResId = exceptionHandler.handleExceptionMessage(exception.message)
                    _pageEffect.emit(
                        SignUpScreenEffect.Message(
                            message = messageResId
                        )
                    )
                }
            }
        }
    }

    private suspend fun validateRegisterForm(
        nickname: String,
        phoneNumber: String,
        password: String,
        repeatPassword: String
    ): Boolean {
        val isNicknameValid = ValidatorHelper.validateNickname(nickname)
        val isPhoneNumberValid = ValidatorHelper.validatePhoneNumber(phoneNumber)
        val isPasswordValid = ValidatorHelper.validatePassword(password)
        val isRepeatPasswordValid = (password == repeatPassword)

        _pageEffect.emit(
            SignUpScreenEffect.FormErrors(
                isNicknameError = !isNicknameValid,
                isPhoneNumberError = !isPhoneNumberValid,
                isPasswordError = !isPasswordValid,
                isRepeatPasswordError = !isRepeatPasswordValid,
            )
        )
        return isNicknameValid && isPasswordValid && isPhoneNumberValid && isRepeatPasswordValid
    }
}