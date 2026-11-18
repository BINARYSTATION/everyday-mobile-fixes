package io.github.binarystation.gallerygrid

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** The app's palette. Background must stay equal to windowBackground in themes.xml. */
object GalleryColors {
    val Background = Color(0xFF10131A)
    val Surface = Color(0xFF1A1F2B)
    val Leaf = Color(0xFF7CD992)
    val Coral = Color(0xFFFF8F7A)
    val Cream = Color(0xFFE9EEF5)
    val Muted = Color(0xFF9AA6B8)
}

private val GalleryColorScheme = darkColorScheme(
    primary = GalleryColors.Leaf,
    onPrimary = GalleryColors.Background,
    background = GalleryColors.Background,
    onBackground = GalleryColors.Cream,
    surface = GalleryColors.Surface,
    onSurface = GalleryColors.Cream,
    surfaceVariant = GalleryColors.Surface,
    onSurfaceVariant = GalleryColors.Muted,
    outline = GalleryColors.Muted.copy(alpha = 0.5f),
)

@Composable
fun GalleryTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GalleryColorScheme, content = content)
}
