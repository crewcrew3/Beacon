package ru.itis.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.itis.core.ui.component.settings.ButtonSettings
import ru.itis.core.ui.theme.BeaconTheme

@Composable
fun PrimaryButtonCustom(
    buttonSettings: ButtonSettings,
) {
    Button(
        onClick = { buttonSettings.onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonSettings.containerColor ?: MaterialTheme.colorScheme.primary,
            contentColor = buttonSettings.contentColor ?: MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onTertiary,
        ),
        shape = buttonSettings.shape,
        enabled = buttonSettings.enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonSettings.height)
    ) {
        Text(
            text = buttonSettings.text,
            style = buttonSettings.textStyle,
        )
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun PrimaryButtonCustomPreview() {
    BeaconTheme {
        PrimaryButtonCustom(
            buttonSettings = ButtonSettings(
                text = "Нажми",
                onClick = {}
            ),
        )
    }
}