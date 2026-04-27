@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package dev.nucleus.scheduleit.ui.common

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable

@Composable
actual fun SlotContextMenuArea(
    addEventLabel: String,
    onAddEvent: () -> Unit,
    content: @Composable () -> Unit,
) {
    ContextMenuArea(
        items = { listOf(ContextMenuItem(addEventLabel, onAddEvent)) },
        content = content,
    )
}
