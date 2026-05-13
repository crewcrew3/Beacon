package ru.itis.feature.map.impl.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import ru.itis.core.ui.component.InputFieldCustom
import ru.itis.core.ui.component.PrimaryButtonCustom
import ru.itis.core.ui.component.settings.ButtonSettings
import ru.itis.core.ui.component.settings.InputFieldSettings
import ru.itis.core.ui.theme.DimensionsCustom

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
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(DimensionsCustom.roundedCorners),
            color = MaterialTheme.colorScheme.surfaceBright
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.add_incident_dialog_header),
                    style = StylesCustom.dialogHeader,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.add_incident_dialog_type_header),
                    style = StylesCustom.basicBodyTextStart,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    incidentTypes.forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(DimensionsCustom.roundedCornersSmall),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected)
                                BorderStroke(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                )
                            else
                                null
                        ) {
                            Text(
                                text = type.name.replace("_", " "),
                                style = StylesCustom.basicBodySubTextCenter,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.add_incident_dialog_details_header),
                    style = StylesCustom.basicBodyTextStart,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                InputFieldCustom(
                    inputFieldSettings = InputFieldSettings(
                        placeholderText = stringResource(R.string.add_incident_dialog_details_placeholder),
                        startValue = description,
                        onValueChange = { description = it },
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        Text(
                            text =stringResource(R.string.btn_cancel),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = StylesCustom.basicBodySubTextCenter
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    PrimaryButtonCustom(
                        buttonSettings = ButtonSettings(
                            onClick = { onConfirm(selectedType, description.takeIf { it.isNotBlank() }) },
                            text = stringResource(R.string.btn_save_text),
                            textStyle = StylesCustom.basicBodySubTextCenter
                        )
                    )
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