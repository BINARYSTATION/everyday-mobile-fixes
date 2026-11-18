package io.github.binarystation.gallerygrid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleSizeTest {

    @Test
    fun a12MegapixelPhotoCostsFortyEightMegabytes() {
        assertEquals(48_000_000L, decodedByteCount(4000, 3000, 1))
    }

    @Test
    fun slotOf300PixelsPicksSampleSize8() {
        assertEquals(8, calculateInSampleSize(4000, 3000, 300, 300))
    }

    @Test
    fun sampleSize8CostsASixtyFourthOfTheFullDecode() {
        val full = decodedByteCount(4000, 3000, 1)
        val sampled = decodedByteCount(4000, 3000, 8)
        assertEquals(750_000L, sampled)
        assertEquals(64L, full / sampled)
    }

    @Test
    fun twelvePhotosAddUpTo576And9Megabytes() {
        assertEquals(576_000_000L, 12 * decodedByteCount(4000, 3000, 1))
        assertEquals(9_000_000L, 12 * decodedByteCount(4000, 3000, 8))
    }

    @Test
    fun matchesTheExampleOnTheAndroidDocsPage() {
        // The page: 2048x1536 with inSampleSize 4 gives about 512x384, "0.75MB rather than 12MB".
        val mib = 1024.0 * 1024.0
        assertEquals(12.0, decodedByteCount(2048, 1536, 1) / mib, 0.0)
        assertEquals(0.75, decodedByteCount(2048, 1536, 4) / mib, 0.0)
    }

    @Test
    fun biggerSlotsGetSmallerSampleSizes() {
        assertEquals(8, calculateInSampleSize(4000, 3000, 318, 318))
        assertEquals(4, calculateInSampleSize(4000, 3000, 400, 400))
        assertEquals(2, calculateInSampleSize(4000, 3000, 800, 800))
        assertEquals(1, calculateInSampleSize(4000, 3000, 1600, 1600))
    }

    @Test
    fun anImageThatIsAlreadySmallEnoughIsNotShrunk() {
        assertEquals(1, calculateInSampleSize(300, 200, 300, 300))
        assertEquals(1, calculateInSampleSize(100, 100, 300, 300))
    }

    @Test
    fun theLongerSideDoesNotLetTheShorterSideGetTooSmall() {
        // 4000 / 8 = 500 is fine for a 400-wide slot, but 3000 / 8 = 375 is too short for a 400-tall slot.
        assertEquals(4, calculateInSampleSize(4000, 3000, 400, 400))
        // A tall and narrow slot is limited by its height.
        assertEquals(2, calculateInSampleSize(4000, 3000, 100, 1000))
    }

    @Test
    fun resultIsAlwaysAPowerOfTwoAndKeepsTheDecodedImageBigEnough() {
        for (width in listOf(640, 1024, 3000, 4000, 4032, 6000)) {
            for (height in listOf(480, 768, 2000, 3000, 3024, 4000)) {
                for (slot in listOf(64, 100, 160, 300, 318, 512, 1000)) {
                    val size = calculateInSampleSize(width, height, slot, slot)
                    assertTrue("$width x $height for $slot gave $size", size >= 1 && (size and (size - 1)) == 0)
                    // When one side is already smaller than the slot, nothing can be done: stay at 1.
                    if (width >= slot && height >= slot) {
                        assertTrue("$width x $height for $slot gave $size", width / size >= slot && height / size >= slot)
                    }
                }
            }
        }
    }

    @Test
    fun anOddSizeRoundsUpLikeTheDecoder() {
        // 4001 / 8 = 500.1, so the decoder needs 501 columns.
        assertEquals(501L * 375 * 4, decodedByteCount(4001, 3000, 8))
    }

    @Test
    fun aZeroSizedSlotIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { calculateInSampleSize(4000, 3000, 0, 0) }
    }
}
