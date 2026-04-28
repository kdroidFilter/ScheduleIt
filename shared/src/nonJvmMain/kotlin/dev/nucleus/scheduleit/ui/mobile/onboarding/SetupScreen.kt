package dev.nucleus.scheduleit.ui.mobile.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleUiState
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.common.localizedWeekOrder
import dev.nucleus.scheduleit.ui.mobile.components.MobileHourPicker
import dev.nucleus.scheduleit.ui.mobile.components.MobileSwitch
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.onboarding_setup_continue
import scheduleit.shared.generated.resources.onboarding_setup_days_label
import scheduleit.shared.generated.resources.onboarding_setup_hint
import scheduleit.shared.generated.resources.onboarding_setup_hours_label
import scheduleit.shared.generated.resources.onboarding_setup_subtitle
import scheduleit.shared.generated.resources.onboarding_setup_title
import scheduleit.shared.generated.resources.settings_end_hour
import scheduleit.shared.generated.resources.settings_start_hour

@Composable
internal fun SetupScreen(
    state: ScheduleUiState,
    stepIndex: Int,
    totalSteps: Int,
    onIntent: (ScheduleIntent) -> Unit,
    onContinue: () -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val startHour = state.settings.startMinute / 60
    val endHour = state.settings.endMinute / 60

    OnboardingScaffold(
        stepIndex = stepIndex,
        totalSteps = totalSteps,
        primaryButton = {
            OnboardingPrimaryButton(
                label = stringResource(Res.string.onboarding_setup_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.assignments.isNotEmpty(),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(
                text = stringResource(Res.string.onboarding_setup_title),
                style = TextStyle(
                    color = colors.text,
                    fontSize = typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(Modifier.height(6.dp))
            BasicText(
                text = stringResource(Res.string.onboarding_setup_subtitle),
                style = TextStyle(
                    color = colors.textSec,
                    fontSize = typography.body,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(24.dp))
            SectionLabel(text = stringResource(Res.string.onboarding_setup_days_label))
            Spacer(Modifier.height(8.dp))
            DaysCard(state = state, onIntent = onIntent)

            Spacer(Modifier.height(20.dp))
            SectionLabel(text = stringResource(Res.string.onboarding_setup_hours_label))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MobileHourPicker(
                    label = stringResource(Res.string.settings_start_hour),
                    valueHour = startHour,
                    range = 0..(endHour - 1),
                    onChange = { onIntent(ScheduleIntent.ChangeHours(it, endHour)) },
                )
                MobileHourPicker(
                    label = stringResource(Res.string.settings_end_hour),
                    valueHour = endHour,
                    range = (startHour + 1)..24,
                    onChange = { onIntent(ScheduleIntent.ChangeHours(startHour, it)) },
                )
            }

            Spacer(Modifier.height(16.dp))
            BasicText(
                text = stringResource(Res.string.onboarding_setup_hint),
                style = TextStyle(
                    color = colors.textTer,
                    fontSize = typography.labelSmall,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    BasicText(
        text = text.uppercase(),
        style = TextStyle(
            color = colors.textTer,
            fontSize = typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
        ),
    )
}

@Composable
private fun DaysCard(
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val days = localizedWeekOrder()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.line, RoundedCornerShape(10.dp)),
    ) {
        days.forEachIndexed { index, day ->
            val visible = state.assignments.containsKey(day)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = day.fullName(),
                    style = TextStyle(
                        color = colors.text,
                        fontSize = typography.body,
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = Modifier.weight(1f),
                )
                MobileSwitch(
                    checked = visible,
                    onCheckedChange = { checked ->
                        if (checked) onIntent(ScheduleIntent.AssignDayToNewTemplate(day))
                        else onIntent(ScheduleIntent.HideDay(day))
                    },
                )
            }
            if (index != days.lastIndex) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.line),
                )
            }
        }
    }
}
