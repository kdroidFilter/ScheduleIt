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
 * Tablet-landscape — same chrome as tablet-portrait but a full 7-day grid.
 * No sidebar (anti-pattern when paired with a bottom-sheet settings flow).
 */
@Composable
fun TabletLandscapeScreen(
    state: ScheduleUiState,
    visibleDays: List<AppDayOfWeek>,
    onIntent: (ScheduleIntent) -> Unit,
    onOpenAbout: () -> Unit,
) {
    val colors = MobileTheme.colors
    val pasteLabel = stringResource(Res.string.action_paste_event)

    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        MobileHeader(
            onOpenSettings = { onIntent(ScheduleIntent.OpenSettings) },
            onOpenAbout = onOpenAbout,
            horizontalPadding = 24.dp,
            verticalPadding = 14.dp,
            logoSize = 26.dp,
            showDivider = true,
        )

        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
