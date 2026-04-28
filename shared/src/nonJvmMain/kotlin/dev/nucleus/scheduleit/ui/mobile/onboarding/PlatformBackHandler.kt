package dev.nucleus.scheduleit.ui.mobile.onboarding

import androidx.compose.runtime.Composable

/**
 * Hooks the system back gesture on platforms that surface one (Android).
 * iOS has no system back, so the actual is a no-op there.
 */
@Composable
internal expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
