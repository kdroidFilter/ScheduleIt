package dev.nucleus.scheduleit.ui.jewel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberDialogState
import io.github.kdroidfilter.nucleus.window.jewel.JewelDecoratedDialog
import io.github.kdroidfilter.nucleus.window.jewel.JewelDialogTitleBar
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleUiState
import dev.nucleus.scheduleit.ui.common.formatHourLabel
import dev.nucleus.scheduleit.ui.common.fullName
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_cancel
import scheduleit.shared.generated.resources.action_close
import scheduleit.shared.generated.resources.action_export
import scheduleit.shared.generated.resources.action_import
import scheduleit.shared.generated.resources.action_reset_confirm
import scheduleit.shared.generated.resources.action_reset_database
import scheduleit.shared.generated.resources.reset_confirm_message
import scheduleit.shared.generated.resources.reset_confirm_title
import scheduleit.shared.generated.resources.settings_data_section
import scheduleit.shared.generated.resources.settings_days_explanation
import scheduleit.shared.generated.resources.settings_days_section
import scheduleit.shared.generated.resources.settings_end_hour
import scheduleit.shared.generated.resources.settings_hours_range_label
import scheduleit.shared.generated.resources.settings_hours_section
import scheduleit.shared.generated.resources.settings_link_hidden
import scheduleit.shared.generated.resources.settings_link_independent
import scheduleit.shared.generated.resources.settings_link_same_as
import scheduleit.shared.generated.resources.settings_notifications_label
import scheduleit.shared.generated.resources.settings_notifications_section
import scheduleit.shared.generated.resources.settings_start_hour
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

private fun colorForTemplate(templateId: Long, order: List<Long>): Color {
    val idx = order.indexOf(templateId).coerceAtLeast(0)
    return GROUP_PALETTE[idx % GROUP_PALETTE.size]
}

@Composable
fun JewelSettingsWindow(
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val windowState = rememberDialogState(size = DpSize(540.dp, 680.dp))
    val title = stringResource(Res.string.settings_title)
    JewelDecoratedDialog(
        onCloseRequest = { onIntent(ScheduleIntent.CloseSettings) },
        state = windowState,
        title = title,
    ) {
        JewelDialogTitleBar { _ -> Text(title) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(JewelTheme.globalColors.panelBackground)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            HoursSection(
                startHour = state.settings.startMinute / 60,
                endHour = state.settings.endMinute / 60,
                onIntent = onIntent,
            )

            DaysSection(state = state, onIntent = onIntent)

            NotificationsSection(
                enabled = state.settings.notificationsEnabled,
                onIntent = onIntent,
            )

            DataSection(onIntent = onIntent)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                DefaultButton(onClick = { onIntent(ScheduleIntent.CloseSettings) }) {
                    Text(stringResource(Res.string.action_close))
                }
            }
        }
    }
}

@Composable
private fun NotificationsSection(
    enabled: Boolean,
    onIntent: (ScheduleIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GroupHeader(stringResource(Res.string.settings_notifications_section))
        CheckboxRow(
            checked = enabled,
            onCheckedChange = { onIntent(ScheduleIntent.SetNotificationsEnabled(it)) },
            text = stringResource(Res.string.settings_notifications_label),
        )
    }
}

@Composable
private fun DataSection(
    onIntent: (ScheduleIntent) -> Unit,
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GroupHeader(stringResource(Res.string.settings_data_section))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onIntent(ScheduleIntent.ExportData) }) {
                Text(stringResource(Res.string.action_export))
            }
            OutlinedButton(onClick = { onIntent(ScheduleIntent.ImportData) }) {
                Text(stringResource(Res.string.action_import))
            }
            OutlinedButton(onClick = { showResetConfirm = true }) {
                Text(stringResource(Res.string.action_reset_database))
            }
        }
    }
    if (showResetConfirm) {
        ResetConfirmDialog(
            onConfirm = {
                showResetConfirm = false
                onIntent(ScheduleIntent.ResetData)
            },
            onCancel = { showResetConfirm = false },
        )
    }
}

@Composable
private fun ResetConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val dialogState = rememberDialogState(size = DpSize(420.dp, 200.dp))
    val title = stringResource(Res.string.reset_confirm_title)
    JewelDecoratedDialog(
        onCloseRequest = onCancel,
        state = dialogState,
        title = title,
    ) {
        JewelDialogTitleBar { _ -> Text(title) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(JewelTheme.globalColors.panelBackground)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.reset_confirm_message))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text(stringResource(Res.string.action_cancel))
                }
                DefaultButton(onClick = onConfirm) {
                    Text(stringResource(Res.string.action_reset_confirm))
                }
            }
        }
    }
}

@Composable
private fun HoursSection(
    startHour: Int,
    endHour: Int,
    onIntent: (ScheduleIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GroupHeader(stringResource(Res.string.settings_hours_section))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        ) {
            JewelHourPicker(
                label = stringResource(Res.string.settings_start_hour),
                valueHour = startHour,
                range = 0..(endHour - 1),
                onChange = { onIntent(ScheduleIntent.ChangeHours(it, endHour)) },
            )
            JewelHourPicker(
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        GroupHeader(stringResource(Res.string.settings_days_section))
        Text(
            stringResource(Res.string.settings_days_explanation),
            color = JewelTheme.globalColors.text.info,
        )
        AppDayOfWeek.entries.forEach { day ->
            DayCard(day = day, state = state, templateOrder = templateOrder, onIntent = onIntent)
        }
    }
}

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, JewelTheme.globalColors.borders.normal, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isVisible) groupColor else JewelTheme.globalColors.borders.normal),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(day.fullName())
                Spacer(Modifier.height(4.dp))
                if (isVisible) {
                    SameAsRow(day = day, anchorDay = anchorDay, state = state, onIntent = onIntent)
                } else {
                    Text(
                        text = stringResource(Res.string.settings_link_hidden),
                        color = JewelTheme.globalColors.text.info,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            CheckboxRow(
                checked = isVisible,
                onCheckedChange = { checked ->
                    if (checked) onIntent(ScheduleIntent.AssignDayToNewTemplate(day))
                    else onIntent(ScheduleIntent.HideDay(day))
                },
            ) {}
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
    val visibleOthers = state.assignments.keys
        .filter { it != day }
        .sortedBy { it.isoIndex }

    val anchorLabel = anchorDay?.fullName() ?: stringResource(Res.string.settings_link_independent)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.settings_link_same_as),
            color = JewelTheme.globalColors.text.info,
        )
        Spacer(Modifier.width(8.dp))
        Dropdown(
            menuContent = {
                selectableItem(
                    selected = anchorDay == null,
                    onClick = { onIntent(ScheduleIntent.AssignDayToNewTemplate(day)) },
                ) {
                    Text(stringResource(Res.string.settings_link_independent))
                }
                visibleOthers.forEach { other ->
                    val otherTpl = state.assignments[other] ?: return@forEach
                    selectableItem(
                        selected = state.assignments[day] == otherTpl,
                        onClick = { onIntent(ScheduleIntent.AssignDayToTemplate(day, otherTpl)) },
                    ) {
                        Text(other.fullName())
                    }
                }
            },
        ) {
            Text(anchorLabel)
        }
    }
}
