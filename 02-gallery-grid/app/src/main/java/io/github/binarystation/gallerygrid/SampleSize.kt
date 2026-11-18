package io.github.binarystation.gallerygrid

/** Bytes per pixel of Bitmap.Config.ARGB_8888, the config BitmapFactory uses by default. */
const val BYTES_PER_PIXEL = 4

/**
 * The recipe from the Android docs: the largest power of two that still leaves both sides
 * of the decoded image at least as big as the requested size. Plain Kotlin, so it is unit tested.
 */
fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
    require(reqWidth > 0 && reqHeight > 0) { "The requested size must be positive" }
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/** What a decode of this size should cost in memory: every pixel of the sampled image, 4 bytes each. */
fun decodedByteCount(width: Int, height: Int, inSampleSize: Int): Long {
    val sampledWidth = (width + inSampleSize - 1) / inSampleSize
    val sampledHeight = (height + inSampleSize - 1) / inSampleSize
    return sampledWidth.toLong() * sampledHeight * BYTES_PER_PIXEL
}
