package dev.nucleus.scheduleit.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.nucleus.scheduleit.AppInfo
import dev.nucleus.scheduleit.ui.mobile.components.IconLogoMark
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.about_license
import scheduleit.shared.generated.resources.about_made_with_love
import scheduleit.shared.generated.resources.about_title
import scheduleit.shared.generated.resources.action_close
import scheduleit.shared.generated.resources.app_name

@Composable
fun MobileAboutDialog(onDismiss: () -> Unit) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.sheetBg)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BasicText(
                text = stringResource(Res.string.about_title),
                style = TextStyle(
                    color = colors.textTer,
                    fontSize = typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                ),
            )
            IconLogoMark(
                modifier = Modifier.size(56.dp),
                background = colors.accent,
                foreground = Color.White,
            )
            BasicText(
                text = stringResource(Res.string.app_name),
                style = TextStyle(
                    color = colors.text,
                    fontSize = typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
            BasicText(
                text = stringResource(Res.string.about_license, AppInfo.LICENSE_NAME),
                style = TextStyle(
                    color = colors.textSec,
                    fontSize = typography.bodySmall,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(Modifier.height(4.dp))
            BasicText(
                text = stringResource(Res.string.about_made_with_love),
                style = TextStyle(
                    color = colors.textSec,
                    fontSize = typography.bodySmall,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                BasicText(
                    text = stringResource(Res.string.action_close),
                    style = TextStyle(
                        color = colors.accent,
                        fontSize = typography.body,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}
