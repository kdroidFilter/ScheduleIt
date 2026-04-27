package dev.nucleus.scheduleit.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import dev.nucleus.scheduleit.db.ScheduleDatabase
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.DayTemplate
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings
import dev.nucleus.scheduleit.domain.ScheduleSnapshot
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

        return combine(settingsFlow, templatesFlow, assignmentsFlow, eventsFlow) { s, t, a, e ->
            ScheduleSnapshot(s, t, a, e)
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

            ScheduleSnapshot(
                settings = ScheduleSettings(
                    startMinute = s.start_minute.toInt(),
                    endMinute = s.end_minute.toInt(),
                    notificationsEnabled = s.notifications_enabled != 0L,
                ),
                templates = templates,
                assignments = assignments,
                eventsByTemplate = events,
            )
        }
    }

    suspend fun resetAll() = withContext(ioDispatcher) {
        database.transaction {
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
        }
    }

    private companion object {
        const val DEFAULT_START_MINUTE = 480L
        const val DEFAULT_END_MINUTE = 1200L
    }
}
