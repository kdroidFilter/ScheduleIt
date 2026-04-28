package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

/**
 * Bottom sheet with native-like slide-up entrance and slide-down exit.
 *
 * Built directly on Compose foundation — no Material3 chrome — so the look honours
 * [MobileTheme]. Animations: scrim fades, sheet slides from below the screen with
 * a spring (slightly bouncy) on enter and an ease-out tween on exit.
 *
 * Stays mounted across visibility changes so the exit animation can play before
 * the composable leaves composition.
 */
@Composable
fun ModalSheet(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    primaryLabel: String? = null,
    onPrimary: (() -> Unit)? = null,
    cancelLabel: String? = null,
    fillHeight: Float = 0.85f,
    content: @Composable () -> Unit,
) {
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = visible
    // Early return once the exit animation has fully played out.
    if (!transitionState.targetState && transitionState.isIdle) return

    val colors = MobileTheme.colors
    val typography = MobileTheme.typography

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxSheetHeight = maxHeight * fillHeight

        // Scrim (fades in/out, taps dismiss).
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(tween(durationMillis = 200)),
            exit = fadeOut(tween(durationMillis = 180)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.overlay)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDismiss() })
                    },
            )
        }

        // Sheet panel (slides up + fades).
        AnimatedVisibility(
            visibleState = transitionState,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                initialOffsetY = { fullHeight -> fullHeight },
            ) + fadeIn(tween(durationMillis = 140)),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 220),
                targetOffsetY = { fullHeight -> fullHeight },
            ) + fadeOut(tween(durationMillis = 180)),
        ) {
            Column(
                modifier = modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(colors.sheetBg)
                    .imePadding()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    // Swallow taps on the panel so they don't reach the scrim.
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {})
                    },
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BasicText(
                        text = title,
                        style = TextStyle(
                            color = colors.text,
                            fontSize = typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    IconBtn(onClick = onDismiss, contentDescription = "Close") {
                        IconClose(
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxSize(),
                            color = colors.textSec,
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                ) {
                    content()
                }

                // Footer
                if (primaryLabel != null && onPrimary != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.bgAlt)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (cancelLabel != null) {
                            SheetButton(
                                label = cancelLabel,
                                onClick = onDismiss,
                                primary = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        SheetButton(
                            label = primaryLabel,
                            onClick = onPrimary,
                            primary = true,
                            modifier = Modifier.weight(if (cancelLabel != null) 2f else 1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val bg = if (primary) colors.accent else Color.Transparent
    val fg = if (primary) Color.White else colors.text
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .let { mod ->
                if (primary) mod else mod.border(1.dp, colors.lineStrong, RoundedCornerShape(6.dp))
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = fg,
                fontSize = typography.body,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}
