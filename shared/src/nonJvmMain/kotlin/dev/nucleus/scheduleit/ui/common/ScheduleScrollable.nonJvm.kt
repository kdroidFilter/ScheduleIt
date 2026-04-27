package dev.nucleus.scheduleit.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ScheduleScrollableContainer(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val scroll = rememberScrollState()
    Box(modifier = modifier.verticalScroll(scroll)) {
        content()
    }
}
