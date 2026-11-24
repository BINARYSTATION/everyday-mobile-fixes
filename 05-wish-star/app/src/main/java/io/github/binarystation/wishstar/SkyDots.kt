package io.github.binarystation.wishstar

import kotlin.random.Random

/** One background star, in fractions of the sky size so it fits any screen. */
data class SkyDot(val x: Float, val y: Float, val radiusDp: Float, val alpha: Float)

/**
 * Builds the same star field for the same [seed], so every run and every recording
 * shows the same sky. Dots stay in the upper [maxY] part, above the hills.
 */
fun skyDots(count: Int, seed: Int, maxY: Float = 0.7f): List<SkyDot> {
    require(count >= 0) { "count must be >= 0, was $count" }
    val random = Random(seed)
    return List(count) {
        SkyDot(
            x = random.nextFloat(),
            y = random.nextFloat() * maxY,
            radiusDp = 0.8f + random.nextFloat() * 1.6f,
            alpha = 0.35f + random.nextFloat() * 0.65f,
        )
    }
}
