package io.github.binarystation.grocerynotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GroceryScreen(viewModel: GroceryViewModel = viewModel()) {
    // Surface also sets the default text color for everything inside it.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 16.dp),
        ) {
            Header(onReset = viewModel::reset)
            ModeCard(keyed = viewModel.keyed, onChange = viewModel::switchMode)
            // key(listId) only throws away the typed notes when the list restarts.
            key(viewModel.listId) {
                GroceryList(viewModel.rows, viewModel.keyed, viewModel::delete)
            }
        }
    }
}

@Composable
private fun Header(onReset: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onReset) {
            Text(stringResource(R.string.action_reset))
        }
    }
}

/** Shows which mode is on and lets you flip it. */
@Composable
private fun ModeCard(keyed: Boolean, onChange: (Boolean) -> Unit) {
    val accent = if (keyed) GroceryColors.Leaf else GroceryColors.Coral
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(if (keyed) R.string.mode_after else R.string.mode_before),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                text = stringResource(
                    if (keyed) R.string.mode_after_detail else R.string.mode_before_detail,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
        Switch(
            checked = keyed,
            onCheckedChange = onChange,
            modifier = Modifier.testTag("mode-switch"),
        )
    }
}
