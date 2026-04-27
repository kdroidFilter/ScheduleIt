package dev.nucleus.scheduleit.ui.material3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nucleus.scheduleit.presentation.schedule.ErrorKey
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.ui.common.TimeGrid
import dev.nucleus.scheduleit.ui.common.fullName
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_open_settings
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
            if (state.visibleDays.isEmpty()) {
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
                    visibleDays = state.visibleDays,
                    startMinute = state.settings.startMinute,
                    endMinute = state.settings.endMinute,
                    eventsForDay = { day ->
                        val tpl = state.assignments[day] ?: return@TimeGrid emptyList()
                        state.eventsByTemplate[tpl].orEmpty()
                    },
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
                        Surface(
                            color = Color(event.color.toInt()),
                            contentColor = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { viewModel.onEvent(ScheduleIntent.RequestEditEvent(event)) },
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                                Text(
                                    text = event.title.ifEmpty { "—" },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    softWrap = false,
                                )
                            }
                        }
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
                )
            }
        }
    }

    state.editor?.let { editor ->
        MaterialEventEditor(
            editor = editor,
            settings = state.settings,
            siblings = state.eventsByTemplate[editor.templateId].orEmpty(),
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
