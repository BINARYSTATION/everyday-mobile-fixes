package io.github.binarystation.wishstar

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.unit.dp

/** A drawn night sky: gradient, star field, crescent moon and two hills. No image files. */
@Composable
fun NightSky(modifier: Modifier = Modifier) {
    val dots = remember { skyDots(count = 110, seed = 8) }
    Canvas(modifier) {
        drawRect(
            Brush.verticalGradient(
                0f to WishColors.SkyTop,
                0.55f to WishColors.SkyMid,
                0.85f to WishColors.SkyGlow,
            )
        )
        dots.forEach { dot ->
            drawCircle(
                color = Color.White.copy(alpha = dot.alpha),
                radius = dot.radiusDp.dp.toPx(),
                center = Offset(dot.x * size.width, dot.y * size.height),
            )
        }

        val moonCenter = Offset(size.width * 0.74f, size.height * 0.22f)
        val moonRadius = 34.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                listOf(WishColors.Moon.copy(alpha = 0.22f), Color.Transparent),
                center = moonCenter,
                radius = moonRadius * 3.2f,
            ),
            radius = moonRadius * 3.2f,
            center = moonCenter,
        )
        val moon = Path().apply { addOval(Rect(moonCenter, moonRadius)) }
        val bite = Path().apply {
            addOval(Rect(moonCenter + Offset(moonRadius * 0.45f, -moonRadius * 0.3f), moonRadius * 0.85f))
        }
        drawPath(Path.combine(PathOperation.Difference, moon, bite), WishColors.Moon)

        drawPath(hill(size, left = 0.80f, dip = 0.70f, rise = 0.88f, right = 0.74f), WishColors.HillFar)
        drawPath(hill(size, left = 0.90f, dip = 0.80f, rise = 0.97f, right = 0.86f), WishColors.Night)
    }
}

/** A closed hill shape along the bottom; the four values are heights as fractions of the sky. */
private fun hill(size: Size, left: Float, dip: Float, rise: Float, right: Float): Path =
    Path().apply {
        val w = size.width
        val h = size.height
        moveTo(0f, h * left)
        cubicTo(w * 0.3f, h * dip, w * 0.65f, h * rise, w, h * right)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
