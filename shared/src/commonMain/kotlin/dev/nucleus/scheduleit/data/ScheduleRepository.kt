package dev.nucleus.scheduleit.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import dev.nucleus.scheduleit.db.ScheduleDatabase
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.DayEvent
import dev.nucleus.scheduleit.domain.DayTemplate
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings
import dev.nucleus.scheduleit.domain.ScheduleSnapshot
import dev.nucleus.scheduleit.domain.TemplateEventOverride
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
@Inject
class ScheduleRepository(
    private val database: ScheduleDatabase,
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
    private val queries get() = database.schemaQueries

    fun observeSchedule(): Flow<ScheduleSnapshot> {
        val settingsFlow = queries.selectSettings()
            .asFlow()
            .mapToOne(ioDispatcher)
            .map {
                ScheduleSettings(
                    startMinute = it.start_minute.toInt(),
                    endMinute = it.end_minute.toInt(),
                    notificationsEnabled = it.notifications_enabled != 0L,
                )
            }

        val templatesFlow = queries.selectAllTemplates()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.map { DayTemplate(id = it.id, name = it.name) } }

        val assignmentsFlow = queries.selectAllAssignments()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows ->
                rows.associate { AppDayOfWeek.fromIso(it.day.toInt()) to it.template_id }
            }

        val eventsFlow = queries.selectAllEvents()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows ->
                rows.map {
                    ScheduleEvent(
                        id = it.id,
                        templateId = it.template_id,
                        title = it.title,
                        startMinute = it.start_minute.toInt(),
                        endMinute = it.end_minute.toInt(),
                        color = it.color,
                        notes = it.notes,
                    )
                }.groupBy { it.templateId }
            }

        val dayEventsFlow = queries.selectAllDayEvents()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows ->
                rows.map { row ->
                    DayEvent(
                        id = row.id,
                        day = AppDayOfWeek.fromIso(row.day.toInt()),
                        title = row.title,
                        startMinute = row.start_minute.toInt(),
                        endMinute = row.end_minute.toInt(),
                        color = row.color,
                        notes = row.notes,
                    )
                }.groupBy { it.day }
            }

        val overridesFlow = queries.selectAllOverrides()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows ->
                rows.map { row ->
                    TemplateEventOverride(
                        baseEventId = row.base_event_id,
                        day = AppDayOfWeek.fromIso(row.day.toInt()),
                        hidden = row.hidden != 0L,
                        title = row.title,
                        startMinute = row.start_minute?.toInt(),
                        endMinute = row.end_minute?.toInt(),
                        color = row.color,
                        notes = row.notes,
                    )
                }.groupBy { it.day }.mapValues { (_, list) ->
                    list.associateBy { it.baseEventId }
                }
            }

        val eventsBundleFlow = combine(eventsFlow, dayEventsFlow, overridesFlow) { byTpl, byDay, ov ->
            Triple(byTpl, byDay, ov)
        }

        return combine(settingsFlow, templatesFlow, assignmentsFlow, eventsBundleFlow) { s, t, a, bundle ->
            ScheduleSnapshot(
                settings = s,
                templates = t,
                assignments = a,
                eventsByTemplate = bundle.first,
                dayEventsByDay = bundle.second,
                overridesByDay = bundle.third,
            )
        }.flowOn(ioDispatcher)
    }

    suspend fun ensureDefaults() = withContext(ioDispatcher) {
        database.transaction {
            val existing = queries.selectAllAssignments().executeAsList()
            if (existing.isEmpty()) {
                AppDayOfWeek.entries.forEach { day ->
                    queries.insertTemplate("")
                    val id = queries.lastInsertedId().executeAsOne()
                    queries.upsertAssignment(day.isoIndex.toLong(), id)
                }
            }
        }
    }

    suspend fun updateSettings(settings: ScheduleSettings) = withContext(ioDispatcher) {
        queries.updateSettings(settings.startMinute.toLong(), settings.endMinute.toLong())
    }

    suspend fun createTemplate(name: String = ""): Long = withContext(ioDispatcher) {
        database.transactionWithResult {
            queries.insertTemplate(name)
            queries.lastInsertedId().executeAsOne()
        }
    }

    suspend fun renameTemplate(id: Long, name: String) = withContext(ioDispatcher) {
        queries.renameTemplate(name, id)
    }

    suspend fun deleteTemplateIfEmpty(id: Long) = withContext(ioDispatcher) {
        database.transaction {
            val count = queries.countAssignmentsForTemplate(id).executeAsOne()
            if (count == 0L) queries.deleteTemplate(id)
        }
    }

    suspend fun assignDayToTemplate(day: AppDayOfWeek, templateId: Long) = withContext(ioDispatcher) {
        queries.upsertAssignment(day.isoIndex.toLong(), templateId)
    }

    suspend fun hideDay(day: AppDayOfWeek) = withContext(ioDispatcher) {
        queries.deleteAssignment(day.isoIndex.toLong())
    }

    suspend fun upsertEvent(event: ScheduleEvent): Long = withContext(ioDispatcher) {
        if (event.id == 0L) {
            database.transactionWithResult {
                queries.insertEvent(
                    event.templateId,
                    event.title,
                    event.startMinute.toLong(),
                    event.endMinute.toLong(),
                    event.color,
                    event.notes,
                )
                queries.lastInsertedId().executeAsOne()
            }
        } else {
            queries.updateEvent(
                event.title,
                event.startMinute.toLong(),
                event.endMinute.toLong(),
                event.color,
                event.notes,
                event.id,
            )
            event.id
        }
    }

    suspend fun deleteEvent(id: Long) = withContext(ioDispatcher) {
        queries.deleteEvent(id)
    }

    suspend fun upsertDayEvent(event: DayEvent): Long = withContext(ioDispatcher) {
        if (event.id == 0L) {
            database.transactionWithResult {
                queries.insertDayEvent(
                    event.day.isoIndex.toLong(),
                    event.title,
                    event.startMinute.toLong(),
                    event.endMinute.toLong(),
                    event.color,
                    event.notes,
                )
                queries.lastInsertedId().executeAsOne()
            }
        } else {
            queries.updateDayEvent(
                event.day.isoIndex.toLong(),
                event.title,
                event.startMinute.toLong(),
                event.endMinute.toLong(),
                event.color,
                event.notes,
                event.id,
            )
            event.id
        }
    }

    suspend fun deleteDayEvent(id: Long) = withContext(ioDispatcher) {
        queries.deleteDayEvent(id)
    }

    suspend fun upsertOverride(override: TemplateEventOverride) = withContext(ioDispatcher) {
        queries.upsertOverride(
            override.baseEventId,
            override.day.isoIndex.toLong(),
            if (override.hidden) 1L else 0L,
            override.title,
            override.startMinute?.toLong(),
            override.endMinute?.toLong(),
            override.color,
            override.notes,
        )
    }

    suspend fun deleteOverride(baseEventId: Long, day: AppDayOfWeek) = withContext(ioDispatcher) {
        queries.deleteOverride(baseEventId, day.isoIndex.toLong())
    }

    suspend fun hideTemplateEventForDay(baseEventId: Long, day: AppDayOfWeek) {
        upsertOverride(
            TemplateEventOverride(
                baseEventId = baseEventId,
                day = day,
                hidden = true,
                title = null,
                startMinute = null,
                endMinute = null,
                color = null,
                notes = null,
            ),
        )
    }

    suspend fun promoteDayEventToTemplate(dayEventId: Long, templateId: Long): Long = withContext(ioDispatcher) {
        database.transactionWithResult {
            val rows = queries.selectAllDayEvents().executeAsList()
            val source = rows.firstOrNull { it.id == dayEventId }
                ?: error("DayEvent $dayEventId not found")
            queries.insertEvent(
                templateId,
                source.title,
                source.start_minute,
                source.end_minute,
                source.color,
                source.notes,
            )
            val newId = queries.lastInsertedId().executeAsOne()
            queries.deleteDayEvent(dayEventId)
            newId
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        queries.updateNotificationsEnabled(if (enabled) 1L else 0L)
    }

    suspend fun snapshotOnce(): ScheduleSnapshot = withContext(ioDispatcher) {
        database.transactionWithResult {
            val s = queries.selectSettings().executeAsOne()
            val templates = queries.selectAllTemplates().executeAsList()
                .map { DayTemplate(id = it.id, name = it.name) }
            val assignments = queries.selectAllAssignments().executeAsList()
                .associate { AppDayOfWeek.fromIso(it.day.toInt()) to it.template_id }
            val events = queries.selectAllEvents().executeAsList().map {
                ScheduleEvent(
                    id = it.id,
                    templateId = it.template_id,
                    title = it.title,
                    startMinute = it.start_minute.toInt(),
                    endMinute = it.end_minute.toInt(),
                    color = it.color,
                    notes = it.notes,
                )
            }.groupBy { it.templateId }
            val dayEvents = queries.selectAllDayEvents().executeAsList().map { row ->
                DayEvent(
                    id = row.id,
                    day = AppDayOfWeek.fromIso(row.day.toInt()),
                    title = row.title,
                    startMinute = row.start_minute.toInt(),
                    endMinute = row.end_minute.toInt(),
                    color = row.color,
                    notes = row.notes,
                )
            }.groupBy { it.day }
            val overrides = queries.selectAllOverrides().executeAsList().map { row ->
                TemplateEventOverride(
                    baseEventId = row.base_event_id,
                    day = AppDayOfWeek.fromIso(row.day.toInt()),
                    hidden = row.hidden != 0L,
                    title = row.title,
                    startMinute = row.start_minute?.toInt(),
                    endMinute = row.end_minute?.toInt(),
                    color = row.color,
                    notes = row.notes,
                )
            }.groupBy { it.day }.mapValues { (_, list) ->
                list.associateBy { it.baseEventId }
            }

            ScheduleSnapshot(
                settings = ScheduleSettings(
                    startMinute = s.start_minute.toInt(),
                    endMinute = s.end_minute.toInt(),
                    notificationsEnabled = s.notifications_enabled != 0L,
                ),
                templates = templates,
                assignments = assignments,
                eventsByTemplate = events,
                dayEventsByDay = dayEvents,
                overridesByDay = overrides,
            )
        }
    }

    suspend fun resetAll() = withContext(ioDispatcher) {
        database.transaction {
            queries.deleteAllOverrides()
            queries.deleteAllDayEvents()
            queries.deleteAllEvents()
            queries.deleteAllAssignments()
            queries.deleteAllTemplates()
            queries.updateSettings(DEFAULT_START_MINUTE, DEFAULT_END_MINUTE)
            queries.updateNotificationsEnabled(0L)
            AppDayOfWeek.entries.forEach { day ->
                queries.insertTemplate("")
                val id = queries.lastInsertedId().executeAsOne()
                queries.upsertAssignment(day.isoIndex.toLong(), id)
            }
        }
    }

    suspend fun replaceAll(snapshot: ScheduleSnapshot) = withContext(ioDispatcher) {
        database.transaction {
            queries.deleteAllOverrides()
            queries.deleteAllDayEvents()
            queries.deleteAllEvents()
            queries.deleteAllAssignments()
            queries.deleteAllTemplates()

            queries.updateSettings(
                snapshot.settings.startMinute.toLong(),
                snapshot.settings.endMinute.toLong(),
            )
            queries.updateNotificationsEnabled(if (snapshot.settings.notificationsEnabled) 1L else 0L)

            snapshot.templates.forEach { tpl ->
                queries.insertTemplateWithId(tpl.id, tpl.name)
            }
            snapshot.assignments.forEach { (day, templateId) ->
                queries.upsertAssignment(day.isoIndex.toLong(), templateId)
            }
            snapshot.eventsByTemplate.values.flatten().forEach { e ->
                queries.insertEventWithId(
                    e.id,
                    e.templateId,
                    e.title,
                    e.startMinute.toLong(),
                    e.endMinute.toLong(),
                    e.color,
                    e.notes,
                )
            }
            snapshot.dayEventsByDay.values.flatten().forEach { e ->
                queries.insertDayEventWithId(
                    e.id,
                    e.day.isoIndex.toLong(),
                    e.title,
                    e.startMinute.toLong(),
                    e.endMinute.toLong(),
                    e.color,
                    e.notes,
                )
            }
            snapshot.overridesByDay.values.flatMap { it.values }.forEach { ov ->
                queries.upsertOverride(
                    ov.baseEventId,
                    ov.day.isoIndex.toLong(),
                    if (ov.hidden) 1L else 0L,
                    ov.title,
                    ov.startMinute?.toLong(),
                    ov.endMinute?.toLong(),
                    ov.color,
                    ov.notes,
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_START_MINUTE = 480L
        const val DEFAULT_END_MINUTE = 1200L
    }
}
