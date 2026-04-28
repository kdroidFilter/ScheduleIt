package dev.nucleus.scheduleit.updater

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import kotlin.math.roundToInt

/**
 * Floating pill anchored at the bottom-end of the screen, surfacing the
 * AndroidAppUpdater state. Hidden when there is nothing to act upon.
 */
@Composable
fun AppUpdaterOverlay(
    state: State<UpdateState>,
    onInstall: () -> Unit,
) {
    val current = state.value
    val visible = current is UpdateState.Downloading ||
        current is UpdateState.ReadyToInstall ||
        current is UpdateState.Installing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            UpdaterPill(state = current, onInstall = onInstall)
        }
    }
}

@Composable
private fun UpdaterPill(state: UpdateState, onInstall: () -> Unit) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography

    val clickable = state is UpdateState.ReadyToInstall
    val text = when (state) {
        is UpdateState.Downloading -> "Téléchargement ${state.percent.roundToInt()} %"
        is UpdateState.ReadyToInstall -> "Mettre à jour ${state.newVersion}"
        UpdateState.Installing -> "Installation…"
        else -> ""
    }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.accent)
            .then(if (clickable) Modifier.clickable(onClick = onInstall) else Modifier)
            .padding(PaddingValues(horizontal = 16.dp, vertical = 12.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        androidx.compose.foundation.Image(
            imageVector = Lucide.Download,
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.chipText),
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        BasicText(
            text = text,
            style = TextStyle(
                color = colors.chipText,
                fontSize = typography.body,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}
