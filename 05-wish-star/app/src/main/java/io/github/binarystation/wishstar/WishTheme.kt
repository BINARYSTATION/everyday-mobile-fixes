package io.github.binarystation.wishstar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** The app's palette. Night must stay equal to windowBackground in themes.xml. */
object WishColors {
    val Night = Color(0xFF0A0E24)
    val SkyTop = Color(0xFF0D1236)
    val SkyMid = Color(0xFF2B1F5E)
    val SkyGlow = Color(0xFFD9735B)
    val HillFar = Color(0xFF1A1442)
    val Moon = Color(0xFFF7EBC8)
    val Cream = Color(0xFFF4EBD9)
    val Gold = Color(0xFFFFC857)
    val Muted = Color(0xFFA9A4CC)
}

private val WishColorScheme = darkColorScheme(
    primary = WishColors.Gold,
    onPrimary = WishColors.Night,
    background = WishColors.Night,
    onBackground = WishColors.Cream,
    surface = WishColors.Night,
    onSurface = WishColors.Cream,
    onSurfaceVariant = WishColors.Muted,
)

@Composable
fun WishTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = WishColorScheme, content = content)
}
