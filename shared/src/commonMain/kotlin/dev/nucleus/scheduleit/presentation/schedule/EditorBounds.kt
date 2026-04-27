package dev.nucleus.scheduleit.presentation.schedule

import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings

data class EditorBounds(val lowerMinute: Int, val upperMinute: Int)

fun computeEditorBounds(
    draft: ScheduleEvent,
    siblings: List<ScheduleEvent>,
    settings: ScheduleSettings,
): EditorBounds {
    val others = siblings.filter { it.id != draft.id }
    val lower = others
        .filter { it.endMinute <= draft.startMinute }
        .maxOfOrNull { it.endMinute }
        ?: settings.startMinute
    val upper = others
        .filter { it.startMinute >= draft.endMinute }
        .minOfOrNull { it.startMinute }
        ?: settings.endMinute
    return EditorBounds(lower, upper)
}

fun ScheduleEvent.overlapsAnyOf(others: List<ScheduleEvent>): Boolean =
    others.any { other ->
        other.id != id &&
            startMinute < other.endMinute &&
            endMinute > other.startMinute
    }
