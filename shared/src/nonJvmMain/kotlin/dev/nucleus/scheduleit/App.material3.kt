package dev.nucleus.scheduleit

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import dev.nucleus.scheduleit.di.AppGraph
import dev.nucleus.scheduleit.ui.material3.MaterialScheduleHost
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

@Composable
actual fun App(graph: AppGraph) {
    MaterialTheme {
        CompositionLocalProvider(LocalMetroViewModelFactory provides graph.viewModelFactory) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                MaterialScheduleHost()
            }
        }
    }
}
