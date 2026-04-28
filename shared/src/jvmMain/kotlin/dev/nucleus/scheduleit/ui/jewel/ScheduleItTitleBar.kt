package dev.nucleus.scheduleit.ui.jewel

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.kdroidfilter.nucleus.core.runtime.Platform
import io.github.kdroidfilter.nucleus.window.DecoratedWindowScope
import io.github.kdroidfilter.nucleus.window.jewel.JewelTitleBar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.painter.hints.Size
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_open_about
import scheduleit.shared.generated.resources.action_open_github
import scheduleit.shared.generated.resources.action_open_settings
import scheduleit.shared.generated.resources.app_name

@Composable
@androidx.compose.foundation.ExperimentalFoundationApi
fun DecoratedWindowScope.ScheduleItTitleBar(
    onOpenGithub: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val viewModel: ScheduleViewModel = metroViewModel()
    JewelTitleBar(Modifier) {
        Text(stringResource(Res.string.app_name))

        // Place the action buttons opposite the window control buttons:
        // End on macOS (controls on the left), Start on Windows/Linux (controls on the right).
        val actionsAlignment = if (Platform.Current == Platform.MacOS) Alignment.End else Alignment.Start
        Row(Modifier.align(actionsAlignment)) {
            val githubTooltip = stringResource(Res.string.action_open_github)
            Tooltip({ Text(githubTooltip) }) {
                IconButton(
                    onClick = onOpenGithub,
                    modifier = Modifier.size(40.dp).padding(5.dp),
                ) {
                    Icon(
                        key = AllIconsKeys.Vcs.Vendors.Github,
                        contentDescription = githubTooltip,
                        hints = arrayOf(Size(20)),
                    )
                }
            }

            val aboutTooltip = stringResource(Res.string.action_open_about)
            Tooltip({ Text(aboutTooltip) }) {
                IconButton(
                    onClick = onOpenAbout,
                    modifier = Modifier.size(40.dp).padding(5.dp),
                ) {
                    Icon(
                        key = AllIconsKeys.General.ShowInfos,
                        contentDescription = aboutTooltip,
                        hints = arrayOf(Size(20)),
                    )
                }
            }

            val settingsTooltip = stringResource(Res.string.action_open_settings)
            Tooltip({ Text(settingsTooltip) }) {
                IconButton(
                    onClick = { viewModel.onEvent(ScheduleIntent.OpenSettings) },
                    modifier = Modifier.size(40.dp).padding(5.dp),
                ) {
                    Icon(
                        key = AllIconsKeys.General.Gear,
                        contentDescription = settingsTooltip,
                        hints = arrayOf(Size(20)),
                    )
                }
            }
        }
    }
}
