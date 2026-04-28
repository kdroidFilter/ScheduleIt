package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Small uppercase section label used by Settings and the Event editor.
 * Equivalent to the JSX `SectionLabel` component.
 */
@Composable
fun SectionHeading(
    text: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    val colors = MobileTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BasicText(
            text = text.uppercase(),
            style = TextStyle(
                color = colors.textSec,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            ),
        )
        if (hint != null) {
            BasicText(
                text = hint,
                style = TextStyle(
                    color = colors.textTer,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}
