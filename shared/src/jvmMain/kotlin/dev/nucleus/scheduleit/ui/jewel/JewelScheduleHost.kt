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
import dev.nucleus.scheduleit.ui.common.fullName
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.empty_schedule_hint

@Composable
fun JewelScheduleHost() {
    val viewModel: ScheduleViewModel = metroViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.visibleDays.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(Res.string.empty_schedule_hint))
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
                dayHeader = { day -> Text(day.fullName()) },
                hourLabel = { hour -> Text(dev.nucleus.scheduleit.ui.common.formatHourLabel(hour)) },
                eventCell = { event, _ ->
                    JewelEventCell(
                        event = event,
                        onEdit = { viewModel.onEvent(ScheduleIntent.RequestEditEvent(event)) },
                        onDelete = { viewModel.onEvent(ScheduleIntent.DeleteEvent(event.id)) },
                    )
                },
                onSlotClick = { day, minute ->
                    viewModel.onEvent(ScheduleIntent.RequestCreateEvent(day, minute))
                },
                backgroundColor = JewelTheme.globalColors.panelBackground,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    state.editor?.let { editor ->
        JewelEventEditor(
            editor = editor,
            settings = state.settings,
            siblings = state.eventsByTemplate[editor.templateId].orEmpty(),
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
