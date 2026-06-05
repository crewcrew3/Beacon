package ru.itis.feature.map.impl.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import ru.itis.core.domain.model.route.RouteRequestModel
import ru.itis.core.ui.R
import ru.itis.core.ui.component.InputFieldCustom
import ru.itis.core.ui.theme.DimensionsCustom
import ru.itis.core.ui.theme.StylesCustom
import ru.itis.core.ui.component.settings.InputFieldSettings

/**
 * Панель для выбора начальной и конечной точки маршрута.
 */
@Composable
internal fun RouteSelectionPanel(
    startPoint: RouteRequestModel.PointData?,
    endPoint: RouteRequestModel.PointData?,
    isLoading: Boolean,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onBuildRouteClick: () -> Unit,
    onFinishRouteClick: () -> Unit,
    onSearchAddress: (String, Boolean) -> Unit
) {
    var startAddressInput by remember { mutableStateOf(startPoint?.address ?: "") }
    var endAddressInput by remember { mutableStateOf(endPoint?.address ?: "") }

    AnimatedVisibility(
        visible = !isCollapsed,
        enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(DimensionsCustom.roundedCorners),
            color = MaterialTheme.colorScheme.surfaceBright,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.route_selection_title),
                    style = StylesCustom.basicBodyTextCenter,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                InputFieldCustom(
                    inputFieldSettings = InputFieldSettings(
                        placeholderText = stringResource(R.string.route_start_placeholder),
                        startValue = startAddressInput,
                        onValueChange = { startAddressInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                )

                InputFieldCustom(
                    inputFieldSettings = InputFieldSettings(
                        placeholderText = stringResource(R.string.route_end_placeholder),
                        startValue = endAddressInput,
                        onValueChange = { endAddressInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Если точки уже есть - сразу строим маршрут
                            if (startPoint != null && endPoint != null) {
                                onBuildRouteClick()
                            }
                            // Если точек нет, но адреса введены - запускаем геокодинг
                            else {
                                if (startAddressInput.isNotBlank()) {
                                    onSearchAddress(startAddressInput, true)
                                }
                                if (endAddressInput.isNotBlank()) {
                                    onSearchAddress(endAddressInput, false)
                                }
                                // НЕ вызываем onBuildRouteClick() здесь - он сработает автоматически, когда придут координаты
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = (startPoint != null && endPoint != null) ||
                                (startAddressInput.isNotBlank() && endAddressInput.isNotBlank()) && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                            disabledContentColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.4f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.btn_build_route),
                                style = StylesCustom.basicBodySubTextCenter,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onFinishRouteClick,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.btn_finish_route),
                            style = StylesCustom.basicBodySubTextCenter
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.route_mode_hint),
                    style = StylesCustom.basicBodySubTextCenter.copy(
                        fontSize = TextUnit(15f, TextUnitType.Sp)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
        }
    }
}