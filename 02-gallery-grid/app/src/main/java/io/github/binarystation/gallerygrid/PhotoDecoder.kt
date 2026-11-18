package io.github.binarystation.gallerygrid

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/** Two ways to load the same file into a thumbnail slot. */
object PhotoDecoder {

    /** BEFORE: decodes every pixel of the file, however small the slot is. */
    fun decodeFullSize(path: String): Bitmap {
        val bmp = BitmapFactory.decodeFile(path)
        return checkNotNull(bmp) { "Could not decode $path" }
    }

    /** AFTER: reads the file size first, then decodes a smaller copy that still fills the slot. */
    fun decodeSampled(path: String, cellPx: Int): Bitmap {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true // read the size only, no pixels
        BitmapFactory.decodeFile(path, o)
        o.inSampleSize = sampleSize(o, cellPx)
        o.inJustDecodeBounds = false
        val bmp = BitmapFactory.decodeFile(path, o)
        return checkNotNull(bmp) { "Could not decode $path" }
    }

    private fun sampleSize(o: BitmapFactory.Options, cellPx: Int): Int =
        calculateInSampleSize(o.outWidth, o.outHeight, cellPx, cellPx)
}
