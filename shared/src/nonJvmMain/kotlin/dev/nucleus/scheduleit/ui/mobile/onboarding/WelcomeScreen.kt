package dev.nucleus.scheduleit.ui.mobile.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.ui.mobile.components.IconLogoMark
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.onboarding_skip
import scheduleit.shared.generated.resources.onboarding_welcome_cta
import scheduleit.shared.generated.resources.onboarding_welcome_subtitle
import scheduleit.shared.generated.resources.onboarding_welcome_title

@Composable
internal fun WelcomeScreen(
    stepIndex: Int,
    totalSteps: Int,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    OnboardingScaffold(
        stepIndex = stepIndex,
        totalSteps = totalSteps,
        primaryButton = {
            OnboardingPrimaryButton(
                label = stringResource(Res.string.onboarding_welcome_cta),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        secondaryAction = {
            OnboardingGhostButton(
                label = stringResource(Res.string.onboarding_skip),
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
            WeekPreview()
            Spacer(Modifier.height(28.dp))
            IconLogoMark(modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(20.dp))
            BasicText(
                text = stringResource(Res.string.onboarding_welcome_title),
                style = TextStyle(
                    color = colors.text,
                    fontSize = typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(Modifier.height(10.dp))
            BasicText(
                text = stringResource(Res.string.onboarding_welcome_subtitle),
                style = TextStyle(
                    color = colors.textSec,
                    fontSize = typography.body,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}
