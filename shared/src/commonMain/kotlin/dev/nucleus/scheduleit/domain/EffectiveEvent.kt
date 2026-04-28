package dev.nucleus.scheduleit.domain

data class EffectiveEvent(
    val day: AppDayOfWeek,
    val title: String,
    val startMinute: Int,
    val endMinute: Int,
    val color: Long,
    val notes: String,
    val source: Source,
) {
    sealed interface Source {
        data class TemplateShared(val event: ScheduleEvent) : Source
        data class TemplateOverridden(
            val base: ScheduleEvent,
            val override: TemplateEventOverride,
        ) : Source
        data class DayOnly(val event: DayEvent) : Source
    }
}

fun ScheduleSnapshot.effectiveEventsFor(day: AppDayOfWeek): List<EffectiveEvent> {
    val templateId = assignments[day]
    val overrides = overridesByDay[day].orEmpty()

    val fromTemplate: List<EffectiveEvent> = if (templateId == null) {
        emptyList()
    } else {
        eventsByTemplate[templateId].orEmpty().mapNotNull { base ->
            val override = overrides[base.id]
            when {
                override == null -> EffectiveEvent(
                    day = day,
                    title = base.title,
                    startMinute = base.startMinute,
                    endMinute = base.endMinute,
                    color = base.color,
                    notes = base.notes,
                    source = EffectiveEvent.Source.TemplateShared(base),
                )
                override.hidden -> null
                else -> EffectiveEvent(
                    day = day,
                    title = override.title ?: base.title,
                    startMinute = override.startMinute ?: base.startMinute,
                    endMinute = override.endMinute ?: base.endMinute,
                    color = override.color ?: base.color,
                    notes = override.notes ?: base.notes,
                    source = EffectiveEvent.Source.TemplateOverridden(base, override),
                )
            }
        }
    }

    val fromDay: List<EffectiveEvent> = dayEventsByDay[day].orEmpty().map { e ->
        EffectiveEvent(
            day = day,
            title = e.title,
            startMinute = e.startMinute,
            endMinute = e.endMinute,
            color = e.color,
            notes = e.notes,
            source = EffectiveEvent.Source.DayOnly(e),
        )
    }

    return (fromTemplate + fromDay).sortedBy { it.startMinute }
}
