package ru.itis.core.ui.component.settings

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import ru.itis.core.ui.theme.DimensionsCustom

data class ImageSettings(
    val url: String? = null,
    val contentDescription: String = "",
    val contentScale: ContentScale = ContentScale.Crop,
    val shape: Shape = RoundedCornerShape(DimensionsCustom.roundedCornersSmall),
    val backgroundColor: Color? = null,
    val modifier: Modifier = Modifier,
)