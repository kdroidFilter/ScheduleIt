package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Two-column stepper picker (hour | minute) with up/down chevrons.
 * Replicates the look of editor.jsx → TimeInput while keeping the existing
 * stepper UX so iso-feature parity is preserved.
 */
@Composable
fun MobileTimePicker(
    label: String,
    valueMinute: Int,
    rangeStart: Int,
    rangeEnd: Int,
    stepMinutes: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onBlocked: ((atUpper: Boolean) -> Unit)? = null,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val hour = valueMinute / 60
    val minute = valueMinute % 60

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.bgChip)
            .border(1.dp, colors.line, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BasicText(
            text = label.uppercase(),
            style = TextStyle(
                color = colors.textTer,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
            ),
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StepperColumn(
                    value = hour,
                    onUp = {
                        val target = (valueMinute + 60).coerceAtMost(rangeEnd)
                        if (target != valueMinute) onChange(target) else onBlocked?.invoke(true)
                    },
                    onDown = {
                        val target = (valueMinute - 60).coerceAtLeast(rangeStart)
                        if (target != valueMinute) onChange(target) else onBlocked?.invoke(false)
                    },
                )
                BasicText(
                    text = ":",
                    style = TextStyle(
                        color = colors.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                StepperColumn(
                    value = minute,
                    onUp = {
                        val target = (valueMinute + stepMinutes).coerceAtMost(rangeEnd)
                        if (target != valueMinute) onChange(target) else onBlocked?.invoke(true)
                    },
                    onDown = {
                        val target = (valueMinute - stepMinutes).coerceAtLeast(rangeStart)
                        if (target != valueMinute) onChange(target) else onBlocked?.invoke(false)
                    },
                )
            }
        }
    }
}

@Composable
fun MobileHourPicker(
    label: String,
    valueHour: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.bgChip)
            .border(1.dp, colors.line, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BasicText(
            text = label.uppercase(),
            style = TextStyle(
                color = colors.textTer,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
            ),
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StepperColumn(
                    value = valueHour,
                    onUp = {
                        val target = (valueHour + 1).coerceAtMost(range.last)
                        if (target != valueHour) onChange(target)
                    },
                    onDown = {
                        val target = (valueHour - 1).coerceAtLeast(range.first)
                        if (target != valueHour) onChange(target)
                    },
                )
                BasicText(
                    text = ":00",
                    style = TextStyle(
                        color = colors.textSec,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun StepperColumn(
    value: Int,
    onUp: () -> Unit,
    onDown: () -> Unit,
) {
    val colors = MobileTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ChevronCell(label = "▲", onClick = onUp)
        BasicText(
            text = value.toString().padStart(2, '0'),
            style = TextStyle(
                color = colors.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier.padding(vertical = 2.dp),
        )
        ChevronCell(label = "▼", onClick = onDown)
    }
}

@Composable
private fun ChevronCell(label: String, onClick: () -> Unit) {
    val colors = MobileTheme.colors
    Box(
        modifier = Modifier
            .size(32.dp, 18.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(color = colors.textSec, fontSize = 9.sp),
        )
    }
}
