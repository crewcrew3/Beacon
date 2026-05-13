package ru.itis.feature.map.impl.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.IncidentType
import ru.itis.core.domain.model.mark.VerificationActionType
import ru.itis.core.ui.theme.BeaconTheme
import ru.itis.core.ui.theme.ColorsCustom
import ru.itis.core.ui.theme.DimensionsCustom
import ru.itis.core.ui.theme.StylesCustom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Диалог с детальной информацией об инциденте и возможностью проголосовать.
 *
 * @param incident инцидент для отображения
 * @param onDismissRequest вызывается при закрытии диалога
 * @param onVerify вызывается при голосовании (confirm/dispute)
 */
@Composable
internal fun IncidentDetailDialog(
    incident: IncidentModel,
    onDismissRequest: () -> Unit,
    onVerify: (VerificationActionType) -> Unit
) {
    Log.i("TAP_INCIDENT_DEBUG", "IncidentDetailDialog composed for incident id=${incident.id}")
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
                    text = incident.type.name.replace("_", " ").uppercase(),
                    style = StylesCustom.dialogHeader,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (incident.status) {
                        IncidentStatus.VERIFIED -> ColorsCustom.IncidentVerified
                        IncidentStatus.PENDING_VERIFICATION -> ColorsCustom.IncidentPendingVerification.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    Text(
                        text = incident.status.name.replace("_", " "),
                        style = StylesCustom.basicBodySubTextCenter,
                        color = when (incident.status) {
                            IncidentStatus.VERIFIED -> ColorsCustom.OnIncidentVerified
                            IncidentStatus.PENDING_VERIFICATION -> ColorsCustom.OnIncidentPendingVerification
                            else -> MaterialTheme.colorScheme.onSecondary
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Создатель: ${incident.creatorNickname}",
                        style = StylesCustom.basicBodyTextStart,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Время: ${incident.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}",
                        style = StylesCustom.basicBodyTextStart,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "✓ ${incident.confirmCount}",
                            style = StylesCustom.basicBodyTextStart,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "✗ ${incident.disputeCount}",
                            style = StylesCustom.basicBodyTextStart,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    incident.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            text = "Описание",
                            style = StylesCustom.basicBodySubTextStart,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Text(
                                text = desc,
                                style = StylesCustom.basicBodySubTextStart,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                if (incident.status != IncidentStatus.ARCHIVED) {
                    Text(
                        text = "Ваш голос:",
                        style = StylesCustom.basicBodySubTextCenter,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onVerify(VerificationActionType.CONFIRM) },
                            modifier = Modifier.fillMaxWidth(0.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text(
                                text = "Подтвердить",
                                color = MaterialTheme.colorScheme.onTertiary,
                                style = StylesCustom.basicBodySubTextCenter
                            )
                        }
                        Button(
                            onClick = { onVerify(VerificationActionType.DISPUTE) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "Оспорить",
                                color = MaterialTheme.colorScheme.onError,
                                style = StylesCustom.basicBodySubTextCenter
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Метка архивирована и больше не участвует в верификации.",
                        style = StylesCustom.basicBodySubTextStart,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                // Кнопка закрытия
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Закрыть",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = StylesCustom.basicBodySubTextCenter
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
internal fun IncidentDetailDialogPreview() {
    BeaconTheme {
        IncidentDetailDialog(
            incident = IncidentModel(
                creatorId = 1,
                creatorNickname = "crewcrew",
                latitude = 0.0,
                longitude = 0.0,
                type = IncidentType.SUSPICIOUS_PERSON,
                description = "Подозрительный дядька!!",
                createdAt = LocalDateTime.now(),
                status = IncidentStatus.VERIFIED,
                confirmCount = 3,
                disputeCount = 7,
            ),
            onDismissRequest = {},
            onVerify = { _ -> }
        )
    }
}