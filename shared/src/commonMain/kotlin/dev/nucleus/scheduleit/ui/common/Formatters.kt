package dev.nucleus.scheduleit.ui.common

import androidx.compose.runtime.Composable
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.day_friday
import scheduleit.shared.generated.resources.day_monday
import scheduleit.shared.generated.resources.day_saturday
import scheduleit.shared.generated.resources.day_short_friday
import scheduleit.shared.generated.resources.day_short_monday
import scheduleit.shared.generated.resources.day_short_saturday
import scheduleit.shared.generated.resources.day_short_sunday
import scheduleit.shared.generated.resources.day_short_thursday
import scheduleit.shared.generated.resources.day_short_tuesday
import scheduleit.shared.generated.resources.day_short_wednesday
import scheduleit.shared.generated.resources.day_sunday
import scheduleit.shared.generated.resources.day_thursday
import scheduleit.shared.generated.resources.day_tuesday
import scheduleit.shared.generated.resources.day_wednesday

fun AppDayOfWeek.fullNameRes(): StringResource = when (this) {
    AppDayOfWeek.Monday -> Res.string.day_monday
    AppDayOfWeek.Tuesday -> Res.string.day_tuesday
    AppDayOfWeek.Wednesday -> Res.string.day_wednesday
    AppDayOfWeek.Thursday -> Res.string.day_thursday
    AppDayOfWeek.Friday -> Res.string.day_friday
    AppDayOfWeek.Saturday -> Res.string.day_saturday
    AppDayOfWeek.Sunday -> Res.string.day_sunday
}

fun AppDayOfWeek.shortNameRes(): StringResource = when (this) {
    AppDayOfWeek.Monday -> Res.string.day_short_monday
    AppDayOfWeek.Tuesday -> Res.string.day_short_tuesday
    AppDayOfWeek.Wednesday -> Res.string.day_short_wednesday
    AppDayOfWeek.Thursday -> Res.string.day_short_thursday
    AppDayOfWeek.Friday -> Res.string.day_short_friday
    AppDayOfWeek.Saturday -> Res.string.day_short_saturday
    AppDayOfWeek.Sunday -> Res.string.day_short_sunday
}

@Composable
fun AppDayOfWeek.fullName(): String = stringResource(fullNameRes())

@Composable
fun AppDayOfWeek.shortName(): String = stringResource(shortNameRes())

fun formatTime(totalMinutes: Int): String {
    val hours = (totalMinutes / 60).coerceIn(0, 23)
    val minutes = (totalMinutes % 60).coerceIn(0, 59)
    return buildString {
        if (hours < 10) append('0')
        append(hours)
        append(':')
        if (minutes < 10) append('0')
        append(minutes)
    }
}

fun formatHourLabel(hour: Int): String = formatTime(hour * 60)
