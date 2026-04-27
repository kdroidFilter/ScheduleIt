package dev.nucleus.scheduleit.ui.material3

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun MaterialStableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
) {
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }
    if (fieldValue.text != value) {
        val newSelection = TextRange(value.length.coerceAtMost(fieldValue.selection.start))
        fieldValue = fieldValue.copy(text = value, selection = newSelection)
    }
    OutlinedTextField(
        value = fieldValue,
        onValueChange = { next ->
            fieldValue = next
            if (next.text != value) onValueChange(next.text)
        },
        label = { Text(label) },
        singleLine = singleLine,
        modifier = modifier,
    )
}
