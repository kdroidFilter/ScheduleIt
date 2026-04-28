package dev.nucleus.scheduleit.ui.common

import androidx.compose.runtime.Composable

@Composable
expect fun SlotContextMenuArea(
    addEventLabel: String,
    onAddEvent: () -> Unit,
    pasteEventLabel: String?,
    onPasteEvent: (() -> Unit)?,
    content: @Composable (onLongPress: (() -> Unit)?) -> Unit,
)
