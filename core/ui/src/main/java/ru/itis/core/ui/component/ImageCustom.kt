package ru.itis.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import ru.itis.core.ui.R
import ru.itis.core.ui.component.settings.ImageSettings

@Composable
fun ImageCustom(
    imageSettings: ImageSettings,
) {
    val hasImage = imageSettings.url?.isNotBlank()
    AsyncImage(
        model = if (hasImage == true) imageSettings.url else null,
        contentDescription = imageSettings.contentDescription,
        modifier = imageSettings.modifier
            .clip(imageSettings.shape)
            .background(imageSettings.backgroundColor ?: Color.Transparent),
        contentScale = imageSettings.contentScale,
        placeholder = painterResource(R.drawable.pic_img_placeholder),
        error = painterResource(R.drawable.pic_img_placeholder),
    )
}