package dev.nucleus.scheduleit.ui.mobile.onboarding

import androidx.compose.runtime.Composable

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS uses swipe-back gestures handled by the system; no equivalent here.
}
