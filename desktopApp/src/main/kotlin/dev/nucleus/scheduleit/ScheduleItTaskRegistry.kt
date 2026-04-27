package dev.nucleus.scheduleit

import io.github.kdroidfilter.nucleus.scheduler.TaskId
import io.github.kdroidfilter.nucleus.scheduler.TaskRegistry

object ScheduleItTaskRegistry {
    val EventNotificationsId = TaskId("event-notifications")

    val registry: TaskRegistry = TaskRegistry.Builder()
        .register(EventNotificationsId) { EventNotificationTask() }
        .build()
}
