package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Single- or multi-line text field with rounded border, theme-aware.
 * Selection state is preserved across recompositions caused by external value updates.
 */
@Composable
fun MobileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String? = null,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val style = TextStyle(color = colors.text, fontSize = typography.body)

    var fieldState by remember(value === fieldStateMarker) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    if (fieldState.text != value) {
        fieldState = fieldState.copy(text = value, selection = TextRange(value.length.coerceAtMost(value.length)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.bgChip)
            .border(1.dp, colors.line, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        BasicTextField(
            value = fieldState,
            onValueChange = { next ->
                fieldState = next
                if (next.text != value) onValueChange(next.text)
            },
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            textStyle = style,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
            modifier = Modifier.fillMaxWidth(),
        )
        if (value.isEmpty() && placeholder != null) {
            androidx.compose.foundation.text.BasicText(
                text = placeholder,
                style = style.copy(color = colors.textTer),
            )
        }
    }
}

private val fieldStateMarker = Any()
