package dev.nucleus.scheduleit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberDialogState
import io.github.kdroidfilter.nucleus.core.runtime.NucleusApp
import io.github.kdroidfilter.nucleus.window.jewel.JewelDecoratedDialog
import io.github.kdroidfilter.nucleus.window.jewel.JewelDialogTitleBar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.about_license
import scheduleit.shared.generated.resources.about_made_with_love
import scheduleit.shared.generated.resources.about_title
import scheduleit.shared.generated.resources.about_version
import scheduleit.shared.generated.resources.app_name

private const val LICENSE_NAME: String = "GPLv3"

@Composable
fun JewelAboutWindow(onCloseRequest: () -> Unit) {
    val dialogState = rememberDialogState(size = DpSize(380.dp, 240.dp))
    val title = stringResource(Res.string.about_title)
    JewelDecoratedDialog(
        onCloseRequest = onCloseRequest,
        state = dialogState,
        title = title,
    ) {
        JewelDialogTitleBar { _ -> Text(title) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(JewelTheme.globalColors.panelBackground)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(Res.string.app_name))
            NucleusApp.version?.let { version ->
                Text(stringResource(Res.string.about_version, version))
            }
            Text(stringResource(Res.string.about_license, LICENSE_NAME))
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.about_made_with_love))
        }
    }
}
