package io.github.binarystation.draftsaver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Test tags shared by the screen and the UI tests. */
object DraftTags {
    const val EDITOR = "editor"
    const val SAVED_COPY = "saved-copy"
    const val SAVE_COUNT = "save-count"
    const val MODE_SWITCH = "mode-switch"
}

/**
 * The app: one screen and the BEFORE / AFTER switch. Flipping the switch starts a fresh screen
 * (empty draft, empty log), so each mode starts the way a user would open it.
 */
@Composable
fun DraftSaverApp(startFixed: Boolean) {
    var fixed by rememberSaveable { mutableStateOf(startFixed) }
    key(fixed) {
        DraftScreen(fixed = fixed, onFixedChange = { fixed = it })
    }
}

/**
 * A postcard editor that autosaves every [SAVE_EVERY_MS] ms.
 *
 * @param fixed false runs [AutoSaveBuggy], true runs [AutoSaveFixed].
 * @param onSaved called with every saved text, after the screen has stored it. Empty in the
 *   app; the UI tests use it to read each save directly.
 */
@Composable
fun DraftScreen(
    fixed: Boolean,
    onFixedChange: (Boolean) -> Unit,
    onSaved: (String) -> Unit = {},
) {
    var draft by remember { mutableStateOf("") }
    var log by remember { mutableStateOf(SaveLog()) }
    // `log` is read when a save lands, not when this lambda is built, so it is never stale.
    val onSave: (String) -> Unit = { text ->
        log = log.record(text)
        onSaved(text)
    }

    if (fixed) {
        AutoSaveFixed(text = draft, onSave = onSave)
    } else {
        AutoSaveBuggy(text = draft, onSave = onSave)
    }

    val accent = if (fixed) DraftColors.Leaf else DraftColors.Coral
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
            Header()
            ModeCard(fixed = fixed, accent = accent, onChange = onFixedChange)
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text(stringResource(R.string.editor_label)) },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .testTag(DraftTags.EDITOR),
            )
            SavedCard(log = log, accent = accent)
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Shows which mode is on and lets you flip it. */
@Composable
private fun ModeCard(fixed: Boolean, accent: Color, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(if (fixed) R.string.mode_after else R.string.mode_before),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                text = stringResource(if (fixed) R.string.mode_after_detail else R.string.mode_before_detail),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
        Switch(
            checked = fixed,
            onCheckedChange = onChange,
            modifier = Modifier.testTag(DraftTags.MODE_SWITCH),
        )
    }
}

/** What the autosave stored last, and how many saves ran. */
@Composable
private fun SavedCard(log: SaveLog, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.saved_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.save_count, log.count),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(DraftTags.SAVE_COUNT),
            )
        }
        val copy = when (log.copyState) {
            SavedCopyState.NothingYet -> stringResource(R.string.saved_none)
            SavedCopyState.Empty -> stringResource(R.string.saved_empty)
            SavedCopyState.Text -> log.last.orEmpty()
        }
        Text(
            text = copy,
            style = MaterialTheme.typography.bodyLarge,
            color = if (log.copyState == SavedCopyState.Text) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier
                .padding(top = 6.dp)
                .testTag(DraftTags.SAVED_COPY),
        )
    }
}
