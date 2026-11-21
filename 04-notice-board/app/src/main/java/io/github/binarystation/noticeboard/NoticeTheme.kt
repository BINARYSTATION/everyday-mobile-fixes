package io.github.binarystation.noticeboard

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** The app's palette. Background must stay equal to windowBackground in themes.xml. */
object NoticeColors {
    val Background = Color(0xFF16130F)
    val Surface = Color(0xFF241F18)
    val Pin = Color(0xFFFFC857)
    val Leaf = Color(0xFF7CD992)
    val Coral = Color(0xFFFF8F7A)
    val Paper = Color(0xFFF4EBD9)
    val Muted = Color(0xFFA89F8E)
}

private val NoticeColorScheme = darkColorScheme(
    primary = NoticeColors.Pin,
    onPrimary = NoticeColors.Background,
    background = NoticeColors.Background,
    onBackground = NoticeColors.Paper,
    surface = NoticeColors.Surface,
    onSurface = NoticeColors.Paper,
    surfaceVariant = NoticeColors.Surface,
    onSurfaceVariant = NoticeColors.Muted,
    outline = NoticeColors.Muted.copy(alpha = 0.5f),
)

@Composable
fun NoticeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NoticeColorScheme, content = content)
}
