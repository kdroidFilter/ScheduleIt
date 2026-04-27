package dev.nucleus.scheduleit

import androidx.compose.ui.window.ComposeUIViewController
import dev.nucleus.scheduleit.data.DriverFactory
import dev.nucleus.scheduleit.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

private val appGraph: AppGraph by lazy {
    createGraphFactory<AppGraph.Factory>().create(DriverFactory())
}

fun MainViewController() = ComposeUIViewController { App(appGraph) }

