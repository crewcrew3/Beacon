package ru.itis.core.ui.component.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class IconSettings(
    val icon: ImageVector? = null, //null т.к. предполагается что есть дефолтная иконка но ее сюда я засунуть не могу тк она Composable
    val color: Color? = null,
    val onClick: () -> Unit = {},
    val description: String = "",
)