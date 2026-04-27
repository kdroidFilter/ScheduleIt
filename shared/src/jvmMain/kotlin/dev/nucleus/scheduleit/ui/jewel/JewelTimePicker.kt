package dev.nucleus.scheduleit.ui.jewel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.painter.hints.Size
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_step_down
import scheduleit.shared.generated.resources.action_step_up

@Composable
fun JewelTimePicker(
    label: String,
    valueMinute: Int,
    rangeStart: Int,
    rangeEnd: Int,
    stepMinutes: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onBlocked: ((atUpper: Boolean) -> Unit)? = null,
) {
    val hour = valueMinute / 60
    val minute = valueMinute % 60
    val shape = RoundedCornerShape(10.dp)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = JewelTheme.globalColors.text.info)
        Row(
            modifier = Modifier
                .clip(shape)
                .background(JewelTheme.globalColors.panelBackground)
                .border(1.dp, JewelTheme.globalColors.borders.normal, shape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
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
            Spacer(Modifier.width(6.dp))
            Text(":", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(6.dp))
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

@Composable
fun JewelHourPicker(
    label: String,
    valueHour: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = JewelTheme.globalColors.text.info)
        Row(
            modifier = Modifier
                .clip(shape)
                .background(JewelTheme.globalColors.panelBackground)
                .border(1.dp, JewelTheme.globalColors.borders.normal, shape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
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
            Spacer(Modifier.width(6.dp))
            Text("00", color = JewelTheme.globalColors.text.info)
        }
    }
}

@Composable
private fun StepperColumn(
    value: Int,
    onUp: () -> Unit,
    onDown: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onUp, modifier = Modifier.size(28.dp, 20.dp)) {
            Icon(
                key = AllIconsKeys.General.ChevronUp,
                contentDescription = stringResource(Res.string.action_step_up),
                hints = arrayOf(Size(16)),
            )
        }
        Text(
            text = value.toString().padStart(2, '0'),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.height(28.dp).padding(top = 4.dp),
        )
        IconButton(onClick = onDown, modifier = Modifier.size(28.dp, 20.dp)) {
            Icon(
                key = AllIconsKeys.General.ChevronDown,
                contentDescription = stringResource(Res.string.action_step_down),
                hints = arrayOf(Size(16)),
            )
        }
    }
}
