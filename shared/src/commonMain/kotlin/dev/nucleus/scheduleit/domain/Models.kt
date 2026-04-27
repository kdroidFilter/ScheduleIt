package dev.nucleus.scheduleit.domain

enum class AppDayOfWeek(val isoIndex: Int) {
    Monday(1),
    Tuesday(2),
    Wednesday(3),
    Thursday(4),
    Friday(5),
    Saturday(6),
    Sunday(7);

    companion object {
        fun fromIso(value: Int): AppDayOfWeek =
            entries.first { it.isoIndex == value }
    }
}

data class ScheduleSettings(
    val startMinute: Int,
    val endMinute: Int,
    val notificationsEnabled: Boolean = false,
) {
    init {
        require(startMinute in 0..1440) { "startMinute out of range" }
        require(endMinute in 0..1440) { "endMinute out of range" }
        require(startMinute < endMinute) { "start must be before end" }
    }

    companion object {
        val Default = ScheduleSettings(startMinute = 8 * 60, endMinute = 20 * 60)
    }
}

data class DayTemplate(
    val id: Long,
    val name: String,
)

data class DayAssignment(
    val day: AppDayOfWeek,
    val templateId: Long,
)

data class ScheduleEvent(
    val id: Long,
    val templateId: Long,
    val title: String,
    val startMinute: Int,
    val endMinute: Int,
    val color: Long,
    val notes: String,
)

data class ScheduleSnapshot(
    val settings: ScheduleSettings,
    val templates: List<DayTemplate>,
    val assignments: Map<AppDayOfWeek, Long>,
    val eventsByTemplate: Map<Long, List<ScheduleEvent>>,
) {
    val visibleDays: List<AppDayOfWeek>
        get() = AppDayOfWeek.entries.filter { it in assignments }
}
