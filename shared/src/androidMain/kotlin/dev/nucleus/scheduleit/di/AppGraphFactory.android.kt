package dev.nucleus.scheduleit.di

import androidx.activity.ComponentActivity
import dev.nucleus.scheduleit.data.DriverFactory
import dev.nucleus.scheduleit.data.drive.AndroidContextHolder
import dev.zacsweers.metro.createGraphFactory
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

fun createAndroidAppGraph(activity: ComponentActivity): AppGraph {
    AndroidContextHolder.applicationContext = activity.applicationContext
    // FileKit's openFileSaver / openFilePicker need an ActivityResultRegistry.
    FileKit.init(activity)
    return createGraphFactory<AppGraph.Factory>().create(DriverFactory(activity))
}
