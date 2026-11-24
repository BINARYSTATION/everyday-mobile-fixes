package io.github.binarystation.wishstar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyDotsTest {

    @Test
    fun sameSeed_drawsTheSameSky() {
        assertEquals(skyDots(count = 50, seed = 8), skyDots(count = 50, seed = 8))
        assertNotEquals(skyDots(count = 50, seed = 8), skyDots(count = 50, seed = 9))
    }

    @Test
    fun dots_stayAboveTheHills_andInsideTheSky() {
        val dots = skyDots(count = 500, seed = 8, maxY = 0.7f)
        assertEquals(500, dots.size)
        assertTrue(dots.all { it.x in 0f..1f && it.y in 0f..0.7f })
        assertTrue(dots.all { it.alpha in 0f..1f && it.radiusDp > 0f })
    }
}
