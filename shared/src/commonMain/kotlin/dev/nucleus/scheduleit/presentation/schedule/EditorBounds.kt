package dev.nucleus.scheduleit.presentation.schedule

import dev.nucleus.scheduleit.domain.EffectiveEvent
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
    siblings: List<EffectiveEvent>,
    editing: EventEditorState.Original?,
    settings: ScheduleSettings,
): EditorBounds {
    val others = siblings.filterNot { it.matchesOriginal(editing) }
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

fun ScheduleEvent.overlapsAnyOf(
    siblings: List<EffectiveEvent>,
    editing: EventEditorState.Original?,
): Boolean {
    val others = siblings.filterNot { it.matchesOriginal(editing) }
    return others.any { other ->
        startMinute < other.endMinute && endMinute > other.startMinute
    }
}

internal fun EffectiveEvent.matchesOriginal(original: EventEditorState.Original?): Boolean {
    if (original == null) return false
    return when (val s = source) {
        is EffectiveEvent.Source.TemplateShared ->
            original is EventEditorState.Original.TemplateEvent && s.event.id == original.event.id
        is EffectiveEvent.Source.TemplateOverridden ->
            original is EventEditorState.Original.Overridden && s.base.id == original.base.id
        is EffectiveEvent.Source.DayOnly ->
            original is EventEditorState.Original.DayOnly && s.event.id == original.event.id
    }
}
