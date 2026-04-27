package dev.nucleus.scheduleit.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_add_event
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel

private const val SLOT_PER_HOUR = 60 / ScheduleViewModel.SLOT_MINUTES

data class TimeGridDimensions(
    val hourHeight: Dp = 96.dp,
    val hourColumnWidth: Dp = 60.dp,
    val headerHeight: Dp = 40.dp,
    val gridLineColor: Color = Color(0x22000000),
    val slotHighlight: Color = Color(0x08000000),
)

@Composable
fun TimeGrid(
    visibleDays: List<AppDayOfWeek>,
    startMinute: Int,
    endMinute: Int,
    eventsForDay: (AppDayOfWeek) -> List<ScheduleEvent>,
    dayHeader: @Composable (AppDayOfWeek) -> Unit,
    hourLabel: @Composable (hour: Int) -> Unit,
    eventCell: @Composable (ScheduleEvent, AppDayOfWeek) -> Unit,
    onSlotClick: (AppDayOfWeek, startMinute: Int) -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    dimensions: TimeGridDimensions = TimeGridDimensions(),
) {
    val hours = remember(startMinute, endMinute) {
        val first = startMinute / 60
        val last = (endMinute + 59) / 60
        (first until last).toList()
    }
    val baseMinute = hours.first() * 60
    val totalHeight = dimensions.hourHeight * hours.size

    Column(modifier = modifier.background(backgroundColor)) {
        Row(modifier = Modifier.fillMaxWidth().height(dimensions.headerHeight)) {
            Spacer(Modifier.width(dimensions.hourColumnWidth))
            visibleDays.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    dayHeader(day)
                }
            }
        }
        ScheduleScrollableContainer(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                HourColumn(
                    hours = hours,
                    hourHeight = dimensions.hourHeight,
                    width = dimensions.hourColumnWidth,
                    hourLabel = hourLabel,
                )
                visibleDays.forEach { day ->
                    DayColumn(
                        baseMinute = baseMinute,
                        totalHeight = totalHeight,
                        hours = hours,
                        hourHeight = dimensions.hourHeight,
                        gridLineColor = dimensions.gridLineColor,
                        slotHighlight = dimensions.slotHighlight,
                        events = eventsForDay(day),
                        eventCell = { event -> eventCell(event, day) },
                        onSlotClick = { minute -> onSlotClick(day, minute) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HourColumn(
    hours: List<Int>,
    hourHeight: Dp,
    width: Dp,
    hourLabel: @Composable (hour: Int) -> Unit,
) {
    Column(modifier = Modifier.width(width)) {
        hours.forEach { hour ->
            Box(
                modifier = Modifier.fillMaxWidth().height(hourHeight).padding(top = 2.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                hourLabel(hour)
            }
        }
    }
}

@Composable
private fun DayColumn(
    baseMinute: Int,
    totalHeight: Dp,
    hours: List<Int>,
    hourHeight: Dp,
    gridLineColor: Color,
    slotHighlight: Color,
    events: List<ScheduleEvent>,
    eventCell: @Composable (ScheduleEvent) -> Unit,
    onSlotClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val pxPerMinute = with(density) { hourHeight.toPx() / 60f }
    Box(modifier = modifier.height(totalHeight)) {
        Column(modifier = Modifier.fillMaxSize()) {
            hours.forEach { hour ->
                Column(modifier = Modifier.fillMaxWidth().height(hourHeight)) {
                    val hourStart = hour * 60
                    repeat(SLOT_PER_HOUR) { slotIdx ->
                        val slotStart = hourStart + slotIdx * ScheduleViewModel.SLOT_MINUTES
                        HoverableSlot(
                            slotStart = slotStart,
                            slotIdx = slotIdx,
                            hourHeight = hourHeight,
                            gridLineColor = gridLineColor,
                            slotHighlight = slotHighlight,
                            onSlotClick = onSlotClick,
                        )
                    }
                }
            }
        }

        events.forEach { event ->
            val topDp = with(density) { ((event.startMinute - baseMinute) * pxPerMinute).toDp() }
            val heightDp = with(density) { ((event.endMinute - event.startMinute) * pxPerMinute).toDp() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                    .offset(y = topDp)
                    .height(heightDp),
            ) {
                eventCell(event)
            }
        }
    }
}

@Composable
private fun HoverableSlot(
    slotStart: Int,
    slotIdx: Int,
    hourHeight: Dp,
    gridLineColor: Color,
    slotHighlight: Color,
    onSlotClick: (Int) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val baseColor = if (slotIdx == 0) gridLineColor.copy(alpha = 0.05f) else slotHighlight
    val color = if (isHovered) gridLineColor.copy(alpha = 0.18f) else baseColor
    val addEventLabel = stringResource(Res.string.action_add_event)
    SlotContextMenuArea(
        addEventLabel = addEventLabel,
        onAddEvent = { onSlotClick(slotStart) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(hourHeight / SLOT_PER_HOUR)
                .background(color)
                .hoverable(interactionSource)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(interactionSource = interactionSource, indication = null) { onSlotClick(slotStart) },
        )
    }
}
