package dev.nucleus.scheduleit.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ScheduleScrollableContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
