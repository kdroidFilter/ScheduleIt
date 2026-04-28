package dev.nucleus.scheduleit.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.data.drive.GoogleDriveStatus
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleUiState
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.common.localizedWeekOrder
import dev.nucleus.scheduleit.ui.mobile.components.ConfirmDialog
import dev.nucleus.scheduleit.ui.mobile.components.MobileDropdownMenu
import dev.nucleus.scheduleit.ui.mobile.components.MobileHourPicker
import dev.nucleus.scheduleit.ui.mobile.components.MobileMenuItem
import dev.nucleus.scheduleit.ui.mobile.components.MobileSwitch
import dev.nucleus.scheduleit.ui.mobile.components.ModalSheet
import dev.nucleus.scheduleit.ui.mobile.components.SectionHeading
import dev.nucleus.scheduleit.ui.mobile.theme.EventColors
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_cancel
import scheduleit.shared.generated.resources.action_drive_backup_now
import scheduleit.shared.generated.resources.action_drive_connect
import scheduleit.shared.generated.resources.action_drive_disconnect
import scheduleit.shared.generated.resources.action_drive_restore
import scheduleit.shared.generated.resources.action_drive_retry
import scheduleit.shared.generated.resources.action_export
import scheduleit.shared.generated.resources.action_import
import scheduleit.shared.generated.resources.action_reset_confirm
import scheduleit.shared.generated.resources.action_reset_database
import scheduleit.shared.generated.resources.action_restore_confirm
import scheduleit.shared.generated.resources.reset_confirm_message
import scheduleit.shared.generated.resources.reset_confirm_title
import scheduleit.shared.generated.resources.restore_confirm_message
import scheduleit.shared.generated.resources.restore_confirm_title
import scheduleit.shared.generated.resources.settings_data_section
import scheduleit.shared.generated.resources.settings_days_explanation
import scheduleit.shared.generated.resources.settings_days_section
import scheduleit.shared.generated.resources.settings_drive_connected
import scheduleit.shared.generated.resources.settings_drive_connected_anon
import scheduleit.shared.generated.resources.settings_drive_connecting
import scheduleit.shared.generated.resources.settings_drive_disconnected
import scheduleit.shared.generated.resources.settings_drive_error
import scheduleit.shared.generated.resources.settings_drive_last_backup
import scheduleit.shared.generated.resources.settings_drive_no_backup
import scheduleit.shared.generated.resources.settings_drive_restoring
import scheduleit.shared.generated.resources.settings_drive_section
import scheduleit.shared.generated.resources.settings_drive_uploading
import scheduleit.shared.generated.resources.settings_end_hour
import scheduleit.shared.generated.resources.settings_hours_section
import scheduleit.shared.generated.resources.settings_link_hidden
import scheduleit.shared.generated.resources.settings_link_independent
import scheduleit.shared.generated.resources.settings_link_same_as
import scheduleit.shared.generated.resources.settings_start_hour
import scheduleit.shared.generated.resources.settings_title

@Composable
fun MobileSettingsSheet(
    visible: Boolean,
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    ModalSheet(
        visible = visible,
        title = stringResource(Res.string.settings_title),
        onDismiss = { onIntent(ScheduleIntent.CloseSettings) },
        fillHeight = 0.9f,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            HoursSection(
                startHour = state.settings.startMinute / 60,
                endHour = state.settings.endMinute / 60,
                onIntent = onIntent,
            )
            DaysSection(state = state, onIntent = onIntent)
            state.googleDrive?.let { driveStatus ->
                DriveSection(status = driveStatus, onIntent = onIntent)
            }
            DataSection(onReset = { showResetConfirm = true }, onIntent = onIntent)
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showResetConfirm) {
        ConfirmDialog(
            title = stringResource(Res.string.reset_confirm_title),
            message = stringResource(Res.string.reset_confirm_message),
            confirmLabel = stringResource(Res.string.action_reset_confirm),
            cancelLabel = stringResource(Res.string.action_cancel),
            danger = true,
            onConfirm = {
                showResetConfirm = false
                onIntent(ScheduleIntent.ResetData)
            },
            onDismiss = { showResetConfirm = false },
        )
    }
}

@Composable
private fun HoursSection(
    startHour: Int,
    endHour: Int,
    onIntent: (ScheduleIntent) -> Unit,
) {
    SectionHeading(text = stringResource(Res.string.settings_hours_section))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
    ) {
        MobileHourPicker(
            label = stringResource(Res.string.settings_start_hour),
            valueHour = startHour,
            range = 0..(endHour - 1),
            onChange = { onIntent(ScheduleIntent.ChangeHours(it, endHour)) },
        )
        MobileHourPicker(
            label = stringResource(Res.string.settings_end_hour),
            valueHour = endHour,
            range = (startHour + 1)..24,
            onChange = { onIntent(ScheduleIntent.ChangeHours(startHour, it)) },
        )
    }
}

