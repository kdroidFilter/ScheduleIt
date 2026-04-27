package dev.nucleus.scheduleit.di

import android.content.Context
import dev.nucleus.scheduleit.data.DriverFactory
import dev.zacsweers.metro.createGraphFactory

fun createAndroidAppGraph(context: Context): AppGraph =
    createGraphFactory<AppGraph.Factory>().create(DriverFactory(context))
