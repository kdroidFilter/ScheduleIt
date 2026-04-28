package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Sidebar row used by the tablet-landscape layout (Schedule, Settings).
 * Equivalent to tablet.jsx → SideRow.
 */
@Composable
fun SideRow(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val bg = if (active) colors.accentSoft else Color.Transparent
    val fg = if (active) colors.accent else colors.text

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.size(16.dp)) { icon() }
        BasicText(
            text = label,
            style = TextStyle(
                color = fg,
                fontSize = typography.bodySmall,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
