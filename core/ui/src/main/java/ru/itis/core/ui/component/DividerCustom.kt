package ru.itis.core.ui.component

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DividerCustom(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    )
}