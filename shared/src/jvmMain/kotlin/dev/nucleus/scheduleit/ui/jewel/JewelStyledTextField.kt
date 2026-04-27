package dev.nucleus.scheduleit.ui.jewel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
fun JewelStyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = JewelTheme.globalColors.text.normal),
        cursorBrush = SolidColor(JewelTheme.globalColors.text.normal),
        modifier = modifier
            .clip(shape)
            .background(JewelTheme.globalColors.panelBackground)
            .border(1.dp, JewelTheme.globalColors.borders.normal, shape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    )
}
