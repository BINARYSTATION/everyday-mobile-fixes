package io.github.binarystation.noticeboard

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** The number this demo is about: how many screens sit in the back stack right now. */
@Composable
fun BackStackChip(stackSize: Int, accent: Color) {
    Text(
        text = stringResource(R.string.back_stack, stackSize),
        style = MaterialTheme.typography.titleMedium,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = accent,
        modifier = Modifier
            .border(1.5.dp, accent, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("back-stack-chip"),
    )
}
