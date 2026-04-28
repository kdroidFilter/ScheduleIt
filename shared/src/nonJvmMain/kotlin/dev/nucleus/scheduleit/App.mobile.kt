package dev.nucleus.scheduleit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.nucleus.scheduleit.di.AppGraph
import dev.nucleus.scheduleit.ui.mobile.screens.MobileScheduleHost
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

@Composable
actual fun App(graph: AppGraph) {
    MobileTheme {
        CompositionLocalProvider(LocalMetroViewModelFactory provides graph.viewModelFactory) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MobileTheme.colors.bg),
            ) {
                MobileScheduleHost()
            }
        }
    }
}
