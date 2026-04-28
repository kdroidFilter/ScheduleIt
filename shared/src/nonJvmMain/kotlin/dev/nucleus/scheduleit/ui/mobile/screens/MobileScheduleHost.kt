package dev.nucleus.scheduleit.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nucleus.scheduleit.presentation.schedule.ErrorKey
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.ui.common.localizedWeekOrder
import dev.nucleus.scheduleit.ui.mobile.components.ConfirmDialog
import dev.nucleus.scheduleit.ui.mobile.components.LoadingIndicator
import dev.nucleus.scheduleit.ui.mobile.onboarding.OnboardingFlow
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_close
import scheduleit.shared.generated.resources.empty_schedule_hint
import scheduleit.shared.generated.resources.error_invalid_backup
import scheduleit.shared.generated.resources.error_invalid_range
import scheduleit.shared.generated.resources.error_outside_window
import scheduleit.shared.generated.resources.error_overlap

/** Width breakpoints (dp). */
private const val PHONE_MAX_DP = 600
private const val TABLET_PORTRAIT_MAX_DP = 900

/**
 * Root mobile composable. Dispatches between phone-portrait, phone-landscape,
 * tablet-portrait and tablet-landscape based on the available width × height.
 */
@Composable
fun MobileScheduleHost() {
    val viewModel: ScheduleViewModel = metroViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val visibleDays = localizedWeekOrder().filter { it in state.assignments }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    val openAbout: () -> Unit = { showAbout = true }

    val showOnboarding = !state.isLoading && !state.settings.onboardingCompleted

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileTheme.colors.bg)
            .safeDrawingPadding(),
    ) {
        val isLandscape = maxWidth > maxHeight
        val widthDp = maxWidth.value.toInt()

        when {
            state.isLoading -> LoadingState()
            showOnboarding -> OnboardingFlow(state = state, onIntent = viewModel::onEvent)
            visibleDays.isEmpty() -> EmptyScheduleHint(onIntent = viewModel::onEvent)
            widthDp < PHONE_MAX_DP && !isLandscape ->
                PhoneDayScreen(state, visibleDays, viewModel::onEvent, openAbout)
            widthDp < TABLET_PORTRAIT_MAX_DP && isLandscape ->
                PhoneLandscapeScreen(state, visibleDays, viewModel::onEvent, openAbout)
            widthDp < TABLET_PORTRAIT_MAX_DP ->
                TabletPortraitScreen(state, visibleDays, viewModel::onEvent, openAbout)
            else ->
                TabletLandscapeScreen(state, visibleDays, viewModel::onEvent, openAbout)
        }
    }

    if (showAbout && !showOnboarding) {
        MobileAboutDialog(onDismiss = { showAbout = false })
    }

    // Modal overlays — always mounted so the exit animation can play out.
    // Suppressed during onboarding to keep the flow uncluttered.
    if (!showOnboarding) {
        MobileEventEditor(
            editor = state.editor,
            settings = state.settings,
            siblings = state.editor?.day?.let { state.effectiveEventsFor(it) } ?: emptyList(),
            onIntent = viewModel::onEvent,
        )

        MobileSettingsSheet(
            visible = state.showSettings,
            state = state,
            onIntent = viewModel::onEvent,
        )

        ErrorBanner(
            errorMessage = state.errorMessage,
            onDismiss = { viewModel.onEvent(ScheduleIntent.DismissError) },
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator(color = MobileTheme.colors.accent)
    }
}

@Composable
private fun EmptyScheduleHint(onIntent: (ScheduleIntent) -> Unit) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(
            text = stringResource(Res.string.empty_schedule_hint),
            style = TextStyle(
                color = colors.textSec,
                fontSize = typography.body,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun ErrorBanner(
    errorMessage: ErrorKey?,
    onDismiss: () -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    if (errorMessage == null) return
    val text = when (errorMessage) {
        ErrorKey.InvalidRange -> stringResource(Res.string.error_invalid_range)
        ErrorKey.OutsideWindow -> stringResource(Res.string.error_outside_window)
        ErrorKey.Overlap -> stringResource(Res.string.error_overlap)
        ErrorKey.InvalidBackup -> stringResource(Res.string.error_invalid_backup)
    }
    ConfirmDialog(
        title = "",
        message = text,
        confirmLabel = stringResource(Res.string.action_close),
        cancelLabel = "",
        onConfirm = onDismiss,
        onDismiss = onDismiss,
    )
}

@Composable
@Suppress("unused")
private fun KeepFontWeightImported(): FontWeight = FontWeight.SemiBold