@Composable
private fun DaysSection(
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val templateOrder = state.templates.map { it.id }

    SectionHeading(text = stringResource(Res.string.settings_days_section))
    BasicText(
        text = stringResource(Res.string.settings_days_explanation),
        style = TextStyle(color = colors.textSec, fontSize = typography.bodySmall),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.line, RoundedCornerShape(8.dp)),
    ) {
        val days = localizedWeekOrder()
        days.forEachIndexed { index, day ->
            DayRow(
                day = day,
                state = state,
                templateOrder = templateOrder,
                onIntent = onIntent,
                isLast = index == days.size - 1,
            )
        }
    }
}

@Composable
private fun DayRow(
    day: AppDayOfWeek,
    state: ScheduleUiState,
    templateOrder: List<Long>,
    onIntent: (ScheduleIntent) -> Unit,
    isLast: Boolean,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val templateId = state.assignments[day]
    val isVisible = templateId != null
    val anchorDay = templateId?.let { tpl ->
        state.assignments.entries.firstOrNull { it.value == tpl && it.key != day }?.key
    }
    val groupColor = if (templateId != null) {
        val idx = templateOrder.indexOf(templateId).coerceAtLeast(0)
        Color(EventColors[idx % EventColors.size].toInt())
    } else colors.lineStrong

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(groupColor),
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = day.fullName(),
                style = TextStyle(
                    color = colors.text,
                    fontSize = typography.body,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(Modifier.height(1.dp))
            if (isVisible) {
                SameAsRow(day = day, anchorDay = anchorDay, state = state, onIntent = onIntent)
            } else {
                BasicText(
                    text = stringResource(Res.string.settings_link_hidden),
                    style = TextStyle(color = colors.textTer, fontSize = typography.labelSmall),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        MobileSwitch(
            checked = isVisible,
            onCheckedChange = { checked ->
                if (checked) onIntent(ScheduleIntent.AssignDayToNewTemplate(day))
                else onIntent(ScheduleIntent.HideDay(day))
            },
        )
    }
    if (!isLast) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))
    }
}

@Composable
private fun SameAsRow(
    day: AppDayOfWeek,
    anchorDay: AppDayOfWeek?,
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    var menuOpen by remember { mutableStateOf(false) }
    val visibleOthers = state.assignments.keys
        .filter { it != day }
        .sortedBy { it.isoIndex }

    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicText(
            text = stringResource(Res.string.settings_link_same_as),
            style = TextStyle(color = colors.textTer, fontSize = typography.labelSmall),
        )
        Spacer(Modifier.width(6.dp))
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { menuOpen = true }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = anchorDay?.fullName() ?: stringResource(Res.string.settings_link_independent),
                    style = TextStyle(
                        color = colors.accent,
                        fontSize = typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            MobileDropdownMenu(
                expanded = menuOpen,
                onDismiss = { menuOpen = false },
                offset = IntOffset(x = 0, y = 0),
            ) {
                MobileMenuItem(
                    label = stringResource(Res.string.settings_link_independent),
                    onClick = {
                        menuOpen = false
                        onIntent(ScheduleIntent.AssignDayToNewTemplate(day))
                    },
                )
                visibleOthers.forEach { other ->
                    val otherTpl = state.assignments[other] ?: return@forEach
                    MobileMenuItem(
                        label = other.fullName(),
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

@Composable
private fun DataSection(
    onReset: () -> Unit,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography

    SectionHeading(text = stringResource(Res.string.settings_data_section))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.line, RoundedCornerShape(8.dp)),
    ) {
        DataRow(
            label = stringResource(Res.string.action_export),
            color = colors.accent,
            onClick = { onIntent(ScheduleIntent.ExportData) },
        )
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))
        DataRow(
            label = stringResource(Res.string.action_import),
            color = colors.accent,
            onClick = { onIntent(ScheduleIntent.ImportData) },
        )
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))
        DataRow(
            label = stringResource(Res.string.action_reset_database),
            color = colors.danger,
            onClick = onReset,
        )
    }
}

