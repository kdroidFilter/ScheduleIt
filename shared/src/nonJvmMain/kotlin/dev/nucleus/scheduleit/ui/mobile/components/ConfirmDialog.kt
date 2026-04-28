package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Centered confirmation dialog — same chrome as ModalSheet but anchored to the centre.
 * Used for destructive actions (e.g. reset data).
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    danger: Boolean = false,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.sheetBg)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BasicText(
                text = title,
                style = TextStyle(
                    color = colors.text,
                    fontSize = typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                ),
            )
            BasicText(
                text = message,
                style = TextStyle(
                    color = colors.textSec,
                    fontSize = typography.bodySmall,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                DialogTextButton(label = cancelLabel, onClick = onDismiss)
                DialogTextButton(
                    label = confirmLabel,
                    onClick = onConfirm,
                    color = if (danger) colors.danger else colors.accent,
                )
            }
        }
    }
}

@Composable
private fun DialogTextButton(
    label: String,
    onClick: () -> Unit,
    color: Color = MobileTheme.colors.accent,
) {
    val typography = MobileTheme.typography
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = color,
                fontSize = typography.body,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

