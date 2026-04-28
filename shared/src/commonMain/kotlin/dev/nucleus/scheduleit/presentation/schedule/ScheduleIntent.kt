package dev.nucleus.scheduleit.presentation.schedule

import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.EffectiveEvent
import dev.nucleus.scheduleit.domain.ScheduleEvent

sealed interface ScheduleIntent {
    data object OpenSettings : ScheduleIntent
    data object CloseSettings : ScheduleIntent

    data class RequestCreateEvent(val day: AppDayOfWeek, val startMinute: Int) : ScheduleIntent
    data class RequestEditEffectiveEvent(val effective: EffectiveEvent) : ScheduleIntent
    data class DeleteEffectiveEvent(val effective: EffectiveEvent) : ScheduleIntent
    data class HideEffectiveEvent(val effective: EffectiveEvent) : ScheduleIntent
    data class UpdateDraft(val draft: ScheduleEvent) : ScheduleIntent
    data class SetEditorScope(val scope: EventEditorState.Scope) : ScheduleIntent
    data object DismissEditor : ScheduleIntent
    data object SaveEditor : ScheduleIntent
    data object DeleteEditor : ScheduleIntent

    data class ChangeHours(val startHour: Int, val endHour: Int) : ScheduleIntent
    data class SetNotificationsEnabled(val enabled: Boolean) : ScheduleIntent
    data object ExportData : ScheduleIntent
    data object ImportData : ScheduleIntent
    data object ResetData : ScheduleIntent
    data class HideDay(val day: AppDayOfWeek) : ScheduleIntent
    data class AssignDayToTemplate(val day: AppDayOfWeek, val templateId: Long) : ScheduleIntent
    data class AssignDayToNewTemplate(val day: AppDayOfWeek) : ScheduleIntent
    data class RenameTemplate(val id: Long, val name: String) : ScheduleIntent

    data object DismissError : ScheduleIntent
    data class ReportBlocked(val reason: ErrorKey) : ScheduleIntent
}