@Composable
private fun DataRow(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    val typography = MobileTheme.typography
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = color,
                fontSize = typography.body,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun DriveSection(
    status: GoogleDriveStatus,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    var showRestoreConfirm by remember { mutableStateOf(false) }

    SectionHeading(text = stringResource(Res.string.settings_drive_section))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.line, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (status) {
            is GoogleDriveStatus.Disconnected -> {
                BasicText(
                    text = stringResource(Res.string.settings_drive_disconnected),
                    style = TextStyle(color = colors.textSec, fontSize = typography.bodySmall),
                )
                DriveButton(
                    label = stringResource(Res.string.action_drive_connect),
                    primary = true,
                    onClick = { onIntent(ScheduleIntent.ConnectGoogleDrive) },
                )
            }
            is GoogleDriveStatus.Connecting -> {
                BasicText(
                    text = stringResource(Res.string.settings_drive_connecting),
                    style = TextStyle(color = colors.textSec, fontSize = typography.bodySmall),
                )
            }
            is GoogleDriveStatus.Connected -> {
                val accountText = status.email
                    ?.let { stringResource(Res.string.settings_drive_connected, it) }
                    ?: stringResource(Res.string.settings_drive_connected_anon)
                BasicText(
                    text = accountText,
                    style = TextStyle(color = colors.text, fontSize = typography.body, fontWeight = FontWeight.Medium),
                )
                val statusText = when (status.operation) {
                    GoogleDriveStatus.Operation.Uploading ->
                        stringResource(Res.string.settings_drive_uploading)
                    GoogleDriveStatus.Operation.Restoring ->
                        stringResource(Res.string.settings_drive_restoring)
                    null -> when (val ts = status.lastBackupEpochSec) {
                        null -> stringResource(Res.string.settings_drive_no_backup)
                        else -> stringResource(Res.string.settings_drive_last_backup, formatEpochSec(ts))
                    }
                }
                BasicText(
                    text = statusText,
                    style = TextStyle(color = colors.textSec, fontSize = typography.bodySmall),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DriveButton(
                        label = stringResource(Res.string.action_drive_backup_now),
                        primary = true,
                        enabled = status.operation == null,
                        onClick = { onIntent(ScheduleIntent.BackupNowToDrive) },
                    )
                    DriveButton(
                        label = stringResource(Res.string.action_drive_restore),
                        primary = false,
                        enabled = status.operation == null,
                        onClick = { showRestoreConfirm = true },
                    )
                }
                DriveButton(
                    label = stringResource(Res.string.action_drive_disconnect),
                    primary = false,
                    onClick = { onIntent(ScheduleIntent.DisconnectGoogleDrive) },
                )
            }
            is GoogleDriveStatus.Error -> {
                BasicText(
                    text = stringResource(Res.string.settings_drive_error, status.message),
                    style = TextStyle(color = colors.danger, fontSize = typography.bodySmall),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DriveButton(
                        label = stringResource(Res.string.action_drive_retry),
                        primary = true,
                        onClick = { onIntent(ScheduleIntent.ConnectGoogleDrive) },
                    )
                    DriveButton(
                        label = stringResource(Res.string.action_drive_disconnect),
                        primary = false,
                        onClick = { onIntent(ScheduleIntent.DisconnectGoogleDrive) },
                    )
                }
            }
        }
    }

    if (showRestoreConfirm) {
        ConfirmDialog(
            title = stringResource(Res.string.restore_confirm_title),
            message = stringResource(Res.string.restore_confirm_message),
            confirmLabel = stringResource(Res.string.action_restore_confirm),
            cancelLabel = stringResource(Res.string.action_cancel),
            onConfirm = {
                showRestoreConfirm = false
                onIntent(ScheduleIntent.RestoreFromDrive)
            },
            onDismiss = { showRestoreConfirm = false },
        )
    }
}

@Composable
private fun DriveButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val bg = if (primary) colors.accent else Color.Transparent
    val fg = if (primary) Color.White else colors.text
    val borderColor = if (primary) Color.Transparent else colors.lineStrong
    val alpha = if (enabled) 1f else 0.45f
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg.copy(alpha = bg.alpha * alpha))
            .border(1.dp, borderColor.copy(alpha = borderColor.alpha * alpha), RoundedCornerShape(6.dp))
            .let { if (enabled) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = fg.copy(alpha = alpha),
                fontSize = typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun formatEpochSec(epochSec: Long): String {
    val instant = Instant.fromEpochSeconds(epochSec)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val pad = { v: Int -> v.toString().padStart(2, '0') }
    return "${pad(local.hour)}:${pad(local.minute)} · ${pad(local.dayOfMonth)}/${pad(local.monthNumber)}"
}
