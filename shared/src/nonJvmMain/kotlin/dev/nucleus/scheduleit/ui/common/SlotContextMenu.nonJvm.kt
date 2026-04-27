package dev.nucleus.scheduleit.ui.common

import androidx.compose.runtime.Composable

@Composable
actual fun SlotContextMenuArea(
    addEventLabel: String,
    onAddEvent: () -> Unit,
    content: @Composable () -> Unit,
) {
    content()
}
