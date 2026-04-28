package dev.nucleus.scheduleit.ui.mobile.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import dev.nucleus.scheduleit.data.drive.GoogleDriveStatus
import dev.nucleus.scheduleit.ui.mobile.components.LoadingIndicator
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.onboarding_drive_connect_cta
import scheduleit.shared.generated.resources.onboarding_drive_connecting
import scheduleit.shared.generated.resources.onboarding_drive_error
import scheduleit.shared.generated.resources.onboarding_drive_finish_later
import scheduleit.shared.generated.resources.onboarding_drive_privacy
import scheduleit.shared.generated.resources.onboarding_drive_subtitle
import scheduleit.shared.generated.resources.onboarding_drive_success
import scheduleit.shared.generated.resources.onboarding_drive_title

@Composable
internal fun DriveScreen(
    driveStatus: GoogleDriveStatus?,
    stepIndex: Int,
    totalSteps: Int,
    onConnect: () -> Unit,
    onSkip: () -> Unit,
    onConnected: () -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography

    AutoFinishOnConnected(status = driveStatus, onConnected = onConnected)

    OnboardingScaffold(
        stepIndex = stepIndex,
        totalSteps = totalSteps,
        primaryButton = {
            OnboardingPrimaryButton(
                label = stringResource(Res.string.onboarding_drive_connect_cta),
                onClick = onConnect,
                modifier = Modifier.fillMaxWidth(),
                enabled = driveStatus !is GoogleDriveStatus.Connecting,
            )
        },
        secondaryAction = {
            OnboardingGhostButton(
                label = stringResource(Res.string.onboarding_drive_finish_later),
                onClick = onSkip,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CloudBadge()
            Spacer(Modifier.height(20.dp))
            BasicText(
                text = stringResource(Res.string.onboarding_drive_title),
                style = TextStyle(
                    color = colors.text,
                    fontSize = typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(Modifier.height(10.dp))
            BasicText(
                text = stringResource(Res.string.onboarding_drive_subtitle),
                style = TextStyle(
                    color = colors.textSec,
                    fontSize = typography.body,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.padding(horizontal = 12.dp),
            )

            Spacer(Modifier.height(20.dp))
            DriveStatusBlock(status = driveStatus)

            Spacer(Modifier.height(16.dp))
            BasicText(
                text = stringResource(Res.string.onboarding_drive_privacy),
                style = TextStyle(
                    color = colors.textTer,
                    fontSize = typography.labelSmall,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

@Composable
private fun CloudBadge() {
    val colors = MobileTheme.colors
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(colors.accentSoft),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.accent),
        )
    }
}

@Composable
private fun DriveStatusBlock(status: GoogleDriveStatus?) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    when (status) {
        null, is GoogleDriveStatus.Disconnected -> Unit
        is GoogleDriveStatus.Connecting -> StatusRow(
            color = colors.textSec,
            content = {
                LoadingIndicator(color = colors.accent, diameter = 14.dp, strokeWidth = 2.dp)
                Spacer(Modifier.size(8.dp))
                BasicText(
                    text = stringResource(Res.string.onboarding_drive_connecting),
                    style = TextStyle(color = colors.textSec, fontSize = typography.bodySmall),
                )
            },
        )
        is GoogleDriveStatus.Connected -> StatusRow(
            color = colors.accent,
            content = {
                BasicText(
                    text = stringResource(Res.string.onboarding_drive_success),
                    style = TextStyle(
                        color = colors.text,
                        fontSize = typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    ),
                )
            },
        )
        is GoogleDriveStatus.Error -> StatusRow(
            color = colors.danger,
            content = {
                BasicText(
                    text = stringResource(Res.string.onboarding_drive_error, status.message),
                    style = TextStyle(
                        color = colors.danger,
                        fontSize = typography.bodySmall,
                        textAlign = TextAlign.Center,
                    ),
                )
            },
        )
    }
}

@Composable
private fun StatusRow(color: Color, content: @Composable () -> Unit) {
    val colors = MobileTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.line, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}
