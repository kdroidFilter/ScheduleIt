package dev.nucleus.scheduleit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kdroidfilter.nucleus.menu.macos.NativeKeyShortcut
import io.github.kdroidfilter.nucleus.menu.macos.NativeMenuBar
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_export
import scheduleit.shared.generated.resources.action_import
import scheduleit.shared.generated.resources.action_open_settings
import scheduleit.shared.generated.resources.app_name
import scheduleit.shared.generated.resources.menu_file
import scheduleit.shared.generated.resources.menu_help
import scheduleit.shared.generated.resources.menu_new_event
import scheduleit.shared.generated.resources.menu_quit
import scheduleit.shared.generated.resources.menu_view
import scheduleit.shared.generated.resources.menu_window
import scheduleit.shared.generated.resources.settings_notifications_label

@OptIn(ExperimentalTime::class)
@Composable
fun ScheduleItMenuBar(onQuit: () -> Unit) {
    val viewModel: ScheduleViewModel = metroViewModel()
    val state by viewModel.state.collectAsState()

    val appName = stringResource(Res.string.app_name)
    val fileTitle = stringResource(Res.string.menu_file)
    val viewTitle = stringResource(Res.string.menu_view)
    val windowTitle = stringResource(Res.string.menu_window)
    val helpTitle = stringResource(Res.string.menu_help)
    val newEventTitle = stringResource(Res.string.menu_new_event)
    val importTitle = stringResource(Res.string.action_import)
    val exportTitle = stringResource(Res.string.action_export)
    val quitTitle = stringResource(Res.string.menu_quit, appName)
    val settingsTitle = stringResource(Res.string.action_open_settings)
    val notificationsTitle = stringResource(Res.string.settings_notifications_label)

    val targetDay = pickTargetDay(state.assignments.keys)
    val canCreate = targetDay != null
    val canExport = state.eventsByTemplate.values.any { it.isNotEmpty() } ||
        state.dayEventsByDay.values.any { it.isNotEmpty() } ||
        state.overridesByDay.isNotEmpty()

    NativeMenuBar {
        Menu(fileTitle) {
            Item(
                text = newEventTitle,
                shortcut = NativeKeyShortcut("n"),
                enabled = canCreate,
                onClick = {
                    val day = targetDay ?: return@Item
                    viewModel.onEvent(
                        ScheduleIntent.RequestCreateEvent(day, state.settings.startMinute),
                    )
                },
            )
            Separator()
            Item(
                text = importTitle,
                shortcut = NativeKeyShortcut("o"),
                onClick = { viewModel.onEvent(ScheduleIntent.ImportData) },
            )
            Item(
                text = exportTitle,
                shortcut = NativeKeyShortcut("e"),
                enabled = canExport,
                onClick = { viewModel.onEvent(ScheduleIntent.ExportData) },
            )
            Separator()
            Item(
                text = quitTitle,
                shortcut = NativeKeyShortcut("q"),
                onClick = onQuit,
            )
        }
        Menu(viewTitle) {
            Item(
                text = settingsTitle,
                shortcut = NativeKeyShortcut(","),
                onClick = { viewModel.onEvent(ScheduleIntent.OpenSettings) },
            )
            Separator()
            CheckboxItem(
                text = notificationsTitle,
                checked = state.settings.notificationsEnabled,
                onCheckedChange = { enabled ->
                    viewModel.onEvent(ScheduleIntent.SetNotificationsEnabled(enabled))
                },
            )
        }
        MenuWindow(windowTitle)
        MenuHelp(helpTitle)
    }
}

@OptIn(ExperimentalTime::class)
private fun pickTargetDay(visibleDays: Set<AppDayOfWeek>): AppDayOfWeek? {
    if (visibleDays.isEmpty()) return null
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val today = AppDayOfWeek.fromIso(now.dayOfWeek.isoDayNumber)
    return if (today in visibleDays) today else AppDayOfWeek.entries.first { it in visibleDays }
}
