package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.sp
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.ui.common.shortName
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * 7-column day picker for the phone-portrait header.
 * Equivalent to phone-day.jsx → DayStrip.
 */
@Composable
fun DayStrip(
    days: List<AppDayOfWeek>,
    activeDay: AppDayOfWeek,
    onPick: (AppDayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        days.forEach { day ->
            val active = day == activeDay
            val bg = if (active) colors.accentSoft else Color.Transparent
            val nameText = if (active) colors.accent else colors.text

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg)
                    .clickable { onPick(day) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = day.shortName(),
                    style = TextStyle(
                        color = nameText,
                        fontSize = MobileTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .background(colors.line)
            .padding(top = 1.dp),
    )
}
