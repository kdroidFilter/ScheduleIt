package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.AppInfo
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_open_about
import scheduleit.shared.generated.resources.action_open_github
import scheduleit.shared.generated.resources.action_open_settings
import scheduleit.shared.generated.resources.app_name

/**
 * Top bar with logo + title and three trailing icon buttons:
 * GitHub, About, Settings. Mirrors the desktop title bar's actions
 * so phone, tablet and desktop expose the same secondary navigation.
 */
@Composable
fun MobileHeader(
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    title: String = stringResource(Res.string.app_name),
    logoSize: Dp = 22.dp,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 12.dp,
    showDivider: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconLogoMark(
                modifier = Modifier.size(logoSize),
                background = colors.accent,
                foreground = Color.White,
            )
            Spacer(Modifier.width(8.dp))
            BasicText(
                text = title,
                style = TextStyle(
                    color = colors.text,
                    fontSize = typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconBtn(
                onClick = { uriHandler.openUri(AppInfo.PROJECT_URL) },
                contentDescription = stringResource(Res.string.action_open_github),
            ) {
                IconGitHub(modifier = Modifier.size(18.dp), color = colors.text)
            }
            IconBtn(
                onClick = onOpenAbout,
                contentDescription = stringResource(Res.string.action_open_about),
            ) {
                IconInfo(modifier = Modifier.size(18.dp), color = colors.text)
            }
            IconBtn(
                onClick = onOpenSettings,
                contentDescription = stringResource(Res.string.action_open_settings),
            ) {
                IconSettings(modifier = Modifier.size(18.dp), color = colors.text)
            }
        }
    }
    if (showDivider) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))
    }
}
