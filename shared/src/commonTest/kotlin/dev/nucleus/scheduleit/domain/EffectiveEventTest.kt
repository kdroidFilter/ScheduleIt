package dev.nucleus.scheduleit.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EffectiveEventTest {

    private val settings = ScheduleSettings(startMinute = 8 * 60, endMinute = 20 * 60)
    private val template = DayTemplate(id = 10L, name = "")
    private val baseEvent = ScheduleEvent(
        id = 100L,
        templateId = template.id,
        title = "Meeting",
        startMinute = 9 * 60,
        endMinute = 10 * 60,
        color = 0xFF42A5F5L,
        notes = "",
    )

    private fun snapshot(
        dayEventsByDay: Map<AppDayOfWeek, List<DayEvent>> = emptyMap(),
        overridesByDay: Map<AppDayOfWeek, Map<Long, TemplateEventOverride>> = emptyMap(),
        events: List<ScheduleEvent> = listOf(baseEvent),
        assignments: Map<AppDayOfWeek, Long> = mapOf(
            AppDayOfWeek.Monday to template.id,
            AppDayOfWeek.Thursday to template.id,
        ),
    ) = ScheduleSnapshot(
        settings = settings,
        templates = listOf(template),
        assignments = assignments,
        eventsByTemplate = events.groupBy { it.templateId },
        dayEventsByDay = dayEventsByDay,
        overridesByDay = overridesByDay,
    )

    @Test
    fun shared_template_event_appears_on_both_days_unchanged() {
        val s = snapshot()
        val mon = s.effectiveEventsFor(AppDayOfWeek.Monday)
        val thu = s.effectiveEventsFor(AppDayOfWeek.Thursday)
        assertEquals(1, mon.size)
        assertEquals(1, thu.size)
        assertIs<EffectiveEvent.Source.TemplateShared>(mon.first().source)
        assertIs<EffectiveEvent.Source.TemplateShared>(thu.first().source)
        assertEquals("Meeting", mon.first().title)
    }

    @Test
    fun override_changes_only_target_day() {
        val s = snapshot(
            overridesByDay = mapOf(
                AppDayOfWeek.Monday to mapOf(
                    baseEvent.id to TemplateEventOverride(
                        baseEventId = baseEvent.id,
                        day = AppDayOfWeek.Monday,
                        hidden = false,
                        title = "Standup",
                        startMinute = null,
                        endMinute = null,
                        color = null,
                        notes = null,
                    ),
                ),
            ),
        )
        val mon = s.effectiveEventsFor(AppDayOfWeek.Monday)
        val thu = s.effectiveEventsFor(AppDayOfWeek.Thursday)
        assertEquals("Standup", mon.first().title)
        assertIs<EffectiveEvent.Source.TemplateOverridden>(mon.first().source)
        assertEquals(9 * 60, mon.first().startMinute)
        assertEquals("Meeting", thu.first().title)
        assertIs<EffectiveEvent.Source.TemplateShared>(thu.first().source)
    }

    @Test
    fun hidden_override_removes_event_for_target_day_only() {
        val s = snapshot(
            overridesByDay = mapOf(
                AppDayOfWeek.Thursday to mapOf(
                    baseEvent.id to TemplateEventOverride(
                        baseEventId = baseEvent.id,
                        day = AppDayOfWeek.Thursday,
                        hidden = true,
                        title = null,
                        startMinute = null,
                        endMinute = null,
                        color = null,
                        notes = null,
                    ),
                ),
            ),
        )
        assertTrue(s.effectiveEventsFor(AppDayOfWeek.Thursday).isEmpty())
        assertEquals(1, s.effectiveEventsFor(AppDayOfWeek.Monday).size)
    }

    @Test
    fun day_only_event_appears_only_on_its_day_and_is_sorted() {
        val morning = DayEvent(
            id = 200L,
            day = AppDayOfWeek.Monday,
            title = "Morning",
            startMinute = 8 * 60,
            endMinute = 9 * 60,
            color = 0xFF66BB6AL,
            notes = "",
        )
        val s = snapshot(
            dayEventsByDay = mapOf(AppDayOfWeek.Monday to listOf(morning)),
        )
        val mon = s.effectiveEventsFor(AppDayOfWeek.Monday)
        assertEquals(2, mon.size)
        assertEquals("Morning", mon[0].title)
        assertIs<EffectiveEvent.Source.DayOnly>(mon[0].source)
        assertEquals("Meeting", mon[1].title)
        assertTrue(s.effectiveEventsFor(AppDayOfWeek.Thursday).none { it.title == "Morning" })
    }
}
