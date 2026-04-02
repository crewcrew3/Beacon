package ru.itis.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.itis.core.ui.component.settings.BottomBarSettings
import ru.itis.core.ui.theme.BeaconTheme
import ru.itis.core.ui.theme.DimensionsCustom
import ru.itis.core.ui.theme.IconsCustom

@Composable
fun BottomBarCustom(
    bottomBarSettings: BottomBarSettings,
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .height(DimensionsCustom.bottomNavHeight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = bottomBarSettings.onMapClick
            ) {
                Icon(
                    imageVector = IconsCustom.mapIcon(),
                    contentDescription = "",
                    modifier = Modifier
                        .size(DimensionsCustom.iconSizeMid)
                )
            }
            IconButton(
                onClick = bottomBarSettings.onProfileClick
            ) {
                Icon(
                    imageVector = IconsCustom.profileIcon(),
                    contentDescription = "",
                    modifier = Modifier
                        .size(DimensionsCustom.iconSizeMid)
                )
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun BottomBarCustomPreview() {
    BeaconTheme {
        BottomBarCustom(
            bottomBarSettings = BottomBarSettings({}, {}))
    }
}