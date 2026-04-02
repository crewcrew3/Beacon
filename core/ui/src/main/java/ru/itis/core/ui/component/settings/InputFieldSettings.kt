package ru.itis.core.ui.component.settings

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import ru.itis.core.ui.theme.DimensionsCustom
import ru.itis.core.ui.theme.StylesCustom

data class InputFieldSettings(
    val labelText: String? = null,
    val placeholderText: String? = null,
    val isError: Boolean = false,
    val textStyle: TextStyle = StylesCustom.body3,
    val shape: Shape = RoundedCornerShape(DimensionsCustom.roundedCorners),
    val errorText: String = "",
    val startValue: String = "",
    val onValueChange: (String) -> Unit = {},
    val isNumberKeyboard: Boolean = false,
    val isPasswordField: Boolean = false,
    val modifier: Modifier = Modifier
)