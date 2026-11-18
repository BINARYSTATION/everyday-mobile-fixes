package io.github.binarystation.gallerygrid

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/** Runs on a real device or emulator: the byte counts come from the platform decoder, not from arithmetic. */
class DecodeMemoryTest {

    private lateinit var photos: List<File>

    @Before
    fun drawTestPhotos() {
        val dir = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "test-photos")
        photos = TestPhotos.ensure(dir)
    }

    @Test
    fun theTestPhotosAreTwelveMegapixelFiles() {
        assertEquals(TestPhotos.COUNT, photos.size)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(photos[0].path, bounds)
        assertEquals(4000, bounds.outWidth)
        assertEquals(3000, bounds.outHeight)
    }

    @Test
    fun fullSizeDecodeCostsFortyEightMegabytes() {
        val bitmap = PhotoDecoder.decodeFullSize(photos[0].path)
        assertEquals(Bitmap.Config.ARGB_8888, bitmap.config)
        assertEquals(48_000_000, bitmap.allocationByteCount)
        bitmap.recycle()
    }

    @Test
    fun sampledDecodeCostsASixtyFourthOfThat() {
        val bitmap = PhotoDecoder.decodeSampled(photos[0].path, 300)
        assertEquals(500, bitmap.width)
        assertEquals(375, bitmap.height)
        assertEquals(750_000, bitmap.allocationByteCount)
        bitmap.recycle()
    }

    @Test
    fun theDecodedSizeMatchesTheArithmeticOnEveryPhoto() {
        val slot = 318
        photos.forEach { photo ->
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(photo.path, bounds)
            val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, slot, slot)
            val expected = decodedByteCount(bounds.outWidth, bounds.outHeight, sampleSize)

            val bitmap = PhotoDecoder.decodeSampled(photo.path, slot)
            assertNotNull(bitmap)
            assertEquals(photo.name, expected, bitmap.allocationByteCount.toLong())
            bitmap.recycle()
        }
    }

    @Test
    fun twelvePhotosAddUp() {
        // One at a time and recycled right away, so the test itself does not need 576 MB.
        var fullSizeTotal = 0L
        var sampledTotal = 0L
        photos.forEach { photo ->
            val full = PhotoDecoder.decodeFullSize(photo.path)
            fullSizeTotal += full.allocationByteCount
            full.recycle()
            val sampled = PhotoDecoder.decodeSampled(photo.path, 318)
            sampledTotal += sampled.allocationByteCount
            sampled.recycle()
        }
        assertEquals(576_000_000L, fullSizeTotal)
        assertEquals(9_000_000L, sampledTotal)
    }
}
