package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composeunstyled.ToggleSwitch
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Neutral pill-shaped switch (38×22) — same look on Android and iOS.
 * Wraps compose-unstyled's ToggleSwitch to skip Material's default green styling.
 */
@Composable
fun MobileSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MobileTheme.colors
    ToggleSwitch(
        toggled = checked,
        onToggled = onCheckedChange,
        enabled = enabled,
        modifier = modifier.size(width = 38.dp, height = 22.dp),
        shape = RoundedCornerShape(11.dp),
        backgroundColor = if (checked) colors.switchOn else colors.switchOff,
        thumb = {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.White),
            )
        },
    )
}
