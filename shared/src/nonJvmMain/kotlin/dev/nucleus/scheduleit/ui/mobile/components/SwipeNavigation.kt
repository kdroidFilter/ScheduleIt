package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

/**
 * Horizontal-swipe pager modifier. Triggers [onSwipeLeft] / [onSwipeRight] when
 * the cumulative drag crosses [thresholdDp] in either direction. Vertical drags
 * are left alone so the underlying time grid keeps scrolling normally.
 */
@Composable
fun Modifier.horizontalSwipe(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    thresholdDp: Int = 56,
): Modifier {
    val thresholdPx = with(LocalDensity.current) { thresholdDp.dp.toPx() }
    return this.pointerInput(onSwipeLeft, onSwipeRight) {
        var total = 0f
        detectHorizontalDragGestures(
            onDragStart = { total = 0f },
            onDragEnd = {
                when {
                    total > thresholdPx -> onSwipeRight()
                    total < -thresholdPx -> onSwipeLeft()
                }
            },
            onDragCancel = { total = 0f },
            onHorizontalDrag = { _, drag -> total += drag },
        )
    }
}

/** Returns today's day-of-week if it's visible, else the first visible day. */
@OptIn(ExperimentalTime::class)
@Composable
fun rememberDefaultDay(visibleDays: List<AppDayOfWeek>): AppDayOfWeek {
    val key = visibleDays.joinToString { it.name }
    return remember(key) {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .dayOfWeek
        val todayApp = AppDayOfWeek.fromIso(today.isoDayNumber)
        if (todayApp in visibleDays) todayApp else (visibleDays.firstOrNull() ?: AppDayOfWeek.Monday)
    }
}
