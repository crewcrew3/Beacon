package ru.itis.feature.map.impl.ui

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.itis.core.ui.BaseScreen
import ru.itis.core.ui.R
import ru.itis.core.ui.component.settings.BottomBarSettings
import ru.itis.core.ui.theme.IconsCustom
import ru.itis.core.ui.theme.StylesCustom
import ru.itis.feature.map.impl.ui.components.AddIncidentDialog
import ru.itis.feature.map.impl.ui.components.IncidentDetailDialog
import ru.itis.feature.map.impl.ui.mvi.MapScreenEffect
import ru.itis.feature.map.impl.ui.mvi.MapScreenEvent
import ru.itis.feature.map.impl.ui.mvi.MapScreenState

@Composable
internal fun MapScreenHost() {
    val context = LocalContext.current
    val fragmentManager = (context as AppCompatActivity).supportFragmentManager
    val containerId = remember { View.generateViewId() }

    val viewModel: MapScreenViewModel = hiltViewModel()
    Log.i("VM_DEBUG", "ViewModel hashCode: ${viewModel.hashCode()}")
    val pageState by viewModel.pageState.collectAsState(initial = MapScreenState.Initial)
    val selectedIncident by viewModel.selectedIncident.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    // Состояния для диалогов
    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogCoords by remember { mutableStateOf(Pair(0.0, 0.0)) }

    LaunchedEffect(Unit) {
        if (fragmentManager.findFragmentById(containerId) == null) {
            fragmentManager.beginTransaction()
                .replace(containerId, MapScreenFragment::class.java, null)
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
                // IncidentAdded и IncidentStatusUpdated обрабатываются в Fragment
                else -> {}
            }
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

        val density = LocalDensity.current
        val layoutDir = LocalLayoutDirection.current

        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    id = containerId

//                    with(density) {
//                        val leftPx = innerPadding.calculateLeftPadding(layoutDir).roundToPx()
//                        val topPx = innerPadding.calculateTopPadding().roundToPx()
//                        val rightPx = innerPadding.calculateRightPadding(layoutDir).roundToPx()
//                        val bottomPx = innerPadding.calculateBottomPadding().roundToPx()
//
//                        setPadding(leftPx, topPx, rightPx, bottomPx)
//                    }
                }
            },
        )

        // Кнопка переключения режима редактирования (в правом верхнем углу)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                onClick = { viewModel.processEvent(MapScreenEvent.OnToggleEditMode) },
                shape = RoundedCornerShape(24.dp),
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
                        style = StylesCustom.body3,
                        color = if (isEditMode)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Диалог добавления инцидента
        if (showAddDialog) {
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