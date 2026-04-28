package dev.nucleus.scheduleit.ui.jewel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.ui.common.TimeGrid
import dev.nucleus.scheduleit.ui.common.TimeGridDimensions
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.common.localizedWeekOrder
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_paste_event
import scheduleit.shared.generated.resources.empty_schedule_hint

@Composable
fun JewelScheduleHost() {
    val viewModel: ScheduleViewModel = metroViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val visibleDays = localizedWeekOrder().filter { it in state.assignments }
    val pasteLabel = stringResource(Res.string.action_paste_event)

    Box(modifier = Modifier.fillMaxSize()) {
        if (visibleDays.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(Res.string.empty_schedule_hint))
            }
        } else {
            TimeGrid(
                visibleDays = visibleDays,
                startMinute = state.settings.startMinute,
                endMinute = state.settings.endMinute,
                eventsForDay = { day -> state.effectiveEventsFor(day) },
                dayHeader = { day -> Text(day.fullName()) },
                hourLabel = { hour -> Text(dev.nucleus.scheduleit.ui.common.formatHourLabel(hour)) },
                eventCell = { event, _ ->
                    JewelEventCell(
                        event = event,
                        onEdit = { viewModel.onEvent(ScheduleIntent.RequestEditEffectiveEvent(event)) },
                        onCopy = { viewModel.onEvent(ScheduleIntent.CopyEvent(event)) },
                        onDelete = { viewModel.onEvent(ScheduleIntent.DeleteEffectiveEvent(event)) },
                    )
                },
                onSlotClick = { day, minute ->
                    viewModel.onEvent(ScheduleIntent.RequestCreateEvent(day, minute))
                },
                backgroundColor = JewelTheme.globalColors.panelBackground,
                modifier = Modifier.fillMaxSize(),
                dimensions = TimeGridDimensions(
                    gridLineColor = JewelTheme.globalColors.borders.normal,
                    slotHighlight = JewelTheme.globalColors.borders.disabled,
                ),
                pasteEventLabel = state.clipboard?.let { pasteLabel },
                onSlotPaste = state.clipboard?.let {
                    { day, minute -> viewModel.onEvent(ScheduleIntent.PasteEventAt(day, minute)) }
                },
            )
        }
    }

    state.editor?.let { editor ->
        JewelEventEditor(
            editor = editor,
            settings = state.settings,
            siblings = state.effectiveEventsFor(editor.day),
            errorMessage = state.errorMessage,
            onIntent = viewModel::onEvent,
        )
    }

    if (state.showSettings) {
        JewelSettingsWindow(
            state = state,
            onIntent = viewModel::onEvent,
        )
    }
}
