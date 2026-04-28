@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package dev.nucleus.scheduleit.ui.material3

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.domain.EffectiveEvent
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_copy_event
import scheduleit.shared.generated.resources.action_delete_event
import scheduleit.shared.generated.resources.action_edit

@Composable
fun MaterialEventCell(
    event: EffectiveEvent,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val editLabel = stringResource(Res.string.action_edit)
    val copyLabel = stringResource(Res.string.action_copy_event)
    val deleteLabel = stringResource(Res.string.action_delete_event)

    Surface(
        color = Color(event.color.toInt()),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = onEdit,
                onLongClick = { menuOpen = true },
            ),
    ) {
        Box(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
            Text(
                text = event.title.ifEmpty { "—" },
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text(editLabel) },
                    onClick = {
                        menuOpen = false
                        onEdit()
                    },
                )
                DropdownMenuItem(
                    text = { Text(copyLabel) },
                    onClick = {
                        menuOpen = false
                        onCopy()
                    },
                )
                DropdownMenuItem(
                    text = { Text(deleteLabel) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}
