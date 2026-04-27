package dev.nucleus.scheduleit.di

import dev.nucleus.scheduleit.data.DriverFactory
import dev.zacsweers.metro.createGraphFactory

fun createDesktopAppGraph(): AppGraph =
    createGraphFactory<AppGraph.Factory>().create(DriverFactory())
