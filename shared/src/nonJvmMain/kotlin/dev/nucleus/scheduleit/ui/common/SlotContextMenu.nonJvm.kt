package dev.nucleus.scheduleit.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
actual fun SlotContextMenuArea(
    addEventLabel: String,
    onAddEvent: () -> Unit,
    pasteEventLabel: String?,
    onPasteEvent: (() -> Unit)?,
    content: @Composable (onLongPress: (() -> Unit)?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        content { expanded = true }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(addEventLabel) },
                onClick = {
                    expanded = false
                    onAddEvent()
                },
            )
            if (pasteEventLabel != null && onPasteEvent != null) {
                DropdownMenuItem(
                    text = { Text(pasteEventLabel) },
                    onClick = {
                        expanded = false
                        onPasteEvent()
                    },
                )
            }
        }
    }
}
