package io.github.binarystation.gallerygrid

import org.junit.Assert.assertEquals
import org.junit.Test

class ByteFormatTest {

    @Test
    fun tilesReadInMegabytesAndKilobytes() {
        assertEquals("48.0 MB", formatBytes(48_000_000L))
        assertEquals("750 KB", formatBytes(750_000L))
    }

    @Test
    fun totalsReadAsWholeMegabytesFromOneHundred() {
        assertEquals("576 MB", formatBytes(576_000_000L))
        assertEquals("9.0 MB", formatBytes(9_000_000L))
    }

    @Test
    fun smallValues() {
        assertEquals("0 B", formatBytes(0L))
        assertEquals("999 B", formatBytes(999L))
        assertEquals("1 KB", formatBytes(1_000L))
    }

    @Test
    fun roundsInsteadOfCuttingOff() {
        assertEquals("576 MB", formatBytes(575_999_999L))
    }
}
