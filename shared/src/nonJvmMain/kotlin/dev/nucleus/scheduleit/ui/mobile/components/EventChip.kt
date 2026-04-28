@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.domain.EffectiveEvent
import dev.nucleus.scheduleit.ui.common.formatTime
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Filled event tile shown inside a day column. Layout adapts to the available height:
 *  - >= 44 dp: title + time range stacked
 *  - 28..43 dp: title only (centered)
 *  - < 28 dp: title + time range on same row
 *
 * Equivalent to grid.jsx → EventChip.
 */
@Composable
fun EventChip(
    event: EffectiveEvent,
    onEdit: () -> Unit,
    onLongPress: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val color = Color(event.color.toInt())
    val titleStyle = TextStyle(
        color = Color.White,
        fontSize = if (compact) MobileTheme.typography.caption else MobileTheme.typography.label,
        fontWeight = FontWeight.SemiBold,
    )
    val timeStyle = TextStyle(
        color = Color.White.copy(alpha = 0.85f),
        fontSize = MobileTheme.typography.caption,
        fontWeight = FontWeight.Medium,
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 1.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .combinedClickable(onClick = onEdit, onLongClick = onLongPress),
    ) {
        val tight = maxHeight < 44.dp
        val stacked = maxHeight < 28.dp
        val title = event.title.ifEmpty { "—" }
        val timeText = "${formatTime(event.startMinute)} – ${formatTime(event.endMinute)}"

        when {
            stacked -> Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BasicText(
                    text = title,
                    style = titleStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            tight -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicText(
                    text = title,
                    style = titleStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                BasicText(
                    text = title,
                    style = titleStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                BasicText(text = timeText, style = timeStyle)
            }
        }
    }
}

/**
 * Small color swatch (16×16, 3 dp radius) used in legends and pickers.
 */
@Composable
fun ColorSwatch(color: Color, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 16.dp) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color),
    )
}

/** Convenience hook for managing the long-press menu open state next to an EventChip. */
@Composable
fun rememberMenuState(): MenuState = remember { MenuState() }

class MenuState {
    var open: Boolean by mutableStateOf(false)
        private set

    fun open() { open = true }
    fun close() { open = false }
}

