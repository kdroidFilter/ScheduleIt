package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.nucleus.scheduleit.domain.EffectiveEvent
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_copy_event
import scheduleit.shared.generated.resources.action_delete_event
import scheduleit.shared.generated.resources.action_edit

/**
 * Self-contained event tile: filled chip + long-press menu (Edit / Copy / Delete).
 * Used as the `eventCell` slot of [MobileWeekView] so the menu lives at the cell
 * level and can anchor near the press location.
 */
@Composable
fun MobileEventCell(
    event: EffectiveEvent,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    compact: Boolean = false,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val editLabel = stringResource(Res.string.action_edit)
    val copyLabel = stringResource(Res.string.action_copy_event)
    val deleteLabel = stringResource(Res.string.action_delete_event)

    Box(modifier = Modifier.fillMaxSize()) {
        EventChip(
            event = event,
            onEdit = onEdit,
            onLongPress = { menuOpen = true },
            compact = compact,
        )
        MobileDropdownMenu(expanded = menuOpen, onDismiss = { menuOpen = false }) {
            MobileMenuItem(label = editLabel, onClick = { menuOpen = false; onEdit() })
            MobileMenuItem(label = copyLabel, onClick = { menuOpen = false; onCopy() })
            MobileMenuItem(label = deleteLabel, onClick = { menuOpen = false; onDelete() }, danger = true)
        }
    }
}
