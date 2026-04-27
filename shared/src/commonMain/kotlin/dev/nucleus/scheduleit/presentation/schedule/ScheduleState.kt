package dev.nucleus.scheduleit.presentation.schedule

import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.DayTemplate
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val settings: ScheduleSettings = ScheduleSettings.Default,
    val templates: List<DayTemplate> = emptyList(),
    val assignments: Map<AppDayOfWeek, Long> = emptyMap(),
    val eventsByTemplate: Map<Long, List<ScheduleEvent>> = emptyMap(),
    val editor: EventEditorState? = null,
    val showSettings: Boolean = false,
    val errorMessage: ErrorKey? = null,
) {
    val visibleDays: List<AppDayOfWeek>
        get() = AppDayOfWeek.entries.filter { it in assignments }
}

data class EventEditorState(
    val mode: Mode,
    val templateId: Long,
    val originDay: AppDayOfWeek,
    val draft: ScheduleEvent,
) {
    enum class Mode { Create, Edit }
}

enum class ErrorKey {
    InvalidRange,
    OutsideWindow,
    Overlap,
    InvalidBackup,
}
