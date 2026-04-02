package ru.itis.feature.login.impl.ui

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
import ru.itis.feature.login.impl.ui.mvi.LogInScreenEffect
import ru.itis.feature.login.impl.ui.mvi.LogInScreenEvent
import ru.itis.feature.login.impl.ui.mvi.LogInScreenState

@Composable
internal fun LogInScreen(
    previewFormState: AuthFormState = AuthFormState()
) {
    //val formState = previewFormState //для превью

    val viewModel: LogInViewModel = hiltViewModel()
    val pageState by viewModel.pageState.collectAsState(initial = LogInScreenState.Initial)
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.pageEffect.collect { effect ->
            when (effect) {
                is LogInScreenEffect.Message -> Toast.makeText(context, context.getText(effect.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    BaseScreen(
        topBarSettings = TopBarSettings(
            text = stringResource(R.string.top_bar_header_log_in)
        ),
        topBarIconSettings = IconSettings(
            onClick = {
                viewModel.processEvent(LogInScreenEvent.OnBackBtnClick)
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
                        text = stringResource(R.string.login_screen_header),
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = StylesCustom.h1,
                        fontSize = 24.sp,
                    )
                }

                Spacer(Modifier.height(56.dp))

                InputFieldCustom(
                    inputFieldSettings = InputFieldSettings(
                        startValue = formState.phoneNumber,
                        labelText = stringResource(R.string.label_phone_number),
                        onValueChange = {
                            viewModel.processEvent(
                                LogInScreenEvent.OnFormFieldChanged(
                                    phoneNumber = it
                                )
                            )
                        },
                    ),
                )

                Spacer(Modifier.height(DimensionsCustom.spaceInputFields))

                InputFieldCustom(
                    inputFieldSettings = InputFieldSettings(
                        startValue = formState.password,
                        labelText = stringResource(R.string.label_pass),
                        onValueChange = {
                            viewModel.processEvent(
                                LogInScreenEvent.OnFormFieldChanged(
                                    password = it
                                )
                            )
                        },
                        isPasswordField = true
                    ),
                )

                Spacer(Modifier.height(48.dp))

                PrimaryButtonCustom(
                    buttonSettings = ButtonSettings(
                        text = stringResource(R.string.btn_continue_text),
                        onClick = {
                            viewModel.processEvent(
                                LogInScreenEvent.OnLogInBtnClick(
                                    phoneNumber = formState.phoneNumber,
                                    password = formState.password,
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
internal fun LogInScreenPreview() {
    BeaconTheme {
        LogInScreen()
    }
}