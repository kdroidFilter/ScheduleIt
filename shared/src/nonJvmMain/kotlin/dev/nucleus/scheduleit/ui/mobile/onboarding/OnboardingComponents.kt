package dev.nucleus.scheduleit.ui.mobile.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme

@Composable
internal fun OnboardingPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val alpha = if (enabled) 1f else 0.5f
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.accent.copy(alpha = colors.accent.alpha * alpha))
            .let { if (enabled) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = Color.White.copy(alpha = alpha),
                fontSize = typography.body,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
internal fun OnboardingGhostButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = colors.textSec,
                fontSize = typography.body,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
internal fun OnboardingProgress(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val active = index <= current
            val width by animateDpAsState(
                targetValue = if (active) 22.dp else 8.dp,
                animationSpec = tween(durationMillis = 220),
                label = "progress_dot_width",
            )
            val color by animateColorAsState(
                targetValue = if (active) colors.accent else colors.lineStrong,
                animationSpec = tween(durationMillis = 220),
                label = "progress_dot_color",
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
            )
        }
    }
}

private data class PreviewBlock(val column: Int, val topFraction: Float, val heightFraction: Float)

private val previewBlocks = listOf(
    PreviewBlock(0, 0.08f, 0.20f),
    PreviewBlock(0, 0.40f, 0.30f),
    PreviewBlock(1, 0.10f, 0.34f),
    PreviewBlock(1, 0.55f, 0.18f),
    PreviewBlock(2, 0.05f, 0.18f),
    PreviewBlock(2, 0.30f, 0.40f),
    PreviewBlock(3, 0.20f, 0.28f),
    PreviewBlock(3, 0.55f, 0.30f),
    PreviewBlock(4, 0.10f, 0.50f),
    PreviewBlock(4, 0.70f, 0.18f),
)

private val previewPalette = listOf(
    Color(0xFF42A5F5),
    Color(0xFFEF5350),
    Color(0xFF66BB6A),
    Color(0xFFFFCA28),
    Color(0xFFAB47BC),
)

/**
 * Visual flair for the Welcome screen: a stylized 5-day grid with colored
 * event blocks. Pure decoration — no interaction.
 */
@Composable
internal fun WeekPreview(modifier: Modifier = Modifier) {
    val colors = MobileTheme.colors
    val accent = colors.accent
    val lineColor = colors.line
    val tabBg = colors.bgAlt
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.line, RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Canvas(
            modifier = Modifier
                .width(260.dp)
                .height(150.dp),
        ) {
            val cols = 5
            val gap = size.width * 0.018f
            val colWidth = (size.width - gap * (cols - 1)) / cols
            val headerH = size.height * 0.16f
            for (i in 0 until cols) {
                val x = i * (colWidth + gap)
                drawRoundRect(
                    color = if (i == 1) accent else tabBg,
                    topLeft = Offset(x, 0f),
                    size = Size(colWidth, headerH),
                    cornerRadius = CornerRadius(6f, 6f),
                )
            }
            val gridTop = headerH + size.height * 0.06f
            val gridHeight = size.height - gridTop
            val rows = 5
            for (r in 0..rows) {
                val y = gridTop + (gridHeight / rows) * r
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
            }
            previewBlocks.forEachIndexed { index, block ->
                val x = block.column * (colWidth + gap)
                val y = gridTop + gridHeight * block.topFraction
                val h = gridHeight * block.heightFraction
                drawRoundRect(
                    color = previewPalette[index % previewPalette.size].copy(alpha = 0.88f),
                    topLeft = Offset(x + 2f, y),
                    size = Size(colWidth - 4f, h),
                    cornerRadius = CornerRadius(4f, 4f),
                )
            }
        }
    }
}
