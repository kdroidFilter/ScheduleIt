package dev.nucleus.scheduleit

import dev.nucleusframework.scheduler.TaskId
import dev.nucleusframework.scheduler.TaskRegistry

object ScheduleItTaskRegistry {
    val EventNotificationsId = TaskId("event-notifications")

    val registry: TaskRegistry = TaskRegistry.Builder()
        .register(EventNotificationsId) { EventNotificationTask() }
        .build()
}
