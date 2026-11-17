package io.github.binarystation.grocerynotes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/** Holds the list and the BEFORE / AFTER switch. It survives screen rotation. */
class GroceryViewModel : ViewModel() {
    var rows by mutableStateOf(sampleGroceries())
        private set

    /** false = BEFORE (no key), true = AFTER (key = id). */
    var keyed by mutableStateOf(false)
        private set

    /** Changes on every restart, so the screen can throw away the notes typed so far. */
    var listId by mutableIntStateOf(0)
        private set

    fun delete(id: Int) {
        rows = rows.filterNot { it.id == id }
    }

    fun switchMode(useKey: Boolean) {
        keyed = useKey
        reset()
    }

    fun reset() {
        rows = sampleGroceries()
        listId += 1
    }
}
