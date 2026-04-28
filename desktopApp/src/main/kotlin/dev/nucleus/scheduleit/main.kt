@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package dev.nucleus.scheduleit

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.nucleus.scheduleit.di.createDesktopAppGraph
import dev.nucleus.scheduleit.ui.JewelAboutWindow
import dev.nucleus.scheduleit.ui.jewel.ScheduleItTitleBar
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import io.github.kdroidfilter.nucleus.aot.runtime.AotRuntime
import io.github.kdroidfilter.nucleus.core.runtime.SingleInstanceManager
import io.github.kdroidfilter.nucleus.darkmodedetector.isSystemInDarkMode
import io.github.kdroidfilter.nucleus.notification.windows.WindowsNotificationCenter
import io.github.kdroidfilter.nucleus.scheduler.DesktopBootReceiver
import io.github.kdroidfilter.nucleus.window.jewel.JewelDecoratedWindow
import io.github.kdroidfilter.nucleus.graalvm.GraalVmInitializer
import java.awt.Desktop
import java.awt.GraphicsEnvironment
import java.net.URI
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.ui.ComponentStyling

private const val AOT_TRAINING_DURATION_MS = 45_000L
private const val PROJECT_URL = "https://github.com/kdroidFilter/ScheduleIt"
private val DEFAULT_WINDOW_SIZE = DpSize(1280.dp, 820.dp)
private val MINIMUM_WINDOW_SIZE = DpSize(1100.dp, 720.dp)

private fun shouldStartMaximized(): Boolean {
    val bounds = runCatching {
        GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
    }.getOrNull() ?: return false
    return bounds.width < DEFAULT_WINDOW_SIZE.width.value ||
        bounds.height < DEFAULT_WINDOW_SIZE.height.value
}

fun main(args: Array<String>) {
    runCatching { GraalVmInitializer.initialize() }
    runCatching { WindowsNotificationCenter.initialize() }
    if (DesktopBootReceiver.isSchedulerInvocation(args)) {
        DesktopBootReceiver.handle(args = args, registry = ScheduleItTaskRegistry.registry)
        exitProcess(0)
    }
    if (AotRuntime.isTraining()) {
        Thread({
            Thread.sleep(AOT_TRAINING_DURATION_MS)
            exitProcess(0)
        }, "aot-training-timer").apply { isDaemon = false }.start()
    }

    application {
        var restoreRequested by remember { mutableStateOf(false) }

        val isSingle = remember {
            SingleInstanceManager.isSingleInstance(
                onRestoreFileCreated = {},
                onRestoreRequest = { restoreRequested = true },
            )
        }
        if (!isSingle) {
            exitApplication()
            return@application
        }

        val graph = remember { createDesktopAppGraph() }
        val notificationScope = remember {
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }
        LaunchedEffect(notificationScope) {
            runCatching { notificationScope.startInAppNotificationLoop(graph.repository) }
            runCatching { notificationScope.startSchedulerSync(graph.repository) }
        }
        DisposableEffect(notificationScope) {
            onDispose { notificationScope.cancel() }
        }

        val theme = if (isSystemInDarkMode()) {
            JewelTheme.darkThemeDefinition()
        } else {
            JewelTheme.lightThemeDefinition()
        }

        IntUiTheme(theme = theme, styling = ComponentStyling.default()) {
            JewelDecoratedWindow(
                onCloseRequest = {
                    notificationScope.cancel()
                    exitApplication()
                },
                state = rememberWindowState(
                    placement = if (shouldStartMaximized()) {
                        WindowPlacement.Maximized
                    } else {
                        WindowPlacement.Floating
                    },
                    position = WindowPosition.Aligned(Alignment.Center),
                    size = DEFAULT_WINDOW_SIZE,
                ),
                title = "ScheduleIt",
                minimumSize = MINIMUM_WINDOW_SIZE,
            ) {
                LaunchedEffect(restoreRequested) {
                    if (restoreRequested) {
                        window.toFront()
                        window.requestFocus()
                        restoreRequested = false
                    }
                }
                var showAbout by remember { mutableStateOf(false) }
                CompositionLocalProvider(LocalMetroViewModelFactory provides graph.viewModelFactory) {
                    ScheduleItMenuBar(
                        onQuit = {
                            notificationScope.cancel()
                            exitApplication()
                        },
                    )
                    ScheduleItTitleBar(
                        onOpenGithub = {
                            runCatching {
                                Desktop.getDesktop().browse(URI.create(PROJECT_URL))
                            }
                        },
                        onOpenAbout = { showAbout = true },
                    )
                    App(graph)
                    if (showAbout) {
                        JewelAboutWindow(onCloseRequest = { showAbout = false })
                    }
                }
            }
        }
    }
}
