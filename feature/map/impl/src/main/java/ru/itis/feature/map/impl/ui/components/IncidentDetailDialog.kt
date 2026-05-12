package ru.itis.feature.map.impl.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.domain.model.mark.IncidentStatus
import ru.itis.core.domain.model.mark.VerificationActionType
import ru.itis.core.ui.theme.StylesCustom
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
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Заголовок с типом инцидента
                Text(
                    text = incident.type.name.replace("_", " ").uppercase(),
                    style = StylesCustom.h4,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Статус бейдж
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (incident.status) {
                        IncidentStatus.VERIFIED -> MaterialTheme.colorScheme.tertiaryContainer
                        IncidentStatus.PENDING_VERIFICATION -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = incident.status.name.replace("_", " "),
                        style = StylesCustom.body3,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Описание
                incident.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        text = desc,
                        style = StylesCustom.body2,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Мета-информация
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Создатель: #${incident.creatorNickname}",
                        style = StylesCustom.body3,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Время: ${incident.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}",
                        style = StylesCustom.body3,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "✓ ${incident.confirmCount}",
                            style = StylesCustom.body3,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "✗ ${incident.disputeCount}",
                            style = StylesCustom.body3,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Кнопки голосования (только если метка ещё не архивирована)
                if (incident.status != IncidentStatus.ARCHIVED) {
                    Text(
                        text = "Ваш голос:",
                        style = StylesCustom.body2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onVerify(VerificationActionType.CONFIRM) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text("Подтвердить", color = MaterialTheme.colorScheme.onTertiary)
                        }
                        Button(
                            onClick = { onVerify(VerificationActionType.DISPUTE) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Оспорить", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                } else {
                    Text(
                        text = "Метка архивирована и больше не участвует в верификации.",
                        style = StylesCustom.body3,
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
                    Text("Закрыть", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}