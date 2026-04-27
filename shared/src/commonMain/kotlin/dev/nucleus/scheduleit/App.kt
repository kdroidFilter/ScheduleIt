package dev.nucleus.scheduleit

import androidx.compose.runtime.Composable
import dev.nucleus.scheduleit.di.AppGraph

@Composable
expect fun App(graph: AppGraph)
