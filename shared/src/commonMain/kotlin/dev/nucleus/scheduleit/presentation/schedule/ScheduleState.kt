package dev.nucleus.scheduleit.presentation.schedule

import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.DayEvent
import dev.nucleus.scheduleit.domain.DayTemplate
import dev.nucleus.scheduleit.domain.EffectiveEvent
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings
import dev.nucleus.scheduleit.domain.ScheduleSnapshot
import dev.nucleus.scheduleit.domain.TemplateEventOverride
import dev.nucleus.scheduleit.domain.effectiveEventsFor

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val settings: ScheduleSettings = ScheduleSettings.Default,
    val templates: List<DayTemplate> = emptyList(),
    val assignments: Map<AppDayOfWeek, Long> = emptyMap(),
    val eventsByTemplate: Map<Long, List<ScheduleEvent>> = emptyMap(),
    val dayEventsByDay: Map<AppDayOfWeek, List<DayEvent>> = emptyMap(),
    val overridesByDay: Map<AppDayOfWeek, Map<Long, TemplateEventOverride>> = emptyMap(),
    val editor: EventEditorState? = null,
    val showSettings: Boolean = false,
    val errorMessage: ErrorKey? = null,
) {
    val visibleDays: List<AppDayOfWeek>
        get() = AppDayOfWeek.entries.filter { it in assignments }

    fun effectiveEventsFor(day: AppDayOfWeek): List<EffectiveEvent> =
        ScheduleSnapshot(
            settings = settings,
            templates = templates,
            assignments = assignments,
            eventsByTemplate = eventsByTemplate,
            dayEventsByDay = dayEventsByDay,
            overridesByDay = overridesByDay,
        ).effectiveEventsFor(day)

    fun isTemplateShared(templateId: Long): Boolean =
        assignments.values.count { it == templateId } >= 2
}

data class EventEditorState(
    val mode: Mode,
    val day: AppDayOfWeek,
    val templateId: Long,
    val draft: ScheduleEvent,
    val scope: Scope,
    val original: Original?,
    val templateIsShared: Boolean,
) {
    enum class Mode { Create, Edit }

    enum class Scope { ThisDayOnly, AllLinkedDays }

    sealed interface Original {
        data class TemplateEvent(val event: ScheduleEvent) : Original
        data class DayOnly(val event: DayEvent) : Original
        data class Overridden(
            val base: ScheduleEvent,
            val override: TemplateEventOverride,
        ) : Original
    }
}

enum class ErrorKey {
    InvalidRange,
    OutsideWindow,
    Overlap,
    InvalidBackup,
}
