package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.EffectiveEvent
import dev.nucleus.scheduleit.ui.common.TimeGrid
import dev.nucleus.scheduleit.ui.common.TimeGridDimensions
import dev.nucleus.scheduleit.ui.common.formatHourLabel
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Wraps the shared [TimeGrid] with mobile-themed slots:
 *  - Day-name header in uppercase with accentSoft tint for the active day
 *  - Tabular-numeric hour labels in the textTer color
 *  - Filled [EventChip] event cells with long-press support
 *
 * The same composable is reused by phone-portrait (single column),
 * phone-landscape, tablet-portrait (3 days) and tablet-landscape (7 days).
 */
@Composable
fun MobileWeekView(
    visibleDays: List<AppDayOfWeek>,
    activeDay: AppDayOfWeek?,
    startMinute: Int,
    endMinute: Int,
    eventsForDay: (AppDayOfWeek) -> List<EffectiveEvent>,
    onSlotClick: (AppDayOfWeek, Int) -> Unit,
    onEventEdit: (EffectiveEvent) -> Unit,
    onEventCopy: (EffectiveEvent) -> Unit,
    onEventDelete: (EffectiveEvent) -> Unit,
    pasteEventLabel: String? = null,
    onSlotPaste: ((AppDayOfWeek, Int) -> Unit)? = null,
    showDayHeader: Boolean = true,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors

    TimeGrid(
        visibleDays = visibleDays,
        startMinute = startMinute,
        endMinute = endMinute,
        eventsForDay = eventsForDay,
        dayHeader = { day ->
            if (showDayHeader) {
                val active = day == activeDay
                val bg = if (active) colors.accentSoft else colors.bgAlt
                val fg = if (active) colors.accent else colors.textSec
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bg),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = day.fullName().uppercase(),
                        style = TextStyle(
                            color = fg,
                            fontSize = if (compact) 10.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                        ),
                    )
                }
            }
        },
        hourLabel = { hour ->
            BasicText(
                text = formatHourLabel(hour),
                style = TextStyle(
                    color = colors.textTer,
                    fontSize = if (compact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp,
                ),
            )
        },
        eventCell = { event, _ ->
            MobileEventCell(
                event = event,
                onEdit = { onEventEdit(event) },
                onCopy = { onEventCopy(event) },
                onDelete = { onEventDelete(event) },
                compact = compact,
            )
        },
        onSlotClick = onSlotClick,
        backgroundColor = colors.bg,
        modifier = modifier,
        dimensions = TimeGridDimensions(
            hourHeight = if (compact) 64.dp else 96.dp,
            hourColumnWidth = 44.dp,
            headerHeight = if (showDayHeader) 36.dp else 0.dp,
            gridLineColor = colors.line,
            slotHighlight = colors.accentSoft,
        ),
        pasteEventLabel = pasteEventLabel,
        onSlotPaste = onSlotPaste,
    )
}

/** Hides the unused-import lint for [Color]. */
@Suppress("unused")
private val ColorRef: Color = Color.Unspecified
