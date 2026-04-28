package dev.nucleus.scheduleit.data

import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.DayEvent
import dev.nucleus.scheduleit.domain.DayTemplate
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings
import dev.nucleus.scheduleit.domain.ScheduleSnapshot
import dev.nucleus.scheduleit.domain.TemplateEventOverride
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ScheduleBackup(
    val version: Int = CURRENT_VERSION,
    val settings: BackupSettings,
    val templates: List<BackupTemplate>,
    val assignments: List<BackupAssignment>,
    val events: List<BackupEvent>,
    val dayEvents: List<BackupDayEvent> = emptyList(),
    val overrides: List<BackupOverride> = emptyList(),
) {
    companion object {
        const val CURRENT_VERSION = 2
    }
}

@Serializable
data class BackupSettings(
    val startMinute: Int,
    val endMinute: Int,
    val notificationsEnabled: Boolean,
)

@Serializable
data class BackupTemplate(
    val id: Long,
    val name: String,
)

@Serializable
data class BackupAssignment(
    val isoDay: Int,
    val templateId: Long,
)

@Serializable
data class BackupEvent(
    val id: Long,
    val templateId: Long,
    val title: String,
    val startMinute: Int,
    val endMinute: Int,
    val color: Long,
    val notes: String,
)

@Serializable
data class BackupDayEvent(
    val id: Long,
    val isoDay: Int,
    val title: String,
    val startMinute: Int,
    val endMinute: Int,
    val color: Long,
    val notes: String,
)

@Serializable
data class BackupOverride(
    val baseEventId: Long,
    val isoDay: Int,
    val hidden: Boolean,
    val title: String? = null,
    val startMinute: Int? = null,
    val endMinute: Int? = null,
    val color: Long? = null,
    val notes: String? = null,
)

private val backupJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

fun ScheduleSnapshot.toBackup(): ScheduleBackup =
    ScheduleBackup(
        settings = BackupSettings(
            startMinute = settings.startMinute,
            endMinute = settings.endMinute,
            notificationsEnabled = settings.notificationsEnabled,
        ),
        templates = templates.map { BackupTemplate(it.id, it.name) },
        assignments = assignments.map { (day, templateId) ->
            BackupAssignment(day.isoIndex, templateId)
        },
        events = eventsByTemplate.values.flatten().map { e ->
            BackupEvent(
                id = e.id,
                templateId = e.templateId,
                title = e.title,
                startMinute = e.startMinute,
                endMinute = e.endMinute,
                color = e.color,
                notes = e.notes,
            )
        },
        dayEvents = dayEventsByDay.values.flatten().map { e ->
            BackupDayEvent(
                id = e.id,
                isoDay = e.day.isoIndex,
                title = e.title,
                startMinute = e.startMinute,
                endMinute = e.endMinute,
                color = e.color,
                notes = e.notes,
            )
        },
        overrides = overridesByDay.values.flatMap { it.values }.map { ov ->
            BackupOverride(
                baseEventId = ov.baseEventId,
                isoDay = ov.day.isoIndex,
                hidden = ov.hidden,
                title = ov.title,
                startMinute = ov.startMinute,
                endMinute = ov.endMinute,
                color = ov.color,
                notes = ov.notes,
            )
        },
    )

fun ScheduleBackup.toSnapshot(): ScheduleSnapshot =
    ScheduleSnapshot(
        settings = ScheduleSettings(
            startMinute = settings.startMinute,
            endMinute = settings.endMinute,
            notificationsEnabled = settings.notificationsEnabled,
        ),
        templates = templates.map { DayTemplate(it.id, it.name) },
        assignments = assignments.associate {
            AppDayOfWeek.fromIso(it.isoDay) to it.templateId
        },
        eventsByTemplate = events.map { be ->
            ScheduleEvent(
                id = be.id,
                templateId = be.templateId,
                title = be.title,
                startMinute = be.startMinute,
                endMinute = be.endMinute,
                color = be.color,
                notes = be.notes,
            )
        }.groupBy { it.templateId },
        dayEventsByDay = dayEvents.map { be ->
            DayEvent(
                id = be.id,
                day = AppDayOfWeek.fromIso(be.isoDay),
                title = be.title,
                startMinute = be.startMinute,
                endMinute = be.endMinute,
                color = be.color,
                notes = be.notes,
            )
        }.groupBy { it.day },
        overridesByDay = overrides.map { bo ->
            TemplateEventOverride(
                baseEventId = bo.baseEventId,
                day = AppDayOfWeek.fromIso(bo.isoDay),
                hidden = bo.hidden,
                title = bo.title,
                startMinute = bo.startMinute,
                endMinute = bo.endMinute,
                color = bo.color,
                notes = bo.notes,
            )
        }.groupBy { it.day }.mapValues { (_, list) ->
            list.associateBy { it.baseEventId }
        },
    )

fun ScheduleBackup.encodeToString(): String = backupJson.encodeToString(this)

fun decodeBackupFromString(text: String): ScheduleBackup =
    backupJson.decodeFromString(ScheduleBackup.serializer(), text)
