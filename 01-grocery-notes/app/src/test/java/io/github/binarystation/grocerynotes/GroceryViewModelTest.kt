package io.github.binarystation.grocerynotes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroceryViewModelTest {

    @Test
    fun startsWithSixRowsInBeforeMode() {
        val model = GroceryViewModel()
        assertEquals(6, model.rows.size)
        assertFalse(model.keyed)
    }

    @Test
    fun rowIdsAreUnique() {
        val ids = sampleGroceries().map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun deleteRemovesOnlyThatRow() {
        val model = GroceryViewModel()
        model.delete(1)
        assertEquals(listOf("Oil", "Milk", "Eggs", "Tomatoes", "Bread"), model.rows.map { it.name })
    }

    @Test
    fun otherRowsKeepTheirIdsAfterADelete() {
        val model = GroceryViewModel()
        model.delete(1)
        assertEquals(listOf(2, 3, 4, 5, 6), model.rows.map { it.id })
    }

    @Test
    fun switchingModeRestartsTheList() {
        val model = GroceryViewModel()
        val firstListId = model.listId
        model.delete(1)
        model.switchMode(true)
        assertTrue(model.keyed)
        assertEquals(6, model.rows.size)
        assertNotEquals(firstListId, model.listId)
    }

    @Test
    fun resetBringsBackDeletedRows() {
        val model = GroceryViewModel()
        model.delete(1)
        model.delete(2)
        model.reset()
        assertEquals(sampleGroceries(), model.rows)
    }
}
