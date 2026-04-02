package ru.itis.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.itis.core.ui.component.settings.IconSettings
import ru.itis.core.ui.component.settings.TopBarSettings
import ru.itis.core.ui.theme.BeaconTheme
import ru.itis.core.ui.theme.DimensionsCustom
import ru.itis.core.ui.theme.IconsCustom
import ru.itis.core.ui.theme.StylesCustom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarCustom(
    topBarSettings: TopBarSettings,
    iconSettings: IconSettings? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = topBarSettings.text,
                style = StylesCustom.h11,
                color = topBarSettings.textColor ?: MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp)
            )
        },
        navigationIcon = {
            iconSettings?.let {
                IconButton(
                    onClick = iconSettings.onClick,
                    modifier = Modifier
                        .padding(top = 8.dp, start = 16.dp)

                ) {
                    Icon(
                        imageVector = iconSettings.icon ?: IconsCustom.arrowBackIcon(),
                        contentDescription = iconSettings.description,
                        tint = iconSettings.color ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .height(DimensionsCustom.iconSize)
                            .width(DimensionsCustom.iconSize)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topBarSettings.containerColor ?: Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun TopBarCustomPreview() {
    BeaconTheme {
        TopBarCustom(
            topBarSettings = TopBarSettings(
                text = "Заголовок",
            ),
            iconSettings = IconSettings()
        )
    }
}