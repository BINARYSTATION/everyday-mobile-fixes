package io.github.binarystation.wishstar

import androidx.compose.foundation.shape.MorphPolygonShape
import androidx.compose.foundation.shape.PolygonShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.graphics.blur.BlurStop
import androidx.compose.ui.unit.dp

// The two Compose 1.13 APIs this demo is about. Both take the animation progress as a
// lambda and read it while drawing, so the screen does not recompose on every frame.

fun starShape(p: () -> Float) =
    MorphPolygonShape(
        start = PolygonShape.circle(),
        end = PolygonShape.star(
            numPoints = 5,
            innerRadiusRatio = 0.45f,
        ),
        progress = p,
    )

fun Modifier.skyBlur(p: () -> Float) =
    blur {
        val stops = skyStops(p())
        blurRadiusSpec = BlurRadiusSpec
            .verticalGradient(stops)
    }

/** Maps the plain [SkyFocus] numbers to Compose blur stops. */
private fun skyStops(p: Float): List<BlurStop> =
    SkyFocus.stops(p).map { BlurStop(it.fraction, it.radiusDp.dp) }
