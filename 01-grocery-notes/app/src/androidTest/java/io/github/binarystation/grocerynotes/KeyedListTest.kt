package io.github.binarystation.grocerynotes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

/**
 * Types a note on the Rice row (id 1), deletes Rice, and checks where the note ends up.
 * Rice is the first row and Oil (id 2) is the row right below it.
 */
class KeyedListTest {
    @get:Rule
    val rule = createComposeRule()

    private fun showList(keyed: Boolean) {
        rule.setContent {
            var rows by remember { mutableStateOf(sampleGroceries()) }
            GroceryList(
                rows = rows,
                keyed = keyed,
                onDelete = { id -> rows = rows.filterNot { it.id == id } },
            )
        }
    }

    private fun typeNoteOnRiceThenDeleteRice() {
        rule.onNodeWithTag("note-1").performTextInput("basmati")
        rule.onNodeWithTag("note-1").assertTextContains("basmati")
        rule.onNodeWithTag("delete-1").performClick()
        rule.waitForIdle()
    }

    @Test
    fun withoutKey_theNoteJumpsToTheNextRow() {
        showList(keyed = false)
        typeNoteOnRiceThenDeleteRice()

        // Rice is gone, but Oil now shows the note that was typed on Rice.
        rule.onNodeWithTag("note-2").assertTextContains("basmati")
    }

    @Test
    fun withKey_theNoteLeavesWithItsRow() {
        showList(keyed = true)
        typeNoteOnRiceThenDeleteRice()

        // The note went away with Rice, so no row shows it any more.
        rule.onAllNodes(hasText("basmati")).assertCountEquals(0)
    }
}
