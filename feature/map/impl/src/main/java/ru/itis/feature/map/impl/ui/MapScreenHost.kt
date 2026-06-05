package ru.itis.feature.map.impl.ui

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.core.domain.model.mark.IncidentModel
import ru.itis.core.ui.BaseScreen
import ru.itis.core.ui.R
import ru.itis.feature.map.impl.ui.components.RouteSelectionPanel
import ru.itis.core.ui.component.settings.BottomBarSettings
import ru.itis.core.ui.theme.BeaconTheme
import ru.itis.core.ui.theme.DimensionsCustom
import ru.itis.core.ui.theme.IconsCustom
import ru.itis.core.ui.theme.StylesCustom
import ru.itis.feature.map.impl.ui.components.AddIncidentDialog
import ru.itis.feature.map.impl.ui.components.IncidentDetailDialog
import ru.itis.feature.map.impl.ui.mvi.MapScreenEffect
import ru.itis.feature.map.impl.ui.mvi.MapScreenEvent
import ru.itis.feature.map.impl.ui.mvi.MapScreenState
import ru.itis.feature.map.impl.ui.utils.MapScreenDelegate

@Composable
internal fun MapScreenHost() {
    val context = LocalContext.current
    val fragmentManager = (context as AppCompatActivity).supportFragmentManager
    val containerId = remember { View.generateViewId() }

    val viewModel: MapScreenViewModel = hiltViewModel()

    val pageState by viewModel.pageState.collectAsState(initial = MapScreenState.Initial)
    val selectedIncident by viewModel.selectedIncident.collectAsState()
    LaunchedEffect(selectedIncident) {
        Log.i("TAP_INCIDENT_DEBUG", "selectedIncident changed: ${selectedIncident?.id ?: "null"}")
    }

    val isEditMode by viewModel.isEditMode.collectAsState()
    val isRouteMode by viewModel.isRouteMode.collectAsState()

    // Состояние для сворачивания панели маршрута
    var isRoutePanelCollapsed by remember { mutableStateOf(false) }

    // Сбрасываем состояние сворачивания при выходе из режима маршрута
    LaunchedEffect(isRouteMode) {
        if (!isRouteMode) {
            isRoutePanelCollapsed = false
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogCoords by remember { mutableStateOf(Pair(0.0, 0.0)) }

    // Создаём делегат, который фрагмент будет использовать для отправки событий
    // Важно: используем remember с ключами isEditMode и isRouteMode, чтобы делегат пересоздавался при смене режима
    val fragmentDelegate = remember(viewModel, isEditMode, isRouteMode) {
        object : MapScreenDelegate {
            override fun onMapTapped(latitude: Double, longitude: Double) {
                when {
                    isEditMode -> {
                        viewModel.processEvent(MapScreenEvent.OnMapTapped(latitude, longitude))
                    }
                    isRouteMode -> {
                        // В режиме маршрута тап выбирает точку (сначала start, потом end)
                        // Точки выбираются только если они ещё не заданы
                        val currentState = viewModel.pageState.value as? MapScreenState.RouteMode
                        if (currentState?.startPoint == null) {
                            Log.i("BUILD_ROUTE", "Start point tap (fragment delegate)")
                            viewModel.processEvent(
                                MapScreenEvent.OnRouteStartSelected(latitude, longitude)
                            )
                        } else if (currentState.endPoint == null) {
                            Log.i("BUILD_ROUTE", "End point tap (fragment delegate)")
                            viewModel.processEvent(
                                MapScreenEvent.OnRouteEndSelected(latitude, longitude)
                            )
                        }
                    }
                }
            }
            override fun onIncidentClicked(incident: IncidentModel) {
                viewModel.processEvent(MapScreenEvent.OnIncidentClicked(incident))
            }

            override fun onMapBoundsChanged(
                minLat: Double,
                maxLat: Double,
                minLng: Double,
                maxLng: Double
            ) {
                if (!isRouteMode) {
                    viewModel.processEvent(
                        MapScreenEvent.OnMapBoundsChanged(
                            bounds = doubleArrayOf(minLat, maxLat, minLng, maxLng)
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (fragmentManager.findFragmentById(containerId) == null) {
            val fragment = MapScreenFragment().apply {
                delegate = fragmentDelegate
            }
            fragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commit()
        }

        viewModel.pageEffect.collect { effect ->
            when (effect) {
                is MapScreenEffect.Message -> Toast.makeText(
                    context,
                    context.getText(effect.message),
                    Toast.LENGTH_SHORT
                ).show()

                is MapScreenEffect.ShowAddIncidentDialog -> {
                    Log.i("ADD_INCIDENT_DEBUG", "Show AddIncidentDialog")
                    addDialogCoords = Pair(effect.latitude, effect.longitude)
                    showAddDialog = true
                }
                is MapScreenEffect.IncidentAdded -> {
                    // Находим фрагмент и обновляем карту
                    fragmentManager.findFragmentById(containerId)?.let { frag ->
                        if (frag is MapScreenFragment) {
                            frag.addIncident(effect.incident)
                        }
                    }
                }
                is MapScreenEffect.IncidentStatusUpdated -> {
                    fragmentManager.findFragmentById(containerId)?.let { frag ->
                        if (frag is MapScreenFragment) {
                            frag.updateIncidentStatus(effect.incidentId, effect.incidentType, effect.newStatus)
                        }
                    }
                }
                is MapScreenEffect.RouteBuilt -> {
                    // Рисуем маршрут и оверлей на карте
                    fragmentManager.findFragmentById(containerId)?.let { frag ->
                        if (frag is MapScreenFragment) {
                            // Рисуем линию маршрута с цветом по riskScore
                            frag.drawSafeRoute(
                                polyline = effect.result.route.polyline,
                                riskScore = effect.result.route.riskScore
                            )
                            // Рисуем иконки безопасности вдоль маршрута (если есть)
                            effect.result.safetyOverlay?.let { overlay ->
                                frag.drawSafetyOverlay(overlay)
                            }
                        }
                    }
                }

                is MapScreenEffect.RouteFinished -> {
                    // Очищаем маршрут и оверлей, возвращаем карту в исходное состояние
                    fragmentManager.findFragmentById(containerId)?.let { frag ->
                        if (frag is MapScreenFragment) {
                            frag.clearRouteAndSafetyOverlay()
                        }
                    }
                }

                is MapScreenEffect.RoutePointMarkerAdded -> {
                    fragmentManager.findFragmentById(containerId)?.let { frag ->
                        if (frag is MapScreenFragment) {
                            frag.drawRoutePointMarker(
                                latitude = effect.latitude,
                                longitude = effect.longitude,
                                isStart = effect.isStart
                            )
                        }
                    }
                }

                is MapScreenEffect.OnAddressGeocoded -> {
                    viewModel.processEvent(
                        if (effect.isStartPoint) {
                            MapScreenEvent.OnRouteStartSelected(
                                latitude = effect.latitude,
                                longitude = effect.longitude,
                                address = effect.address
                            )
                        } else {
                            MapScreenEvent.OnRouteEndSelected(
                                latitude = effect.latitude,
                                longitude = effect.longitude,
                                address = effect.address
                            )
                        }
                    )
                }
            }
        }
    }

    // Подписка на состояние для первоначальной отрисовки
    LaunchedEffect(pageState) {
        Log.i("RENDER_INCIDENT_DEBUG", "Compose: pageState changed to: ${pageState::class.simpleName}")
        if (pageState is MapScreenState.IncidentsLoaded && !isRouteMode) {
            Log.i("RENDER_INCIDENT_DEBUG", "Compose: Calling frag.renderIncidents with ${(pageState as MapScreenState.IncidentsLoaded).incidents.size} items")
            fragmentManager.findFragmentById(containerId)?.let { frag ->
                if (frag is MapScreenFragment) {
                    frag.renderIncidents((pageState as MapScreenState.IncidentsLoaded).incidents)
                } else {
                    Log.e("RENDER_INCIDENT_DEBUG", "Compose: Fragment found but wrong type: ${frag::class}")
                }
            } ?: Log.e("RENDER_INCIDENT_DEBUG", "Compose: Fragment not found by id $containerId")
        }
    }

    BaseScreen(
        bottomBarSettings = BottomBarSettings(
            onMapClick = { }, //мы уже тут
            onProfileClick = {
                viewModel.processEvent(
                    MapScreenEvent.OnProfileBottomBarClick
                )
            },
        )
    ) { innerPadding ->

        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply { id = containerId }
            },
        )

        if (isRouteMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(animationSpec = tween(300)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                RouteSelectionPanel(
                    startPoint = (pageState as? MapScreenState.RouteMode)?.startPoint,
                    endPoint = (pageState as? MapScreenState.RouteMode)?.endPoint,
                    isLoading = (pageState as? MapScreenState.RouteMode)?.isLoading ?: false,
                    isCollapsed = isRoutePanelCollapsed,
                    onToggleCollapse = { isRoutePanelCollapsed = !isRoutePanelCollapsed },
                    onBuildRouteClick = {
                        viewModel.processEvent(MapScreenEvent.OnBuildRouteClicked)
                    },
                    onFinishRouteClick = {
                        viewModel.processEvent(MapScreenEvent.OnFinishRouteClicked)
                    },
                    onSearchAddress = { query, isStartPoint ->
                        viewModel.processEvent(
                            MapScreenEvent.OnSearchAddressRequested(
                                query = query,
                                isStartPoint = isStartPoint
                            )
                        )
                    }
                )

                // Кнопка сворачивания/разворачивания панели
                Surface(
                    onClick = { isRoutePanelCollapsed = !isRoutePanelCollapsed },
                    shape = RoundedCornerShape(DimensionsCustom.roundedCornersSmall),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .padding(top = if (isRoutePanelCollapsed) 32.dp else 4.dp)
                        .animateContentSize(animationSpec = tween(200))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isRoutePanelCollapsed)
                                IconsCustom.expandIcon()
                            else
                                IconsCustom.collapseIcon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRoutePanelCollapsed)
                                stringResource(R.string.btn_expand_route_panel)
                            else
                                stringResource(R.string.btn_collapse_route_panel),
                            style = StylesCustom.basicBodySubTextCenter.copy(
                                fontSize = androidx.compose.ui.unit.TextUnit(
                                    13f,
                                    androidx.compose.ui.unit.TextUnitType.Sp
                                )
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                        )
                    }
                }
            }
        }



        if (!isRouteMode) {
            // Кнопка переключения режима редактирования (в правом верхнем углу)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    onClick = { viewModel.processEvent(MapScreenEvent.OnToggleEditMode) },
                    shape = RoundedCornerShape(DimensionsCustom.roundedCornersMid),
                    color = if (isEditMode)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditMode) IconsCustom.editIcon() else IconsCustom.visibilityIcon(),
                            contentDescription = null,
                            tint = if (isEditMode)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditMode)
                                stringResource(R.string.map_screen_edit_mode_on)
                            else
                                stringResource(R.string.map_screen_edit_mode_off),
                            style = StylesCustom.basicBodyTextCenter,
                            color = if (isEditMode)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Кнопка входа в режим построения маршрута (показывается только когда НЕ в режиме маршрута и НЕ в режиме редактирования)
        if (!isRouteMode && !isEditMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    onClick = { viewModel.processEvent(MapScreenEvent.OnEnterRouteMode) },
                    shape = RoundedCornerShape(DimensionsCustom.roundedCornersMid),
                    color = MaterialTheme.colorScheme.tertiary,
                    tonalElevation = 4.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = IconsCustom.routeIcon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.btn_enter_route_mode),
                            style = StylesCustom.basicBodyTextCenter,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }

        // Диалог добавления инцидента
        if (showAddDialog && isEditMode) {
            AddIncidentDialog(
                onDismissRequest = { showAddDialog = false },
                onConfirm = { type, description ->
                    viewModel.processEvent(
                        MapScreenEvent.OnAddIncident(
                            latitude = addDialogCoords.first,
                            longitude = addDialogCoords.second,
                            type = type,
                            description = description
                        )
                    )
                    showAddDialog = false
                }
            )
        }

        // Диалог деталей инцидента
        selectedIncident?.let { incident ->
            IncidentDetailDialog(
                incident = incident,
                onDismissRequest = { viewModel.processEvent(MapScreenEvent.OnCloseIncidentDialog) },
                onVerify = { action ->
                    incident.id?.let { id ->
                        viewModel.processEvent(MapScreenEvent.OnVerifyIncident(id, action))
                    }
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
internal fun MapScreenHostPreview() {
    BeaconTheme {
        MapScreenHost()
    }
}