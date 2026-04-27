package dev.nucleus.scheduleit.ui.material3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MaterialTimePicker(
    label: String,
    valueMinute: Int,
    rangeStart: Int,
    rangeEnd: Int,
    stepMinutes: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hour = valueMinute / 60
    val minute = valueMinute % 60

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StepperColumn(
                    value = hour,
                    onUp = {
                        val target = (valueMinute + 60).coerceAtMost(rangeEnd)
                        if (target != valueMinute) onChange(target)
                    },
                    onDown = {
                        val target = (valueMinute - 60).coerceAtLeast(rangeStart)
                        if (target != valueMinute) onChange(target)
                    },
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                StepperColumn(
                    value = minute,
                    onUp = {
                        val target = (valueMinute + stepMinutes).coerceAtMost(rangeEnd)
                        if (target != valueMinute) onChange(target)
                    },
                    onDown = {
                        val target = (valueMinute - stepMinutes).coerceAtLeast(rangeStart)
                        if (target != valueMinute) onChange(target)
                    },
                )
            }
        }
    }
}

@Composable
fun MaterialHourPicker(
    label: String,
    valueHour: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                Text(
                    text = ":00",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(
            onClick = onUp,
            modifier = Modifier.size(36.dp, 22.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text("▲", style = MaterialTheme.typography.labelSmall)
        }
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.size(48.dp, 32.dp).padding(top = 2.dp),
        )
        TextButton(
            onClick = onDown,
            modifier = Modifier.size(36.dp, 22.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text("▼", style = MaterialTheme.typography.labelSmall)
        }
    }
}
