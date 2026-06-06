package ru.itis.feature.emergency.impl.ui.sossettings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.core.ui.BaseScreen
import ru.itis.core.ui.R
import ru.itis.core.ui.component.DividerCustom
import ru.itis.core.ui.component.InputFieldCustom
import ru.itis.core.ui.component.PrimaryButtonCustom
import ru.itis.core.ui.component.settings.ButtonSettings
import ru.itis.core.ui.component.settings.IconSettings
import ru.itis.core.ui.component.settings.InputFieldSettings
import ru.itis.core.ui.component.settings.TopBarSettings
import ru.itis.core.ui.theme.IconsCustom
import ru.itis.core.ui.theme.StylesCustom
import ru.itis.feature.emergency.impl.ui.sossettings.mvi.SosSettingsEffect
import ru.itis.feature.emergency.impl.ui.sossettings.mvi.SosSettingsEvent
import ru.itis.feature.emergency.impl.ui.sossettings.mvi.SosSettingsState

@Composable
internal fun SosSettingsScreen() {
    val viewModel: SosSettingsViewModel = hiltViewModel()
    val pageState by viewModel.pageState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.pageEffect.collect { effect ->
            when (effect) {
                is SosSettingsEffect.Message -> {
                    Toast.makeText(context, context.getText(effect.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BaseScreen(
        topBarSettings = TopBarSettings(text = stringResource(R.string.sos_settings_title)),
        topBarIconSettings = IconSettings(
            icon = IconsCustom.arrowBackIcon(),
            onClick = { viewModel.processEvent(SosSettingsEvent.OnBack) }
        )
    ) { innerPadding ->
        when (val state = pageState) {
            is SosSettingsState.Loading -> {
                // Состояние загрузки
            }
            is SosSettingsState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    // Секция сообщения
                    item {
                        Text(
                            text = stringResource(R.string.sos_settings_message_label),
                            style = StylesCustom.inputFieldLabel,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        InputFieldCustom(
                            inputFieldSettings = InputFieldSettings(
                                placeholderText = stringResource(R.string.sos_settings_message_placeholder),
                                startValue = state.message,
                                onValueChange = { viewModel.processEvent(SosSettingsEvent.OnMessageChange(it)) }
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        DividerCustom()
                        Spacer(modifier = Modifier.height(24.dp))

                        // Секция контактов
                        Text(
                            text = stringResource(R.string.sos_settings_contacts_label),
                            style = StylesCustom.inputFieldLabel,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.sos_settings_contacts_hint),
                            style = StylesCustom.basicBodySubTextStart,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Список добавленных контактов
                    items(state.contacts, key = { it.phone }) { contact ->
                        ContactItemRow(
                            phone = contact.phone,
                            onDelete = { viewModel.processEvent(SosSettingsEvent.OnDeleteContact(contact.phone)) }
                        )
                    }

                    // Секция добавления нового контакта и кнопка сохранения
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InputFieldCustom(
                                inputFieldSettings = InputFieldSettings(
                                    placeholderText = stringResource(R.string.sos_settings_phone_placeholder),
                                    startValue = state.newContactInput,
                                    onValueChange = { viewModel.processEvent(SosSettingsEvent.OnNewContactInputChange(it)) },
                                )
                            )
                            PrimaryButtonCustom(
                                buttonSettings = ButtonSettings(
                                    text = stringResource(R.string.sos_settings_btn_add),
                                    onClick = { viewModel.processEvent(SosSettingsEvent.OnAddContact) }
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        PrimaryButtonCustom(
                            buttonSettings = ButtonSettings(
                                text = stringResource(R.string.sos_settings_btn_save),
                                onClick = { viewModel.processEvent(SosSettingsEvent.OnSave) }
                            )
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// Компонент для отображения одного контакта в списке
@Composable
private fun ContactItemRow(phone: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = phone,
            style = StylesCustom.basicBodyTextStart,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = IconsCustom.deleteIcon(),
                contentDescription = stringResource(R.string.sos_settings_delete_contact),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}