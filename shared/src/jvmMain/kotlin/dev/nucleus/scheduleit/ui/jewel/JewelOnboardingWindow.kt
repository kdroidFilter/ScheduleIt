package dev.nucleus.scheduleit.ui.jewel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import dev.nucleus.scheduleit.data.drive.GoogleDriveStatus
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleUiState
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.common.localizedWeekOrder
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kdroidfilter.nucleus.window.DecoratedWindowScope
import io.github.kdroidfilter.nucleus.window.jewel.JewelDecoratedWindow
import io.github.kdroidfilter.nucleus.window.jewel.JewelTitleBar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.app_name
import scheduleit.shared.generated.resources.onboarding_drive_connect_cta
import scheduleit.shared.generated.resources.onboarding_drive_connecting
import scheduleit.shared.generated.resources.onboarding_drive_error
import scheduleit.shared.generated.resources.onboarding_drive_finish_later
import scheduleit.shared.generated.resources.onboarding_drive_open_app
import scheduleit.shared.generated.resources.onboarding_drive_privacy
import scheduleit.shared.generated.resources.onboarding_drive_subtitle
import scheduleit.shared.generated.resources.onboarding_drive_success
import scheduleit.shared.generated.resources.onboarding_drive_title
import scheduleit.shared.generated.resources.onboarding_setup_continue
import scheduleit.shared.generated.resources.onboarding_setup_days_label
import scheduleit.shared.generated.resources.onboarding_setup_hint
import scheduleit.shared.generated.resources.onboarding_setup_hours_label
import scheduleit.shared.generated.resources.onboarding_setup_subtitle
import scheduleit.shared.generated.resources.onboarding_setup_title
import scheduleit.shared.generated.resources.onboarding_skip
import scheduleit.shared.generated.resources.onboarding_welcome_cta
import scheduleit.shared.generated.resources.onboarding_welcome_subtitle
import scheduleit.shared.generated.resources.onboarding_welcome_title
import scheduleit.shared.generated.resources.settings_end_hour
import scheduleit.shared.generated.resources.settings_start_hour

private enum class OnboardingStep { Welcome, Setup, Drive }

private val WINDOW_SIZE = DpSize(640.dp, 760.dp)
private val MIN_WINDOW_SIZE = DpSize(560.dp, 680.dp)

private val PALETTE = listOf(
    Color(0xFF42A5F5L.toInt()),
    Color(0xFF66BB6AL.toInt()),
    Color(0xFFFFA726L.toInt()),
    Color(0xFFAB47BCL.toInt()),
    Color(0xFFEF5350L.toInt()),
)

/**
 * Dedicated onboarding window for the desktop app. Mirrors the mobile flow
 * (Welcome → Setup → Drive) but uses Jewel components and lives in its own
 * top-level [JewelDecoratedWindow] — the caller decides whether to render
 * this window or the main app window based on the persisted onboarding flag.
 */
@Composable
fun JewelOnboardingWindow(viewModelFactory: MetroViewModelFactory) {
    val title = stringResource(Res.string.app_name)
    // Closing the window via its X is treated as a skip — we stash the request
    // and let the inner content dispatch CompleteOnboarding once it has a VM.
    var closeRequested by remember { mutableStateOf(false) }
    JewelDecoratedWindow(
        onCloseRequest = { closeRequested = true },
        state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = WINDOW_SIZE,
        ),
        title = title,
        resizable = true,
        minimumSize = MIN_WINDOW_SIZE,
    ) {
        // LocalViewModelStoreOwner is provided by Compose Desktop per Window,
        // so VM resolution must happen here, not above the Window call.
        CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
            val viewModel: ScheduleViewModel = metroViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(closeRequested) {
                if (closeRequested) viewModel.onEvent(ScheduleIntent.CompleteOnboarding)
            }
            OnboardingWindowContent(
                title = title,
                state = state,
                onIntent = viewModel::onEvent,
            )
        }
    }
}

