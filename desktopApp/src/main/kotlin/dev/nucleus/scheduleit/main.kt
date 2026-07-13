@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package dev.nucleus.scheduleit

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import dev.nucleus.scheduleit.di.createDesktopAppGraph
import dev.nucleus.scheduleit.ui.JewelAboutWindow
import dev.nucleus.scheduleit.ui.jewel.JewelOnboardingWindow
import dev.nucleus.scheduleit.ui.jewel.LocalNucleusApplicationScope
import dev.nucleus.scheduleit.ui.jewel.ScheduleItTitleBar
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.core.runtime.Platform
import dev.nucleusframework.darkmodedetector.isSystemInDarkMode
import dev.nucleusframework.notification.windows.WindowsNotificationCenter
import dev.nucleusframework.scheduler.DesktopBootReceiver
import dev.nucleusframework.window.jewel.JewelDecoratedWindow
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
    // Scheduler invocations run headless (no Tao window) and need notifications up front.
    if (DesktopBootReceiver.isSchedulerInvocation(args)) {
        runCatching { WindowsNotificationCenter.initialize() }
        DesktopBootReceiver.handle(args = args, registry = ScheduleItTaskRegistry.registry)
        exitProcess(0)
    }

    nucleusApplication(args, backend = NucleusBackend.Tao) {
        // GraalVM init, single-instance locking and AutoLaunch/AUMID priming
        // are handled by the bootstrap.
        val nucleusScope = this
        val graph = remember { createDesktopAppGraph() }
        val notificationScope = remember {
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }
        val appUpdater = remember { AppUpdater() }
        val updateState by appUpdater.state.collectAsState()
        LaunchedEffect(notificationScope) {
            runCatching { notificationScope.startInAppNotificationLoop(graph.repository) }
            runCatching { notificationScope.startSchedulerSync(graph.repository) }
            runCatching { appUpdater.start(notificationScope) }
        }
        DisposableEffect(notificationScope) {
            onDispose { notificationScope.cancel() }
        }

        val theme = if (isSystemInDarkMode()) {
            JewelTheme.darkThemeDefinition()
        } else {
            JewelTheme.lightThemeDefinition()
        }

        // Linux DEB requires a graphical pkexec prompt → user must trigger install
        // explicitly via the title-bar icon. Windows/macOS install silently on quit.
        val updateOnLinux = Platform.Current == Platform.Linux
        val quit = {
            if (!updateOnLinux && updateState is UpdateState.ReadyToInstall) {
                appUpdater.installAndQuit()
            }
            notificationScope.cancel()
            exitApplication()
        }

        // Observe just the gating flag from the repository so the choice of
        // which window to open doesn't require a ViewModel scope yet.
        val onboardingCompleted by produceState<Boolean?>(null, graph) {
            graph.repository.observeSchedule().collect { snapshot ->
                value = snapshot.settings.onboardingCompleted
            }
        }

        IntUiTheme(theme = theme, styling = ComponentStyling.default()) {
            when (onboardingCompleted) {
                null -> Unit
                false -> JewelOnboardingWindow(viewModelFactory = graph.viewModelFactory)
                true -> JewelDecoratedWindow(
                    onCloseRequest = { quit() },
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
                    var showAbout by remember { mutableStateOf(false) }
                    CompositionLocalProvider(
                        LocalMetroViewModelFactory provides graph.viewModelFactory,
                        LocalNucleusApplicationScope provides nucleusScope,
                    ) {
                        ScheduleItMenuBar(onQuit = quit)
                        ScheduleItTitleBar(
                            onOpenGithub = {
                                runCatching {
                                    Desktop.getDesktop().browse(URI.create(PROJECT_URL))
                                }
                            },
                            onOpenAbout = { showAbout = true },
                            onInstallUpdate = if (updateOnLinux) {
                                (updateState as? UpdateState.ReadyToInstall)?.let {
                                    { appUpdater.installAndRestart() }
                                }
                            } else null,
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
}
