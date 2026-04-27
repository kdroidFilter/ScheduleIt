package dev.nucleus.scheduleit.ui.common

import androidx.compose.runtime.Composable

@Composable
expect fun SlotContextMenuArea(
    addEventLabel: String,
    onAddEvent: () -> Unit,
    content: @Composable () -> Unit,
)
