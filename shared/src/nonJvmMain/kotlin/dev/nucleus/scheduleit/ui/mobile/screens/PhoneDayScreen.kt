package dev.nucleus.scheduleit.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleUiState
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.mobile.components.DayStrip
import dev.nucleus.scheduleit.ui.mobile.components.IconAdd
import dev.nucleus.scheduleit.ui.mobile.components.MobileHeader
import dev.nucleus.scheduleit.ui.mobile.components.MobileWeekView
import dev.nucleus.scheduleit.ui.mobile.components.horizontalSwipe
import dev.nucleus.scheduleit.ui.mobile.components.rememberDefaultDay
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_add_event
import scheduleit.shared.generated.resources.action_paste_event

/**
 * Phone-portrait layout — header + 7-day strip + single day grid + FAB.
 * Equivalent to phone-day.jsx → PhoneDayView.
 */
@Composable
fun PhoneDayScreen(
    state: ScheduleUiState,
    visibleDays: List<AppDayOfWeek>,
    onIntent: (ScheduleIntent) -> Unit,
    onOpenAbout: () -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val pasteLabel = stringResource(Res.string.action_paste_event)

    val initial = rememberDefaultDay(visibleDays)
    var activeDay by remember(visibleDays.joinToString { it.name }) {
        mutableStateOf(initial)
    }
    if (activeDay !in visibleDays && visibleDays.isNotEmpty()) {
        activeDay = visibleDays.first()
    }
    val shiftDay: (Int) -> Unit = { delta ->
        val idx = visibleDays.indexOf(activeDay)
        if (idx >= 0) {
            val next = (idx + delta).coerceIn(0, visibleDays.lastIndex)
            if (next != idx) activeDay = visibleDays[next]
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MobileHeader(
                onOpenSettings = { onIntent(ScheduleIntent.OpenSettings) },
                onOpenAbout = onOpenAbout,
            )
            DayStrip(
                days = visibleDays,
                activeDay = activeDay,
                onPick = { activeDay = it },
            )
            DayTitle(
                day = activeDay,
                eventCount = state.effectiveEventsFor(activeDay).size,
                startMinute = state.settings.startMinute,
                endMinute = state.settings.endMinute,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalSwipe(
                        onSwipeLeft = { shiftDay(+1) },
                        onSwipeRight = { shiftDay(-1) },
                    ),
            ) {
                MobileWeekView(
                    visibleDays = listOf(activeDay),
                    activeDay = activeDay,
                    startMinute = state.settings.startMinute,
                    endMinute = state.settings.endMinute,
                    eventsForDay = { d -> state.effectiveEventsFor(d) },
                    onSlotClick = { d, minute ->
                        onIntent(ScheduleIntent.RequestCreateEvent(d, minute))
                    },
                    onEventEdit = { onIntent(ScheduleIntent.RequestEditEffectiveEvent(it)) },
                    onEventCopy = { onIntent(ScheduleIntent.CopyEvent(it)) },
                    onEventDelete = { onIntent(ScheduleIntent.DeleteEffectiveEvent(it)) },
                    pasteEventLabel = state.clipboard?.let { pasteLabel },
                    onSlotPaste = state.clipboard?.let {
                        { d, minute -> onIntent(ScheduleIntent.PasteEventAt(d, minute)) }
                    },
                    showDayHeader = false,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        Fab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 18.dp),
            label = stringResource(Res.string.action_add_event),
            onClick = {
                onIntent(ScheduleIntent.RequestCreateEvent(activeDay, state.settings.startMinute))
            },
        )
    }
}

@Composable
private fun DayTitle(
    day: AppDayOfWeek,
    eventCount: Int,
    startMinute: Int,
    endMinute: Int,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val startHour = startMinute / 60
    val endHour = endMinute / 60
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        BasicText(
            text = day.fullName(),
            style = TextStyle(
                color = colors.text,
                fontSize = typography.displayLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            ),
        )
        Spacer(Modifier.height(2.dp))
        val pad = { v: Int -> v.toString().padStart(2, '0') }
        BasicText(
            text = "$eventCount events · ${pad(startHour)}:00 – ${pad(endHour)}:00",
            style = TextStyle(
                color = colors.textSec,
                fontSize = typography.label,
                letterSpacing = 0.2.sp,
            ),
        )
    }
}

@Composable
private fun Fab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconAdd(modifier = Modifier.size(14.dp), color = Color.White)
        BasicText(
            text = label,
            style = TextStyle(
                color = Color.White,
                fontSize = typography.body,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}
