@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package dev.nucleus.scheduleit.ui.jewel

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nucleus.scheduleit.domain.EffectiveEvent
import dev.nucleus.scheduleit.ui.common.formatTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_delete_event
import scheduleit.shared.generated.resources.action_edit

@Composable
fun JewelEventCell(
    event: EffectiveEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val baseColor = Color(event.color.toInt())
    val shape = RoundedCornerShape(6.dp)
    val editLabel = stringResource(Res.string.action_edit)
    val deleteLabel = stringResource(Res.string.action_delete_event)
    val menuState = remember { ContextMenuState() }
    val isMenuOpen = menuState.status is ContextMenuState.Status.Open

    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem(editLabel, onEdit),
                ContextMenuItem(deleteLabel, onDelete),
            )
        },
        state = menuState,
    ) {
        val cell: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(baseColor)
                    .border(
                        width = if (isHovered) 2.dp else 0.dp,
                        color = if (isHovered) Color.White.copy(alpha = 0.6f) else Color.Transparent,
                        shape = shape,
                    )
                    .hoverable(interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(interactionSource = interactionSource, indication = null) { onEdit() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                BasicText(
                    text = event.title.ifEmpty { " " },
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both,
                        ),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
            }
        }

        if (!isMenuOpen) {
            val hours = "${formatTime(event.startMinute)} – ${formatTime(event.endMinute)}"
            val tooltipText = if (event.notes.isNotBlank()) "$hours\n${event.notes}" else hours
            Tooltip(
                tooltip = { Text(text = tooltipText) },
                content = { cell() },
            )
        } else {
            cell()
        }
    }
}
