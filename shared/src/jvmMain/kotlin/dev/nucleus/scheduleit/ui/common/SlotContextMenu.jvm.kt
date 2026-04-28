@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package dev.nucleus.scheduleit.ui.common

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable

@Composable
actual fun SlotContextMenuArea(
    addEventLabel: String,
    onAddEvent: () -> Unit,
    pasteEventLabel: String?,
    onPasteEvent: (() -> Unit)?,
    content: @Composable (onLongPress: (() -> Unit)?) -> Unit,
) {
    ContextMenuArea(
        items = {
            buildList {
                add(ContextMenuItem(addEventLabel, onAddEvent))
                if (pasteEventLabel != null && onPasteEvent != null) {
                    add(ContextMenuItem(pasteEventLabel, onPasteEvent))
                }
            }
        },
        content = { content(null) },
    )
}
