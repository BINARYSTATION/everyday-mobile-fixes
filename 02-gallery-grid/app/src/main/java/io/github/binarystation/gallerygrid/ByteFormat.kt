package io.github.binarystation.gallerygrid

import java.util.Locale
import kotlin.math.roundToLong

/** Decimal units on purpose: 1 MB = 1,000,000 bytes, so 4000 x 3000 x 4 bytes reads as 48.0 MB. */
fun formatBytes(bytes: Long): String = when {
    bytes >= 100_000_000L -> "${(bytes / 1_000_000.0).roundToLong()} MB"
    bytes >= 1_000_000L -> String.format(Locale.US, "%.1f MB", bytes / 1_000_000.0)
    bytes >= 1_000L -> "${(bytes / 1_000.0).roundToLong()} KB"
    else -> "$bytes B"
}
