package dev.nucleus.scheduleit

import dev.nucleus.scheduleit.data.ScheduleRepository
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import io.github.kdroidfilter.nucleus.scheduler.DesktopTaskScheduler
import io.github.kdroidfilter.nucleus.scheduler.TaskRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

fun CoroutineScope.startSchedulerSync(repository: ScheduleRepository): Job = launch {
    val taskId = ScheduleItTaskRegistry.EventNotificationsId
    repository.observeSchedule()
        .map { it.settings.notificationsEnabled }
        .distinctUntilChanged()
        .collect { enabled ->
            runCatching {
                val isScheduled = DesktopTaskScheduler.isScheduled(taskId)
                when {
                    enabled && !isScheduled -> DesktopTaskScheduler.enqueue(
                        TaskRequest.periodic(taskId, ScheduleViewModel.SLOT_MINUTES.minutes),
                    )
                    !enabled && isScheduled -> DesktopTaskScheduler.cancel(taskId)
                }
            }
        }
}
