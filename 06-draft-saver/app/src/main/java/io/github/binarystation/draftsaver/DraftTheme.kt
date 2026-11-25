package io.github.binarystation.draftsaver

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** The app's palette. Background must stay equal to windowBackground in themes.xml. */
object DraftColors {
    val Background = Color(0xFF16130F)
    val Surface = Color(0xFF241F18)
    val Gold = Color(0xFFFFC857)
    val Leaf = Color(0xFF7CD992)
    val Coral = Color(0xFFFF8F7A)
    val Paper = Color(0xFFF4EBD9)
    val Muted = Color(0xFFA89F8E)
}

private val DraftColorScheme = darkColorScheme(
    primary = DraftColors.Gold,
    onPrimary = DraftColors.Background,
    background = DraftColors.Background,
    onBackground = DraftColors.Paper,
    surface = DraftColors.Surface,
    onSurface = DraftColors.Paper,
    surfaceVariant = DraftColors.Surface,
    onSurfaceVariant = DraftColors.Muted,
    outline = DraftColors.Muted.copy(alpha = 0.5f),
)

@Composable
fun DraftTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DraftColorScheme, content = content)
}
