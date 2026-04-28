package dev.nucleus.scheduleit.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.isoDayNumber
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.mobile.components.IconAdd
import dev.nucleus.scheduleit.ui.mobile.components.MobileHeader
import dev.nucleus.scheduleit.ui.mobile.components.MobileWeekView
import dev.nucleus.scheduleit.ui.mobile.components.horizontalSwipe
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_add_event
import scheduleit.shared.generated.resources.action_paste_event

/**
 * Tablet-portrait — 3-day window with day selector at the top.
 * Equivalent to tablet.jsx → TabletPortrait.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun TabletPortraitScreen(
    state: ScheduleUiState,
    visibleDays: List<AppDayOfWeek>,
    onIntent: (ScheduleIntent) -> Unit,
    onOpenAbout: () -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val pasteLabel = stringResource(Res.string.action_paste_event)

    val initial = remember(visibleDays) {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .dayOfWeek
        val todayApp = AppDayOfWeek.fromIso(today.isoDayNumber)
        if (todayApp in visibleDays) todayApp else visibleDays.firstOrNull()
    }
    var anchor by remember(visibleDays.joinToString { it.name }) {
        mutableStateOf(initial ?: AppDayOfWeek.Monday)
    }
    if (anchor !in visibleDays && visibleDays.isNotEmpty()) {
        anchor = visibleDays.first()
    }
    val anchorIdx = visibleDays.indexOf(anchor).coerceAtLeast(0)
    val windowStart = anchorIdx.coerceAtMost((visibleDays.size - 3).coerceAtLeast(0))
    val window = visibleDays.drop(windowStart).take(3)
    val shiftWindow: (Int) -> Unit = { delta ->
        val nextStart = (windowStart + delta).coerceIn(0, (visibleDays.size - 3).coerceAtLeast(0))
        if (nextStart != windowStart) anchor = visibleDays.getOrNull(nextStart) ?: anchor
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MobileHeader(
                onOpenSettings = { onIntent(ScheduleIntent.OpenSettings) },
                onOpenAbout = onOpenAbout,
                horizontalPadding = 24.dp,
                verticalPadding = 14.dp,
                logoSize = 26.dp,
                showDivider = true,
            )
            DaySelector(
                allDays = visibleDays,
                selectedStart = windowStart,
                onPick = { picked ->
                    val pickedIdx = visibleDays.indexOf(picked)
                    if (pickedIdx >= 0) {
                        val targetStart = pickedIdx.coerceAtMost(
                            (visibleDays.size - 3).coerceAtLeast(0),
                        )
                        if (targetStart != windowStart) anchor = visibleDays[targetStart]
                    }
                },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .horizontalSwipe(
                        onSwipeLeft = { shiftWindow(+3) },
                        onSwipeRight = { shiftWindow(-3) },
                    ),
            ) {
                MobileWeekView(
                    visibleDays = window,
                    activeDay = anchor,
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

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.accent)
                .clickable {
                    onIntent(ScheduleIntent.RequestCreateEvent(anchor, state.settings.startMinute))
                }
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconAdd(modifier = Modifier.size(14.dp), color = Color.White)
            BasicText(
                text = stringResource(Res.string.action_add_event),
                style = TextStyle(
                    color = Color.White,
                    fontSize = typography.body,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

@Composable
private fun DaySelector(
    allDays: List<AppDayOfWeek>,
    selectedStart: Int,
    onPick: (AppDayOfWeek) -> Unit,
) {
    val colors = MobileTheme.colors
    val selected = allDays.drop(selectedStart).take(3).toSet()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgAlt)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        allDays.forEach { day ->
            val active = day in selected
            val bg = if (active) colors.bgElev else Color.Transparent
            val borderColor = if (active) colors.accent else Color.Transparent
            val fg = if (active) colors.accent else colors.textSec
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg)
                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                    .clickable { onPick(day) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = day.fullName().uppercase(),
                    style = TextStyle(
                        color = fg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    ),
                )
            }
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))
}
