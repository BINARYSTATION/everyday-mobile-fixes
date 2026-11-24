package io.github.binarystation.wishstar

/** One blur stop: where it sits on the sky (0 = top, 1 = bottom) and its radius in dp. */
data class BlurPoint(val fraction: Float, val radiusDp: Float)

/**
 * The "focus pull" behind the wish button, as plain numbers so it can be unit tested.
 *
 * At rest (progress 0) the sky is sharp and only its lower part blurs into the page.
 * After a wish (progress 1) the blur climbs up the whole sky, so the star is the one
 * sharp thing on screen.
 */
object SkyFocus {
    /** Where the blur ramp starts, as a fraction of the sky height. */
    const val REST_RAMP_START = 0.55f
    const val WISH_RAMP_START = 0.05f

    /** Blur radius above the ramp, in dp. */
    const val REST_TOP_DP = 0f
    const val WISH_TOP_DP = 8f

    /** Blur radius at the bottom edge, in dp. */
    const val REST_EDGE_DP = 16f
    const val WISH_EDGE_DP = 32f

    fun stops(progress: Float): List<BlurPoint> {
        // A spring can overshoot past 1 or below 0. Blur radii must never go negative.
        val p = progress.coerceIn(0f, 1f)
        val top = lerp(REST_TOP_DP, WISH_TOP_DP, p)
        return listOf(
            BlurPoint(fraction = 0f, radiusDp = top),
            BlurPoint(fraction = lerp(REST_RAMP_START, WISH_RAMP_START, p), radiusDp = top),
            BlurPoint(fraction = 1f, radiusDp = lerp(REST_EDGE_DP, WISH_EDGE_DP, p)),
        )
    }

    // This form returns exactly [start] at t = 0 and exactly [end] at t = 1.
    private fun lerp(start: Float, end: Float, t: Float): Float = start * (1f - t) + end * t
}
