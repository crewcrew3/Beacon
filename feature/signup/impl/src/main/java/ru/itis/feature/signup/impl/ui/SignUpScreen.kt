package ru.itis.feature.signup.impl.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.core.ui.BaseScreen
import ru.itis.core.ui.R
import ru.itis.core.ui.component.InputFieldCustom
import ru.itis.core.ui.component.PrimaryButtonCustom
import ru.itis.core.ui.component.settings.ButtonSettings
import ru.itis.core.ui.component.settings.IconSettings
import ru.itis.core.ui.component.settings.InputFieldSettings
import ru.itis.core.ui.component.settings.TopBarSettings
import ru.itis.core.ui.model.AuthFormState
import ru.itis.core.ui.theme.BeaconTheme
import ru.itis.core.ui.theme.DimensionsCustom
import ru.itis.core.ui.theme.StylesCustom
import ru.itis.feature.signup.impl.ui.mvi.SignUpScreenEffect
import ru.itis.feature.signup.impl.ui.mvi.SignUpScreenEvent
import ru.itis.feature.signup.impl.ui.mvi.SignUpScreenState

@Composable
internal fun SignUpScreen(
    previewFormState: AuthFormState = AuthFormState()
) {
    //val formState = previewFormState //для превью

    val viewModel: SignUpViewModel = hiltViewModel()
    val pageState by viewModel.pageState.collectAsState(initial = SignUpScreenState.Initial)
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current

    var isErrorNicknameInput by remember { mutableStateOf(false) }
    var isErrorPhoneNumberInput by remember { mutableStateOf(false) }
    var isErrorPasswordInput by remember { mutableStateOf(false) }
    var isErrorRepeatPasswordInput by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.pageEffect.collect { effect ->
            when (effect) {
                is SignUpScreenEffect.Message -> Toast.makeText(context, context.getText(effect.message), Toast.LENGTH_SHORT).show()
                is SignUpScreenEffect.FormErrors -> {
                    isErrorNicknameInput = effect.isNicknameError
                    isErrorPhoneNumberInput = effect.isPhoneNumberError
                    isErrorPasswordInput = effect.isPasswordError
                    isErrorRepeatPasswordInput = effect.isRepeatPasswordError

                }
            }
        }
    }

    BaseScreen(
        topBarSettings = TopBarSettings(
            text = stringResource(R.string.top_bar_header_sign_up)
        ),
        topBarIconSettings = IconSettings(
            onClick = {
                viewModel.processEvent(SignUpScreenEvent.OnBackBtnClick)
            }
        )
    ) { innerPadding ->
        LazyColumn (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
        ) {
            item {
                Spacer(Modifier.height(DimensionsCustom.authFirstSpacerHeight))

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.registration_screen_header),
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = StylesCustom.h1,
                        fontSize = 24.sp,
                    )
                }

                Spacer(Modifier.height(56.dp))

                InputFieldCustom(
                    inputFieldSettings = InputFieldSettings(
                        startValue = formState.nickname,
                        labelText = stringResource(R.string.label_nickname),
                        onValueChange = {
                            viewModel.processEvent(
                                SignUpScreenEvent.OnFormFieldChanged(
                                    nickname = it
                                )
                            )
                        },
                        isError = isErrorNicknameInput,
                        errorText = stringResource(R.string.nickname_regex),
                    ),
                )

                Spacer(Modifier.height(DimensionsCustom.spaceInputFields))

                InputFieldCustom(
                    inputFieldSettings = InputFieldSettings(
                        startValue = formState.phoneNumber,
                        labelText = stringResource(R.string.label_phone_number),
                        onValueChange = {
                            viewModel.processEvent(
                                SignUpScreenEvent.OnFormFieldChanged(
                                    phoneNumber = it
                                )
                            )
                        },
                        isError = isErrorPhoneNumberInput,
                        errorText = stringResource(R.string.phone_num_regex),
                    ),
                )

                Spacer(Modifier.height(DimensionsCustom.spaceInputFields))

                InputFieldCustom(
                    inputFieldSettings = InputFieldSettings(
                        startValue = formState.password,
                        labelText = stringResource(R.string.label_pass),
                        onValueChange = {
                            viewModel.processEvent(
                                SignUpScreenEvent.OnFormFieldChanged(
                                    password = it
                                )
                            )
                        },
                        isError = isErrorPasswordInput,
                        errorText = stringResource(R.string.password_name_regex),
                        isPasswordField = true
                    ),
                )

                Spacer(Modifier.height(DimensionsCustom.spaceInputFields))

                InputFieldCustom(
                    inputFieldSettings = InputFieldSettings(
                        startValue = formState.repeatPassword,
                        labelText = stringResource(R.string.label_repeat_pass),
                        onValueChange = {
                            viewModel.processEvent(
                                SignUpScreenEvent.OnFormFieldChanged(
                                    repeatPassword = it
                                )
                            )
                        },
                        isError = isErrorRepeatPasswordInput,
                        errorText = stringResource(R.string.repeat_password_error),
                        isPasswordField = true,
                    ),
                )

                Spacer(Modifier.height(48.dp))

                PrimaryButtonCustom(
                    buttonSettings = ButtonSettings(
                        text = stringResource(R.string.btn_save_text),
                        onClick = {
                            viewModel.processEvent(
                                SignUpScreenEvent.OnSignUpBtnClick(
                                    nickname = formState.nickname,
                                    phoneNumber = formState.phoneNumber,
                                    password = formState.password,
                                    repeatPassword = formState.repeatPassword
                                )
                            )
                        },
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
internal fun SignUpScreenPreview() {
    BeaconTheme {
        SignUpScreen()
    }
}