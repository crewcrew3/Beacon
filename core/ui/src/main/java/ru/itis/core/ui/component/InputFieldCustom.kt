package ru.itis.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.itis.core.ui.component.settings.InputFieldSettings
import ru.itis.core.ui.theme.BeaconTheme
import ru.itis.core.ui.theme.IconsCustom
import ru.itis.core.ui.theme.StylesCustom

@Composable
fun InputFieldCustom(
    inputFieldSettings: InputFieldSettings,
) {
    var placeholder: @Composable (() -> Unit)? = null
    inputFieldSettings.placeholderText?.let { text -> 
        placeholder = {
            Text(
                text = text,
                style = StylesCustom.body3,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    var textState by remember { mutableStateOf(TextFieldValue(inputFieldSettings.startValue)) }
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        inputFieldSettings.labelText?.let { text -> 
            Label(text)
        }
        TextField(
            value = textState,
            onValueChange = { input ->
                if (inputFieldSettings.isNumberKeyboard) { //на всякий случай, вдруг нам сюда заходят вставить текст из буфера
                    val filteredInput = input.text.filter { it.isDigit() }
                    textState = input.copy(text = filteredInput)
                    inputFieldSettings.onValueChange(filteredInput)
                } else {
                    textState = input
                    inputFieldSettings.onValueChange(textState.text)
                }
            },
            placeholder = placeholder,
            textStyle = inputFieldSettings.textStyle,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                errorTextColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            isError = inputFieldSettings.isError,
            shape = inputFieldSettings.shape,
            keyboardOptions = if (inputFieldSettings.isNumberKeyboard)
                KeyboardOptions(keyboardType = KeyboardType.Number) else
                KeyboardOptions.Default,
            visualTransformation = if (inputFieldSettings.isPasswordField) {
                if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = {
                if (inputFieldSettings.isPasswordField) {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        if (passwordVisible) {
                            Icon(
                                imageVector = IconsCustom.visibilityIcon(),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = ""
                            )
                        } else {
                            Icon(
                                imageVector = IconsCustom.visibilityOffIcon(),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = ""
                            )
                        }
                    }
                }
            },
            modifier = inputFieldSettings.modifier
                .fillMaxWidth()
        )
        if (inputFieldSettings.isError) {
            Text(
                text = inputFieldSettings.errorText,
                style = StylesCustom.body2,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 8.dp, start = 8.dp)
            )
        }
    }
}

@Composable
fun Label(text: String) {
    Text(
        text = text,
        style = StylesCustom.body5,
        color = MaterialTheme.colorScheme.onSecondary,
        modifier = Modifier
            .padding(bottom = 8.dp, start = 8.dp)
    )
}


@Preview(showBackground = true, apiLevel = 34)
@Composable
fun InputFieldCustomPreview() {
    BeaconTheme {
        InputFieldCustom(
            inputFieldSettings = InputFieldSettings(
                labelText = "Лейбл",
                placeholderText = "Введите текст"
            ),
        )
    }
}
