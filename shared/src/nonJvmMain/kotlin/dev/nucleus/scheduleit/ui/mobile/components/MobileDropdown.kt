package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Popup-anchored menu — replaces Material's DropdownMenu so the chrome
 * (background, separators) honours the mobile theme tokens.
 */
@Composable
fun MobileDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    offset: IntOffset = IntOffset.Zero,
    content: @Composable () -> Unit,
) {
    if (!expanded) return
    val colors = MobileTheme.colors
    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true),
        offset = offset,
    ) {
        Column(
            modifier = modifier
                .width(IntrinsicSize.Max)
                .widthIn(min = 180.dp, max = 280.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.bgElev)
                .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                .padding(vertical = 4.dp),
        ) {
            content()
        }
    }
}

@Composable
fun MobileMenuItem(
    label: String,
    onClick: () -> Unit,
    danger: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val fg = if (danger) colors.danger else colors.text
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = fg,
                fontSize = typography.body,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
