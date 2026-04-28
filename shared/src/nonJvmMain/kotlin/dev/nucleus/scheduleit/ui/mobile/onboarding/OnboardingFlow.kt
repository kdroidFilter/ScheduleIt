package dev.nucleus.scheduleit.ui.mobile.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.data.drive.GoogleDriveStatus
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleUiState
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import kotlinx.coroutines.delay

private enum class OnboardingStep { Welcome, Setup, Drive }

/**
 * Three-step onboarding flow. Steps are linear and the back-stack lives in
 * a single [rememberSaveable] int so the whole UX survives configuration
 * changes without pulling in a navigation library.
 */
@Composable
fun OnboardingFlow(
    state: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var stepOrdinal by rememberSaveable { mutableStateOf(OnboardingStep.Welcome.ordinal) }
    val step = OnboardingStep.entries[stepOrdinal]

    val finish: () -> Unit = { onIntent(ScheduleIntent.CompleteOnboarding) }
    val advance: () -> Unit = {
        val next = stepOrdinal + 1
        if (next < OnboardingStep.entries.size) stepOrdinal = next else finish()
    }
    val goBack: () -> Unit = {
        if (stepOrdinal > 0) stepOrdinal -= 1
    }

    PlatformBackHandler(enabled = stepOrdinal > 0, onBack = goBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MobileTheme.colors.bg),
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
                OnboardingStep.Welcome -> WelcomeScreen(
                    stepIndex = OnboardingStep.Welcome.ordinal,
                    totalSteps = OnboardingStep.entries.size,
                    onContinue = advance,
                    onSkip = finish,
                )
                OnboardingStep.Setup -> SetupScreen(
                    state = state,
                    stepIndex = OnboardingStep.Setup.ordinal,
                    totalSteps = OnboardingStep.entries.size,
                    onIntent = onIntent,
                    onContinue = advance,
                )
                OnboardingStep.Drive -> DriveScreen(
                    driveStatus = state.googleDrive,
                    stepIndex = OnboardingStep.Drive.ordinal,
                    totalSteps = OnboardingStep.entries.size,
                    onConnect = { onIntent(ScheduleIntent.ConnectGoogleDrive) },
                    onSkip = finish,
                    onConnected = finish,
                )
            }
        }
    }
}

@Composable
internal fun OnboardingScaffold(
    stepIndex: Int,
    totalSteps: Int,
    primaryButton: @Composable () -> Unit,
    secondaryAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            OnboardingProgress(current = stepIndex, total = totalSteps)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
        primaryButton()
        if (secondaryAction != null) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                secondaryAction()
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Auto-finishes the flow when Drive becomes [GoogleDriveStatus.Connected].
 * A short delay keeps the success state visible long enough to feel earned.
 */
@Composable
internal fun AutoFinishOnConnected(
    status: GoogleDriveStatus?,
    onConnected: () -> Unit,
) {
    LaunchedEffect(status) {
        if (status is GoogleDriveStatus.Connected) {
            delay(900)
            onConnected()
        }
    }
}
