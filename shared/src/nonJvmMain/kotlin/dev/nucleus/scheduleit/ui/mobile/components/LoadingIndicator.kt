package dev.nucleus.scheduleit.ui.mobile.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Indeterminate circular progress indicator — animated 270° arc that
 * spins continuously. No Material3 dependency.
 */
@Composable
fun LoadingIndicator(
    color: Color,
    modifier: Modifier = Modifier,
    diameter: Dp = 36.dp,
    strokeWidth: Dp = 3.dp,
    sweepDegrees: Float = 270f,
    periodMillis: Int = 900,
) {
    val transition = rememberInfiniteTransition(label = "loading")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "angle",
    )
    Canvas(modifier = modifier.size(diameter)) {
        val s = strokeWidth.toPx()
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = sweepDegrees,
            useCenter = false,
            style = Stroke(width = s, cap = StrokeCap.Round),
            topLeft = Offset(s / 2f, s / 2f),
            size = Size(this.size.width - s, this.size.height - s),
        )
    }
}
