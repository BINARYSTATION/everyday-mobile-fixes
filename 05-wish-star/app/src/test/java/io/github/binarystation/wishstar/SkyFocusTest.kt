package io.github.binarystation.wishstar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyFocusTest {

    @Test
    fun atRest_topIsSharp_andOnlyTheLowerSkyBlurs() {
        val stops = SkyFocus.stops(0f)
        assertEquals(BlurPoint(0f, 0f), stops[0])
        assertEquals(BlurPoint(SkyFocus.REST_RAMP_START, 0f), stops[1])
        assertEquals(BlurPoint(1f, SkyFocus.REST_EDGE_DP), stops[2])
    }

    @Test
    fun afterAWish_theBlurClimbsUpAndGetsStronger() {
        val stops = SkyFocus.stops(1f)
        assertEquals(BlurPoint(0f, SkyFocus.WISH_TOP_DP), stops[0])
        assertEquals(BlurPoint(SkyFocus.WISH_RAMP_START, SkyFocus.WISH_TOP_DP), stops[1])
        assertEquals(BlurPoint(1f, SkyFocus.WISH_EDGE_DP), stops[2])
    }

    @Test
    fun halfway_isTheMidpoint() {
        val stops = SkyFocus.stops(0.5f)
        assertEquals(0.30f, stops[1].fraction, 1e-6f)
        assertEquals(4f, stops[0].radiusDp, 1e-6f)
        assertEquals(24f, stops[2].radiusDp, 1e-6f)
    }

    @Test
    fun springOvershoot_isClamped() {
        assertEquals(SkyFocus.stops(1f), SkyFocus.stops(1.2f))
        assertEquals(SkyFocus.stops(0f), SkyFocus.stops(-0.15f))
    }

    @Test
    fun everyProgress_givesValidSortedStops() {
        // Compose rejects a BlurStop with a fraction outside 0..1 or a negative radius.
        for (step in -20..120) {
            val stops = SkyFocus.stops(step / 100f)
            assertTrue(stops.all { it.fraction in 0f..1f && it.radiusDp >= 0f })
            assertTrue(stops.zipWithNext().all { (a, b) -> a.fraction <= b.fraction })
            assertTrue(stops.zipWithNext().all { (a, b) -> a.radiusDp <= b.radiusDp })
        }
    }
}
