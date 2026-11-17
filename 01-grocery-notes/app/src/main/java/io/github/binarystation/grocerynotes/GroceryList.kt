package io.github.binarystation.grocerynotes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/** Shows the list in the chosen mode. The two lists below are the only difference. */
@Composable
fun GroceryList(
    rows: List<GroceryItem>,
    keyed: Boolean,
    onDelete: (Int) -> Unit,
) {
    if (keyed) {
        KeyedList(rows, onDelete)
    } else {
        UnkeyedList(rows, onDelete)
    }
}

/** BEFORE: no key, so a row's remembered state is tied to its position. */
@Composable
private fun UnkeyedList(rows: List<GroceryItem>, onDelete: (Int) -> Unit) {
    LazyColumn {
        items(rows) { row ->
            GroceryRow(row, onDelete)
        }
    }
}

/** AFTER: key = id, so a row's remembered state follows its item. */
@Composable
private fun KeyedList(rows: List<GroceryItem>, onDelete: (Int) -> Unit) {
    LazyColumn {
        items(rows, key = { it.id }) { row ->
            GroceryRow(
                row = row,
                onDelete = onDelete,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun GroceryRow(
    row: GroceryItem,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The note lives inside the row. This is the state that has to stay with its item.
    var note by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { onDelete(row.id) },
                    modifier = Modifier.testTag("delete-${row.id}"),
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text(stringResource(R.string.note_placeholder)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note-${row.id}"),
            )
        }
    }
}