@Composable
private fun DecoratedWindowScope.OnboardingWindowContent(
    title: String,
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    var stepOrdinal by remember { mutableStateOf(OnboardingStep.Welcome.ordinal) }
    val step = OnboardingStep.entries[stepOrdinal]

    val finish: () -> Unit = { onIntent(ScheduleIntent.CompleteOnboarding) }
    val advance: () -> Unit = {
        val next = stepOrdinal + 1
        if (next < OnboardingStep.entries.size) stepOrdinal = next else finish()
    }
    val goBack: () -> Unit = { if (stepOrdinal > 0) stepOrdinal -= 1 }

    JewelTitleBar { _ -> Text(title) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JewelTheme.globalColors.panelBackground),
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal) {
                    SlideDirection.Left
                } else {
                    SlideDirection.Right
                }
                (slideIntoContainer(direction, tween(280)) + fadeIn(tween(220)))
                    .togetherWith(slideOutOfContainer(direction, tween(280)) + fadeOut(tween(180)))
            },
            label = "onboarding_step",
            modifier = Modifier.fillMaxSize(),
        ) { current ->
            when (current) {
                OnboardingStep.Welcome -> WelcomeStep(
                    stepIndex = OnboardingStep.Welcome.ordinal,
                    totalSteps = OnboardingStep.entries.size,
                    onContinue = advance,
                    onSkip = finish,
                )
                OnboardingStep.Setup -> SetupStep(
                    state = state,
                    stepIndex = OnboardingStep.Setup.ordinal,
                    totalSteps = OnboardingStep.entries.size,
                    onIntent = onIntent,
                    onBack = goBack,
                    onContinue = advance,
                )
                OnboardingStep.Drive -> DriveStep(
                    driveStatus = state.googleDrive,
                    stepIndex = OnboardingStep.Drive.ordinal,
                    totalSteps = OnboardingStep.entries.size,
                    onConnect = { onIntent(ScheduleIntent.ConnectGoogleDrive) },
                    onBack = goBack,
                    onFinish = finish,
                )
            }
        }
    }
}

@Composable
private fun StepScaffold(
    stepIndex: Int,
    totalSteps: Int,
    onBack: (() -> Unit)? = null,
    primary: @Composable () -> Unit,
    secondary: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.CenterStart) {
                if (onBack != null) {
                    OutlinedButton(onClick = onBack) { Text("←") }
                }
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                StepDots(current = stepIndex, total = totalSteps)
            }
            Spacer(Modifier.width(80.dp))
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            primary()
        }
        if (secondary != null) {
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                secondary()
            }
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            val active = index == current
            val width by animateDpAsState(
                targetValue = if (active) 22.dp else 8.dp,
                animationSpec = tween(220),
                label = "dot_width",
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            JewelTheme.globalColors.text.normal
                        } else {
                            JewelTheme.globalColors.borders.normal
                        },
                    ),
            )
        }
    }
}

