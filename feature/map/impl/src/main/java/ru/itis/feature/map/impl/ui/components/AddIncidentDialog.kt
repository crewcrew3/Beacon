package ru.itis.feature.map.impl.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.core.ui.theme.BeaconTheme
import ru.itis.core.ui.theme.StylesCustom
import ru.itis.core.ui.R

/**
 * Диалог для выбора типа инцидента и ввода описания при добавлении новой метки.
 *
 * @param onDismissRequest вызывается при отмене добавления
 * @param onConfirm вызывается при подтверждении с выбранным типом и описанием
 */
@Composable
internal fun AddIncidentDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (IncidentType, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(IncidentType.OTHER) }
    var description by remember { mutableStateOf("") }
    val incidentTypes = IncidentType.entries.toTypedArray()

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.add_incident_dialog_header),
                    style = StylesCustom.h4,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.add_incident_dialog_type_header),
                    style = StylesCustom.body2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    incidentTypes.forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected)
                                BorderStroke(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            else
                                null
                        ) {
                            Text(
                                text = type.name.replace("_", " "),
                                style = StylesCustom.body3,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Поле описания
                Text(
                    text = stringResource(R.string.add_incident_dialog_details_header),
                    style = StylesCustom.body2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    placeholder = { Text(stringResource(R.string.add_incident_dialog_details_placeholder)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedType, description.takeIf { it.isNotBlank() }) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.btn_save_text), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
internal fun AddIncidentDialogPreview() {
    BeaconTheme {
        AddIncidentDialog(
            onDismissRequest = {},
            onConfirm = { _, _ -> }
        )
    }
}