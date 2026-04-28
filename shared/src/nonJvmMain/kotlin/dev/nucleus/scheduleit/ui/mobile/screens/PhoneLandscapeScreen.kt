package dev.nucleus.scheduleit.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleUiState
import dev.nucleus.scheduleit.ui.mobile.components.MobileHeader
import dev.nucleus.scheduleit.ui.mobile.components.MobileWeekView
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_paste_event

/**
 * Phone-landscape — compact 7-day grid.
 * Equivalent to ScheduleIt Mobile.html → "iPhone — Landscape".
 */
@Composable
fun PhoneLandscapeScreen(
    state: ScheduleUiState,
    visibleDays: List<AppDayOfWeek>,
    onIntent: (ScheduleIntent) -> Unit,
    onOpenAbout: () -> Unit,
) {
    val colors = MobileTheme.colors
    val pasteLabel = stringResource(Res.string.action_paste_event)

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MobileHeader(
                onOpenSettings = { onIntent(ScheduleIntent.OpenSettings) },
                onOpenAbout = onOpenAbout,
                horizontalPadding = 16.dp,
                verticalPadding = 8.dp,
                logoSize = 20.dp,
            )
            MobileWeekView(
                visibleDays = visibleDays,
                activeDay = visibleDays.firstOrNull(),
                startMinute = state.settings.startMinute,
                endMinute = state.settings.endMinute,
                eventsForDay = { day -> state.effectiveEventsFor(day) },
                onSlotClick = { day, minute ->
                    onIntent(ScheduleIntent.RequestCreateEvent(day, minute))
                },
                onEventEdit = { onIntent(ScheduleIntent.RequestEditEffectiveEvent(it)) },
                onEventCopy = { onIntent(ScheduleIntent.CopyEvent(it)) },
                onEventDelete = { onIntent(ScheduleIntent.DeleteEffectiveEvent(it)) },
                pasteEventLabel = state.clipboard?.let { pasteLabel },
                onSlotPaste = state.clipboard?.let {
                    { day, minute -> onIntent(ScheduleIntent.PasteEventAt(day, minute)) }
                },
                compact = true,
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            )
        }
    }
}
