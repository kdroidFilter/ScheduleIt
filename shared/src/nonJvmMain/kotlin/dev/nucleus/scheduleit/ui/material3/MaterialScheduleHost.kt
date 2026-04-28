package dev.nucleus.scheduleit.ui.material3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nucleus.scheduleit.presentation.schedule.ErrorKey
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.ui.common.TimeGrid
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.common.localizedWeekOrder
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_open_settings
import scheduleit.shared.generated.resources.action_paste_event
import scheduleit.shared.generated.resources.empty_schedule_hint
import scheduleit.shared.generated.resources.error_invalid_backup
import scheduleit.shared.generated.resources.error_invalid_range
import scheduleit.shared.generated.resources.error_overlap
import scheduleit.shared.generated.resources.error_outside_window
import scheduleit.shared.generated.resources.schedule_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialScheduleHost() {
    val viewModel: ScheduleViewModel = metroViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val visibleDays = localizedWeekOrder().filter { it in state.assignments }
    val pasteLabel = stringResource(Res.string.action_paste_event)

    val invalidRangeText = stringResource(Res.string.error_invalid_range)
    val outsideWindowText = stringResource(Res.string.error_outside_window)
    val overlapText = stringResource(Res.string.error_overlap)
    val invalidBackupText = stringResource(Res.string.error_invalid_backup)
    LaunchedEffect(state.errorMessage) {
        val msg = when (state.errorMessage) {
            ErrorKey.InvalidRange -> invalidRangeText
            ErrorKey.OutsideWindow -> outsideWindowText
            ErrorKey.Overlap -> overlapText
            ErrorKey.InvalidBackup -> invalidBackupText
            null -> null
        }
        if (msg != null) {
            snackbar.showSnackbar(msg)
            viewModel.onEvent(ScheduleIntent.DismissError)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.schedule_title)) },
                actions = {
                    TextButton(onClick = { viewModel.onEvent(ScheduleIntent.OpenSettings) }) {
                        Text(stringResource(Res.string.action_open_settings))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (visibleDays.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.empty_schedule_hint),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                TimeGrid(
                    visibleDays = visibleDays,
                    startMinute = state.settings.startMinute,
                    endMinute = state.settings.endMinute,
                    eventsForDay = { day -> state.effectiveEventsFor(day) },
                    dayHeader = { day ->
                        Text(
                            text = day.fullName(),
                            style = MaterialTheme.typography.titleSmall,
                        )
                    },
                    hourLabel = { hour ->
                        Text(
                            text = dev.nucleus.scheduleit.ui.common.formatHourLabel(hour),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    eventCell = { event, _ ->
                        MaterialEventCell(
                            event = event,
                            onEdit = { viewModel.onEvent(ScheduleIntent.RequestEditEffectiveEvent(event)) },
                            onCopy = { viewModel.onEvent(ScheduleIntent.CopyEvent(event)) },
                            onDelete = { viewModel.onEvent(ScheduleIntent.DeleteEffectiveEvent(event)) },
                        )
                    },
                    onSlotClick = { day, minute ->
                        viewModel.onEvent(ScheduleIntent.RequestCreateEvent(day, minute))
                    },
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize(),
                    dimensions = dev.nucleus.scheduleit.ui.common.TimeGridDimensions(
                        gridLineColor = MaterialTheme.colorScheme.outline,
                        slotHighlight = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    pasteEventLabel = state.clipboard?.let { pasteLabel },
                    onSlotPaste = state.clipboard?.let {
                        { day, minute -> viewModel.onEvent(ScheduleIntent.PasteEventAt(day, minute)) }
                    },
                )
            }
        }
    }

    state.editor?.let { editor ->
        MaterialEventEditor(
            editor = editor,
            settings = state.settings,
            siblings = state.effectiveEventsFor(editor.day),
            onIntent = viewModel::onEvent,
        )
    }

    if (state.showSettings) {
        MaterialSettingsSheet(
            state = state,
            onIntent = viewModel::onEvent,
        )
    }
}
