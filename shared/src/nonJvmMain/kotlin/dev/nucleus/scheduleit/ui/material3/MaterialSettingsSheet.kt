package dev.nucleus.scheduleit.ui.material3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleUiState
import dev.nucleus.scheduleit.ui.common.formatHourLabel
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.common.localizedWeekOrder
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_cancel
import scheduleit.shared.generated.resources.action_close
import scheduleit.shared.generated.resources.settings_days_explanation
import scheduleit.shared.generated.resources.action_export
import scheduleit.shared.generated.resources.action_import
import scheduleit.shared.generated.resources.action_reset_confirm
import scheduleit.shared.generated.resources.action_reset_database
import scheduleit.shared.generated.resources.reset_confirm_message
import scheduleit.shared.generated.resources.reset_confirm_title
import scheduleit.shared.generated.resources.settings_data_section
import scheduleit.shared.generated.resources.settings_days_section
import scheduleit.shared.generated.resources.settings_notifications_label
import scheduleit.shared.generated.resources.settings_notifications_section
import scheduleit.shared.generated.resources.settings_end_hour
import scheduleit.shared.generated.resources.settings_start_hour
import scheduleit.shared.generated.resources.settings_hours_range_label
import scheduleit.shared.generated.resources.settings_hours_section
import scheduleit.shared.generated.resources.settings_link_hidden
import scheduleit.shared.generated.resources.settings_link_independent
import scheduleit.shared.generated.resources.settings_link_same_as
import scheduleit.shared.generated.resources.settings_title

private val GROUP_PALETTE = listOf(
    Color(0xFF42A5F5L.toInt()),
    Color(0xFFEF5350L.toInt()),
    Color(0xFF66BB6AL.toInt()),
    Color(0xFFFFCA28L.toInt()),
    Color(0xFFAB47BCL.toInt()),
    Color(0xFFFF7043L.toInt()),
    Color(0xFF26C6DAL.toInt()),
    Color(0xFF8D6E63L.toInt()),
)

private fun colorForTemplate(templateId: Long, templates: List<Long>): Color {
    val idx = templates.indexOf(templateId).coerceAtLeast(0)
    return GROUP_PALETTE[idx % GROUP_PALETTE.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSettingsSheet(
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onIntent(ScheduleIntent.CloseSettings) },
        confirmButton = {
            TextButton(onClick = { onIntent(ScheduleIntent.CloseSettings) }) {
                Text(stringResource(Res.string.action_close))
            }
        },
        title = { Text(stringResource(Res.string.settings_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                HoursSection(
                    startHour = state.settings.startMinute / 60,
                    endHour = state.settings.endMinute / 60,
                    onIntent = onIntent,
                )
                HorizontalDivider()
                DaysSection(state = state, onIntent = onIntent)
                HorizontalDivider()
                NotificationsSection(
                    enabled = state.settings.notificationsEnabled,
                    onIntent = onIntent,
                )
                HorizontalDivider()
                DataSection(onIntent = onIntent)
            }
        },
    )
}

@Composable
private fun NotificationsSection(
    enabled: Boolean,
    onIntent: (ScheduleIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.settings_notifications_section),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.settings_notifications_label),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = enabled,
                onCheckedChange = { onIntent(ScheduleIntent.SetNotificationsEnabled(it)) },
            )
        }
    }
}

@Composable
private fun DataSection(
    onIntent: (ScheduleIntent) -> Unit,
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.settings_data_section),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onIntent(ScheduleIntent.ExportData) }) {
                Text(stringResource(Res.string.action_export))
            }
            OutlinedButton(onClick = { onIntent(ScheduleIntent.ImportData) }) {
                Text(stringResource(Res.string.action_import))
            }
        }
        OutlinedButton(
            onClick = { showResetConfirm = true },
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        ) {
            Text(stringResource(Res.string.action_reset_database))
        }
    }
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(Res.string.reset_confirm_title)) },
            text = { Text(stringResource(Res.string.reset_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirm = false
                        onIntent(ScheduleIntent.ResetData)
                    },
                ) {
                    Text(
                        stringResource(Res.string.action_reset_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun HoursSection(
    startHour: Int,
    endHour: Int,
    onIntent: (ScheduleIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(Res.string.settings_hours_section),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            MaterialHourPicker(
                label = stringResource(Res.string.settings_start_hour),
                valueHour = startHour,
                range = 0..(endHour - 1),
                onChange = { onIntent(ScheduleIntent.ChangeHours(it, endHour)) },
            )
            MaterialHourPicker(
                label = stringResource(Res.string.settings_end_hour),
                valueHour = endHour,
                range = (startHour + 1)..24,
                onChange = { onIntent(ScheduleIntent.ChangeHours(startHour, it)) },
            )
        }
    }
}

@Composable
private fun DaysSection(
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val templateOrder = state.templates.map { it.id }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.settings_days_section), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(Res.string.settings_days_explanation),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        localizedWeekOrder().forEach { day ->
            DayCard(
                day = day,
                state = state,
                templateOrder = templateOrder,
                onIntent = onIntent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayCard(
    day: AppDayOfWeek,
    state: ScheduleUiState,
    templateOrder: List<Long>,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val templateId = state.assignments[day]
    val isVisible = templateId != null
    val anchorDay = templateId?.let { tpl ->
        state.assignments.entries.firstOrNull { it.value == tpl && it.key != day }?.key
    }
    val groupColor = if (isVisible) colorForTemplate(templateId, templateOrder) else Color.Transparent

    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isVisible) groupColor else MaterialTheme.colorScheme.outlineVariant),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(day.fullName(), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                if (isVisible) {
                    SameAsRow(
                        day = day,
                        anchorDay = anchorDay,
                        state = state,
                        onIntent = onIntent,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.settings_link_hidden),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = isVisible,
                onCheckedChange = { checked ->
                    if (checked) onIntent(ScheduleIntent.AssignDayToNewTemplate(day))
                    else onIntent(ScheduleIntent.HideDay(day))
                },
            )
        }
    }
}

@Composable
private fun SameAsRow(
    day: AppDayOfWeek,
    anchorDay: AppDayOfWeek?,
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val visibleOthers = state.assignments.keys
        .filter { it != day }
        .sortedBy { it.isoIndex }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.settings_link_same_as),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Box {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.clickable { menuOpen = true },
            ) {
                Text(
                    text = anchorDay?.fullName() ?: stringResource(Res.string.settings_link_independent),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.settings_link_independent)) },
                    onClick = {
                        menuOpen = false
                        onIntent(ScheduleIntent.AssignDayToNewTemplate(day))
                    },
                )
                visibleOthers.forEach { other ->
                    val otherTpl = state.assignments[other] ?: return@forEach
                    DropdownMenuItem(
                        text = { Text(other.fullName()) },
                        onClick = {
                            menuOpen = false
                            onIntent(ScheduleIntent.AssignDayToTemplate(day, otherTpl))
                        },
                    )
                }
            }
        }
    }
}

