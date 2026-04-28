package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Segmented control button — Day / 3 Days / Week selector in the tablet header.
 * Equivalent to tablet.jsx → SegBtn.
 */
@Composable
fun SegBtn(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val border = if (active) colors.lineStrong else colors.line
    val bg = if (active) colors.bgElev else Color.Transparent
    val fg = if (active) colors.text else colors.textSec

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = fg,
                fontSize = typography.label,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}
