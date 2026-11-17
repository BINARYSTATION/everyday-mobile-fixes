package io.github.binarystation.grocerynotes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** The app's palette. Background must stay equal to windowBackground in themes.xml. */
object GroceryColors {
    val Background = Color(0xFF0E1411)
    val Surface = Color(0xFF19221D)
    val Leaf = Color(0xFF7CD992)
    val Coral = Color(0xFFFF8F7A)
    val Cream = Color(0xFFE8F0EA)
    val Muted = Color(0xFF9DB0A3)
}

private val GroceryColorScheme = darkColorScheme(
    primary = GroceryColors.Leaf,
    onPrimary = GroceryColors.Background,
    background = GroceryColors.Background,
    onBackground = GroceryColors.Cream,
    surface = GroceryColors.Surface,
    onSurface = GroceryColors.Cream,
    surfaceVariant = GroceryColors.Surface,
    onSurfaceVariant = GroceryColors.Muted,
    outline = GroceryColors.Muted.copy(alpha = 0.5f),
)

@Composable
fun GroceryTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GroceryColorScheme, content = content)
}
