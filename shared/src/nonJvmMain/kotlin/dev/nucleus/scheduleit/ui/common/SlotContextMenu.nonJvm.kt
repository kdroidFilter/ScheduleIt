package dev.nucleus.scheduleit.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.nucleus.scheduleit.ui.mobile.components.MobileDropdownMenu
import dev.nucleus.scheduleit.ui.mobile.components.MobileMenuItem

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
        MobileDropdownMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
        ) {
            MobileMenuItem(label = addEventLabel, onClick = {
                expanded = false
                onAddEvent()
            })
            if (pasteEventLabel != null && onPasteEvent != null) {
                MobileMenuItem(label = pasteEventLabel, onClick = {
                    expanded = false
                    onPasteEvent()
                })
            }
        }
    }
}
