package dev.nucleus.scheduleit

import dev.nucleus.scheduleit.di.createDesktopAppGraph
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.ui.common.formatTime
import io.github.kdroidfilter.nucleus.notification.common.NotificationManager
import io.github.kdroidfilter.nucleus.notification.common.notification
import io.github.kdroidfilter.nucleus.scheduler.DesktopTask
import org.jetbrains.compose.resources.getString
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.notification_default_title
import scheduleit.shared.generated.resources.notification_starts_at
import io.github.kdroidfilter.nucleus.scheduler.TaskContext
import io.github.kdroidfilter.nucleus.scheduler.TaskResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class EventNotificationTask : DesktopTask {
    override suspend fun doWork(context: TaskContext): TaskResult {
        if (!NotificationManager.isAvailable()) return TaskResult.Success

        val graph = createDesktopAppGraph()
        val snapshot = withTimeoutOrNull(5_000L.milliseconds) {
            graph.repository.observeSchedule().first()
        } ?: return TaskResult.Success

        if (!snapshot.settings.notificationsEnabled) return TaskResult.Success

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = AppDayOfWeek.fromIso(now.dayOfWeek.isoDayNumber)
        val templateId = snapshot.assignments[today] ?: return TaskResult.Success
        val events = snapshot.eventsByTemplate[templateId].orEmpty()

        val slot = ScheduleViewModel.SLOT_MINUTES
        val nowMinute = now.hour * 60 + now.minute
        val windowStart = (nowMinute / slot) * slot
        val windowEnd = windowStart + slot

        val defaultTitle = getString(Res.string.notification_default_title)
        events
            .filter { it.startMinute in windowStart until windowEnd }
            .forEach { event ->
                val title = event.title.ifEmpty { defaultTitle }
                val message = getString(Res.string.notification_starts_at, formatTime(event.startMinute))
                notification(title = title, message = message).send()
            }

        return TaskResult.Success
    }
}