@Composable
private fun WelcomeStep(
    stepIndex: Int,
    totalSteps: Int,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    StepScaffold(
        stepIndex = stepIndex,
        totalSteps = totalSteps,
        primary = {
            DefaultButton(onClick = onContinue) {
                Text(stringResource(Res.string.onboarding_welcome_cta))
            }
        },
        secondary = {
            OutlinedButton(onClick = onSkip) {
                Text(stringResource(Res.string.onboarding_skip))
            }
        },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            WeekPreview()
            Text(
                text = stringResource(Res.string.onboarding_welcome_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Text(
                text = stringResource(Res.string.onboarding_welcome_subtitle),
                color = JewelTheme.globalColors.text.info,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

@Composable
private fun SetupStep(
    state: ScheduleUiState,
    stepIndex: Int,
    totalSteps: Int,
    onIntent: (ScheduleIntent) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val days = localizedWeekOrder()
    val anyDayVisible = days.any { it in state.assignments }
    val startHour = state.settings.startMinute / 60
    val endHour = state.settings.endMinute / 60

    StepScaffold(
        stepIndex = stepIndex,
        totalSteps = totalSteps,
        onBack = onBack,
        primary = {
            DefaultButton(onClick = onContinue, enabled = anyDayVisible) {
                Text(stringResource(Res.string.onboarding_setup_continue))
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.onboarding_setup_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(Res.string.onboarding_setup_subtitle),
                color = JewelTheme.globalColors.text.info,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.onboarding_setup_days_label),
                    color = JewelTheme.globalColors.text.info,
                )
                DayChipsRow(days = days, state = state, onIntent = onIntent)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.onboarding_setup_hours_label),
                    color = JewelTheme.globalColors.text.info,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                ) {
                    JewelHourPicker(
                        label = stringResource(Res.string.settings_start_hour),
                        valueHour = startHour,
                        range = 0..(endHour - 1),
                        onChange = { onIntent(ScheduleIntent.ChangeHours(it, endHour)) },
                    )
                    JewelHourPicker(
                        label = stringResource(Res.string.settings_end_hour),
                        valueHour = endHour,
                        range = (startHour + 1)..24,
                        onChange = { onIntent(ScheduleIntent.ChangeHours(startHour, it)) },
                    )
                }
            }

            Text(
                text = stringResource(Res.string.onboarding_setup_hint),
                color = JewelTheme.globalColors.text.info,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DayChipsRow(
    days: List<AppDayOfWeek>,
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        days.forEach { day ->
            val visible = day in state.assignments
            val shape = RoundedCornerShape(10.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .border(1.dp, JewelTheme.globalColors.borders.normal, shape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CheckboxRow(
                    checked = visible,
                    onCheckedChange = { checked ->
                        if (checked) onIntent(ScheduleIntent.AssignDayToNewTemplate(day))
                        else onIntent(ScheduleIntent.HideDay(day))
                    },
                    text = day.fullName(),
                )
            }
        }
    }
}

@Composable
private fun DriveStep(
    driveStatus: GoogleDriveStatus?,
    stepIndex: Int,
    totalSteps: Int,
    onConnect: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    val isConnecting = driveStatus is GoogleDriveStatus.Connecting
    val isConnected = driveStatus is GoogleDriveStatus.Connected

    // Once Drive is connected the primary CTA flips to "Open ScheduleIt" so
    // the user always has an explicit, visible way to leave onboarding —
    // no silent auto-advance.
    val primaryLabel = if (isConnected) {
        stringResource(Res.string.onboarding_drive_open_app)
    } else {
        stringResource(Res.string.onboarding_drive_connect_cta)
    }
    val onPrimary: () -> Unit = if (isConnected) onFinish else onConnect

    StepScaffold(
        stepIndex = stepIndex,
        totalSteps = totalSteps,
        onBack = onBack,
        primary = {
            DefaultButton(onClick = onPrimary, enabled = !isConnecting) {
                Text(primaryLabel)
            }
        },
        secondary = if (isConnected) null else {
            {
                OutlinedButton(onClick = onFinish) {
                    Text(stringResource(Res.string.onboarding_drive_finish_later))
                }
            }
        },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            CloudBadge()
            Text(
                text = stringResource(Res.string.onboarding_drive_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.onboarding_drive_subtitle),
                color = JewelTheme.globalColors.text.info,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.onboarding_drive_privacy),
                color = JewelTheme.globalColors.text.info,
                textAlign = TextAlign.Center,
            )

            DriveStatusBlock(status = driveStatus)
        }
    }
}

@Composable
private fun DriveStatusBlock(status: GoogleDriveStatus?) {
    when (status) {
        is GoogleDriveStatus.Connecting -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CircularProgressIndicator()
            Text(
                stringResource(Res.string.onboarding_drive_connecting),
                color = JewelTheme.globalColors.text.info,
            )
        }
        is GoogleDriveStatus.Connected -> Text(
            stringResource(Res.string.onboarding_drive_success),
            color = JewelTheme.globalColors.text.normal,
        )
        is GoogleDriveStatus.Error -> Text(
            stringResource(Res.string.onboarding_drive_error, status.message),
            color = JewelTheme.globalColors.text.error,
            textAlign = TextAlign.Center,
        )
        else -> Spacer(Modifier.height(0.dp))
    }
}

@Composable
private fun WeekPreview() {
    val gridLine = JewelTheme.globalColors.borders.normal
    Box(
        modifier = Modifier
            .size(width = 320.dp, height = 200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, JewelTheme.globalColors.borders.normal, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cols = 5
            val colWidth = size.width / cols
            for (i in 1 until cols) {
                drawLine(
                    color = gridLine,
                    start = Offset(colWidth * i, 0f),
                    end = Offset(colWidth * i, size.height),
                    strokeWidth = 1f,
                )
            }
            val blocks = listOf(
                Triple(0, 0.10f, 0.30f) to PALETTE[0],
                Triple(0, 0.45f, 0.65f) to PALETTE[2],
                Triple(1, 0.20f, 0.50f) to PALETTE[1],
                Triple(2, 0.05f, 0.25f) to PALETTE[3],
                Triple(2, 0.35f, 0.70f) to PALETTE[0],
                Triple(3, 0.15f, 0.45f) to PALETTE[4],
                Triple(3, 0.55f, 0.85f) to PALETTE[1],
                Triple(4, 0.25f, 0.60f) to PALETTE[2],
                Triple(4, 0.70f, 0.95f) to PALETTE[3],
            )
            blocks.forEach { (placement, color) ->
                val (col, top, bottom) = placement
                val x = colWidth * col + 4f
                val y = size.height * top
                val w = colWidth - 8f
                val h = size.height * (bottom - top)
                drawRoundRect(
                    color = color.copy(alpha = 0.85f),
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                )
            }
        }
    }
}

@Composable
private fun CloudBadge() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(PALETTE[0].copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PALETTE[0]),
        )
    }
}
