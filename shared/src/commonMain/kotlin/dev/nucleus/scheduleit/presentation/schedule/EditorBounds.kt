package dev.nucleus.scheduleit.presentation.schedule

import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings

data class EditorBounds(
    val lowerMinute: Int,
    val upperMinute: Int,
    val lowerReason: ErrorKey,
    val upperReason: ErrorKey,
)

fun computeEditorBounds(
    draft: ScheduleEvent,
    siblings: List<ScheduleEvent>,
    settings: ScheduleSettings,
): EditorBounds {
    val others = siblings.filter { it.id != draft.id }
    val prevEnd = others
        .filter { it.endMinute <= draft.startMinute }
        .maxOfOrNull { it.endMinute }
    val nextStart = others
        .filter { it.startMinute >= draft.endMinute }
        .minOfOrNull { it.startMinute }
    val lower = prevEnd ?: settings.startMinute
    val upper = nextStart ?: settings.endMinute
    val lowerReason = if (prevEnd != null) ErrorKey.Overlap else ErrorKey.OutsideWindow
    val upperReason = if (nextStart != null) ErrorKey.Overlap else ErrorKey.OutsideWindow
    return EditorBounds(lower, upper, lowerReason, upperReason)
}

fun ScheduleEvent.overlapsAnyOf(others: List<ScheduleEvent>): Boolean =
    others.any { other ->
        other.id != id &&
            startMinute < other.endMinute &&
            endMinute > other.startMinute
    }
