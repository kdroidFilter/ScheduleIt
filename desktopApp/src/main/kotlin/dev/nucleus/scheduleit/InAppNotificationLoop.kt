package dev.nucleus.scheduleit

import dev.nucleus.scheduleit.data.ScheduleRepository
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.ui.common.formatTime
import io.github.kdroidfilter.nucleus.notification.common.NotificationManager
import io.github.kdroidfilter.nucleus.notification.common.notification
import org.jetbrains.compose.resources.getString
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.notification_default_title
import scheduleit.shared.generated.resources.notification_starts_at
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun CoroutineScope.startInAppNotificationLoop(repository: ScheduleRepository): Job = launch {
    val sentInWindow = mutableSetOf<Long>()
    var lastWindowKey = ""
    while (true) {
        runCatching {
            val snapshot = repository.snapshotOnce()
            val isAvailable = NotificationManager.isAvailable()
            if (snapshot.settings.notificationsEnabled && isAvailable) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val nowMinute = now.hour * 60 + now.minute
                val slot = ScheduleViewModel.SLOT_MINUTES
                val windowStart = (nowMinute / slot) * slot
                val windowEnd = windowStart + slot
                val windowKey = "${now.date}-$windowStart"
                if (windowKey != lastWindowKey) {
                    sentInWindow.clear()
                    lastWindowKey = windowKey
                }
                val today = AppDayOfWeek.fromIso(now.dayOfWeek.isoDayNumber)
                val templateId = snapshot.assignments[today]
                if (templateId != null) {
                    val defaultTitle = getString(Res.string.notification_default_title)
                    snapshot.eventsByTemplate[templateId].orEmpty()
                        .filter { it.startMinute in windowStart until windowEnd && it.id !in sentInWindow }
                        .forEach { event ->
                            val title = event.title.ifEmpty { defaultTitle }
                            val message = getString(
                                Res.string.notification_starts_at,
                                formatTime(event.startMinute),
                            )
                            notification(title = title, message = message).send()
                            sentInWindow += event.id
                        }
                }
            }
        }
        delay(20.seconds)
    }
}
