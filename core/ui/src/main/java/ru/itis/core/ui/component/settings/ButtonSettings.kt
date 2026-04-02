package ru.itis.core.ui.component.settings

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import ru.itis.core.ui.theme.DimensionsCustom
import ru.itis.core.ui.theme.StylesCustom

data class ButtonSettings(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val shape: Shape = RoundedCornerShape(DimensionsCustom.roundedCorners),
    val height: Dp = DimensionsCustom.btnHeight,
    val textStyle: TextStyle = StylesCustom.body3,
)