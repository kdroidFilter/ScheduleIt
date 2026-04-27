@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package dev.nucleus.scheduleit

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.nucleus.scheduleit.di.createDesktopAppGraph
import dev.nucleus.scheduleit.ui.jewel.ScheduleItTitleBar
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import io.github.kdroidfilter.nucleus.darkmodedetector.isSystemInDarkMode
import io.github.kdroidfilter.nucleus.scheduler.DesktopBootReceiver
import io.github.kdroidfilter.nucleus.window.jewel.JewelDecoratedWindow
import io.github.kdroidfilter.nucleus.graalvm.GraalVmInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.ui.ComponentStyling

fun main(args: Array<String>) {
    GraalVmInitializer.initialize()
    if (DesktopBootReceiver.isSchedulerInvocation(args)) {
        DesktopBootReceiver.handle(args = args, registry = ScheduleItTaskRegistry.registry)
        return
    }

    val graph = createDesktopAppGraph()

    val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    notificationScope.startInAppNotificationLoop(graph.repository)
    notificationScope.startSchedulerSync(graph.repository)

    application {
        val theme = if (isSystemInDarkMode()) {
            JewelTheme.darkThemeDefinition()
        } else {
            JewelTheme.lightThemeDefinition()
        }

        IntUiTheme(theme = theme, styling = ComponentStyling.default()) {
            JewelDecoratedWindow(
                onCloseRequest = ::exitApplication,
                state = rememberWindowState(
                    position = WindowPosition.Aligned(Alignment.Center),
                    size = DpSize(1280.dp, 820.dp),
                ),
                title = "ScheduleIt",
                minimumSize = DpSize(1100.dp, 720.dp),
            ) {
                CompositionLocalProvider(LocalMetroViewModelFactory provides graph.viewModelFactory) {
                    ScheduleItTitleBar()
                    App(graph)
                }
            }
        }
    }
}
