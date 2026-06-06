package ru.itis.feature.emergency.impl.ui.emergency

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.core.ui.BaseScreen
import ru.itis.core.ui.R
import ru.itis.core.ui.component.PrimaryButtonCustom
import ru.itis.core.ui.component.settings.ButtonSettings
import ru.itis.core.ui.component.settings.IconSettings
import ru.itis.core.ui.component.settings.TopBarSettings
import ru.itis.core.ui.theme.IconsCustom
import ru.itis.core.ui.theme.StylesCustom
import ru.itis.feature.emergency.impl.ui.emergency.mvi.EmergencyScreenEffect
import ru.itis.feature.emergency.impl.ui.emergency.mvi.EmergencyScreenEvent
import ru.itis.feature.emergency.impl.ui.emergency.mvi.EmergencyScreenState

@Composable
internal fun EmergencyScreen() {
    val viewModel: EmergencyViewModel = hiltViewModel()
    val pageState by viewModel.pageState.collectAsState()
    val context = LocalContext.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] == true
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val phoneStateGranted = permissions[Manifest.permission.READ_PHONE_STATE] == true

        if (smsGranted && locationGranted && phoneStateGranted) {
            // Если разрешения получены, отправляем SOS
            viewModel.processEvent(EmergencyScreenEvent.OnSosLongPress)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.emergency_sos_permissions_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.pageEffect.collect { effect ->
            when (effect) {
                is EmergencyScreenEffect.Message -> {
                    Toast.makeText(context, context.getText(effect.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BaseScreen(
        topBarSettings = TopBarSettings(
            text = ""
        ),
        topBarIconSettings = IconSettings(
            onClick = { viewModel.processEvent(EmergencyScreenEvent.OnNavigateBack)}
        )
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (pageState) {
                is EmergencyScreenState.Initial -> InitialContent(
                    onFakeCallClick = { viewModel.processEvent(EmergencyScreenEvent.OnFakeCallClick) },
                    onAlarmClick = { viewModel.processEvent(EmergencyScreenEvent.OnAlarmClick) },
                    onSosClick = {
                        val hasSms = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.SEND_SMS
                        ) == PackageManager.PERMISSION_GRANTED
                        val hasLocation = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val hasPhoneState = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.READ_PHONE_STATE
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasSms && hasLocation && hasPhoneState) {
                            viewModel.processEvent(EmergencyScreenEvent.OnSosLongPress)
                        } else {
                            // Запрашиваем разрешения
                            permissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.SEND_SMS,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.READ_PHONE_STATE
                                )
                            )
                        }
                    },
                    onSosSettingsClick = { viewModel.processEvent(EmergencyScreenEvent.OnSosSettingsClick) }
                )
                is EmergencyScreenState.FakeCallIncoming -> FakeCallIncomingContent(
                    onAnswer = { viewModel.processEvent(EmergencyScreenEvent.OnAnswerFakeCall) },
                    onDecline = { viewModel.processEvent(EmergencyScreenEvent.OnDeclineFakeCall) }
                )
                is EmergencyScreenState.FakeCallActive -> FakeCallActiveContent(
                    onEnd = { viewModel.processEvent(EmergencyScreenEvent.OnEndFakeCall) }
                )
                is EmergencyScreenState.AlarmActive -> AlarmActiveContent(
                    onStop = { viewModel.processEvent(EmergencyScreenEvent.OnStopAlarm) }
                )
            }
        }
    }
}

// Главный экран с выбором инструмента
@Composable
private fun InitialContent(
    onFakeCallClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onSosClick: () -> Unit,
    onSosSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.emergency_screen_title),
            style = StylesCustom.profileHeader,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Кнопка SOS с иконкой настроек справа
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryButtonCustom(
                buttonSettings = ButtonSettings(
                    text = stringResource(R.string.emergency_btn_sos),
                    onClick = onSosClick,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.weight(1f)
                )
            )

            IconButton(onClick = onSosSettingsClick) {
                Icon(
                    imageVector = IconsCustom.settingsIcon(),
                    contentDescription = stringResource(R.string.sos_settings_title),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }

        // Подсказка о долгом нажатии
        Text(
            text = stringResource(R.string.emergency_sos_hold),
            style = StylesCustom.basicBodySubTextCenter,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButtonCustom(
            buttonSettings = ButtonSettings(
                text = stringResource(R.string.emergency_screen_btn_fake_call),
                onClick = onFakeCallClick)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButtonCustom(
            buttonSettings = ButtonSettings(
                text = stringResource(R.string.emergency_screen_btn_siren_light),
                onClick = onAlarmClick,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        )
    }
}

// Имитация экрана входящего звонка
@Composable
private fun FakeCallIncomingContent(onAnswer: () -> Unit, onDecline: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Аватарка
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "М", style = StylesCustom.profileNickname, color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(R.string.emergency_fake_call_default_name), style = StylesCustom.profileHeader, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.weight(1f))

            // Кнопки ответа и сброса
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallButton(color = MaterialTheme.colorScheme.error, onClick = onDecline, text = stringResource(R.string.emergency_fake_call_btn_decline))
                CallButton(color = Color(0xFF4CAF50), onClick = onAnswer, text = stringResource(R.string.emergency_fake_call_btn_answer))
            }
        }
    }
}

// Экран активного разговора
@Composable
private fun FakeCallActiveContent(onEnd: () -> Unit) {

    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.emergency_fake_call_conversation_title), style = StylesCustom.profileHeader, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButtonCustom(
                buttonSettings = ButtonSettings(text = stringResource(R.string.emergency_fake_call_conversation_finish), onClick = onEnd, containerColor = MaterialTheme.colorScheme.error)
            )
        }
    }
}

// Экран активной тревоги
@Composable
private fun AlarmActiveContent(onStop: () -> Unit) {

    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.emergency_siren_title), style = StylesCustom.profileHeader, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButtonCustom(
                buttonSettings = ButtonSettings(
                    text = stringResource(R.string.emergency_siren_btn_off),
                    onClick = onStop,
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

// Локальный компонент для круглых кнопок звонка
@Composable
private fun CallButton(color: Color, onClick: () -> Unit, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (text == stringResource(R.string.emergency_fake_call_btn_decline)) "X" else "V", color = Color.White, style = StylesCustom.profileHeader)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, color = MaterialTheme.colorScheme.onSurface, style = StylesCustom.basicBodySubTextCenter)
    }
}