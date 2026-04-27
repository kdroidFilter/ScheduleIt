package dev.nucleus.scheduleit.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.styling.default
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.component.styling.ScrollbarStyle
import org.jetbrains.jewel.ui.component.styling.ScrollbarVisibility
import org.jetbrains.jewel.ui.theme.scrollbarStyle

@Composable
actual fun ScheduleScrollableContainer(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val baseStyle = JewelTheme.scrollbarStyle
    val style = ScrollbarStyle(
        colors = baseStyle.colors,
        metrics = baseStyle.metrics,
        trackClickBehavior = baseStyle.trackClickBehavior,
        scrollbarVisibility = ScrollbarVisibility.AlwaysVisible.default(),
    )
    VerticallyScrollableContainer(modifier = modifier, style = style) {
        content()
    }
}
