package dev.nucleus.scheduleit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nucleus.scheduleit.di.AppGraph
import dev.nucleus.scheduleit.ui.jewel.JewelScheduleHost
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
@Suppress("UnusedParameter")
actual fun App(graph: AppGraph) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JewelTheme.globalColors.panelBackground),
    ) {
        JewelScheduleHost()
    }
}
