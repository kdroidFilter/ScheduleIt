package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Github
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import scheduleit.shared.generated.resources.app_icon

/**
 * Lightweight stroke-based icons replicated from the design's inline SVG paths.
 * Each icon draws inside an 18×18 viewBox scaled to the current size.
 */
private const val VIEWPORT = 18f

@Composable
fun IconAdd(modifier: Modifier = Modifier, color: Color, strokeWidth: Float = 2f) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 14f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val half = 5f * s
        val stroke = Stroke(width = strokeWidth * s, cap = StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(cx, cy - half), androidx.compose.ui.geometry.Offset(cx, cy + half), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(cx - half, cy), androidx.compose.ui.geometry.Offset(cx + half, cy), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
fun IconSettings(modifier: Modifier = Modifier, color: Color) {
    LucideTinted(modifier = modifier, color = color, vector = Lucide.Settings)
}

@Composable
fun IconCalendar(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / VIEWPORT
        val w = 1.5f * s
        val path = Path().apply {
            moveTo(2f * s, 7f * s); lineTo(16f * s, 7f * s)
            moveTo(5f * s, 3f * s); lineTo(5f * s, 6f * s)
            moveTo(13f * s, 3f * s); lineTo(13f * s, 6f * s)
            moveTo(4f * s, 5f * s); lineTo(14f * s, 5f * s)
            // box
            moveTo(4f * s, 5f * s)
            lineTo(14f * s, 5f * s)
            lineTo(14f * s, 16f * s)
            lineTo(4f * s, 16f * s)
            close()
        }
        drawPath(path, color = color, style = Stroke(width = w, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconWeekGrid(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / VIEWPORT
        val w = 1.5f * s
        val path = Path().apply {
            // outer rect
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = 2f * s, top = 3f * s, right = 16f * s, bottom = 15f * s,
                    radiusX = 1.5f * s, radiusY = 1.5f * s,
                ),
            )
            // inner verticals
            moveTo(6f * s, 3f * s); lineTo(6f * s, 15f * s)
            moveTo(10f * s, 3f * s); lineTo(10f * s, 15f * s)
            moveTo(14f * s, 3f * s); lineTo(14f * s, 15f * s)
            // header divider
            moveTo(2f * s, 7f * s); lineTo(16f * s, 7f * s)
        }
        drawPath(path, color = color, style = Stroke(width = w))
    }
}

@Composable
fun IconChevronLeft(modifier: Modifier = Modifier, color: Color, strokeWidth: Float = 2f) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 14f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val w = strokeWidth * s
        val path = Path().apply {
            moveTo(cx + 3f * s, cy - 6f * s)
            lineTo(cx - 3f * s, cy)
            lineTo(cx + 3f * s, cy + 6f * s)
        }
        drawPath(path, color = color, style = Stroke(width = w, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconClose(modifier: Modifier = Modifier, color: Color, strokeWidth: Float = 1.6f) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 12f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val half = 4f * s
        val w = strokeWidth * s
        drawLine(color, androidx.compose.ui.geometry.Offset(cx - half, cy - half), androidx.compose.ui.geometry.Offset(cx + half, cy + half), strokeWidth = w, cap = StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(cx + half, cy - half), androidx.compose.ui.geometry.Offset(cx - half, cy + half), strokeWidth = w, cap = StrokeCap.Round)
    }
}

@Composable
fun IconGitHub(modifier: Modifier = Modifier, color: Color) {
    LucideTinted(modifier = modifier, color = color, vector = Lucide.Github)
}

@Composable
fun IconInfo(modifier: Modifier = Modifier, color: Color) {
    LucideTinted(modifier = modifier, color = color, vector = Lucide.Info)
}

@Composable
fun IconDownload(modifier: Modifier = Modifier, color: Color) {
    LucideTinted(modifier = modifier, color = color, vector = Lucide.Download)
}

@Composable
private fun LucideTinted(
    modifier: Modifier,
    color: Color,
    vector: androidx.compose.ui.graphics.vector.ImageVector,
) {
    androidx.compose.foundation.Image(
        imageVector = vector,
        contentDescription = null,
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(color),
        modifier = modifier,
    )
}

/**
 * Renders the real app icon (`drawable/app_icon.png`) at the requested size.
 * The legacy `background`/`foreground` parameters are ignored — kept to avoid
 * touching every call site, which previously needed the placeholder colours.
 */
@Composable
fun IconLogoMark(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") background: Color = Color.Unspecified,
    @Suppress("UNUSED_PARAMETER") foreground: Color = Color.Unspecified,
) {
    androidx.compose.foundation.Image(
        painter = org.jetbrains.compose.resources.painterResource(
            scheduleit.shared.generated.resources.Res.drawable.app_icon,
        ),
        contentDescription = null,
        modifier = modifier,
    )
}
