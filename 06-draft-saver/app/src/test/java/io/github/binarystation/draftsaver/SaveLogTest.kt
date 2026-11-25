package io.github.binarystation.draftsaver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SaveLogTest {

    @Test
    fun newLog_isEmpty_andShowsNothingYet() {
        val log = SaveLog()
        assertEquals(0, log.count)
        assertNull(log.last)
        assertEquals(SavedCopyState.NothingYet, log.copyState)
    }

    @Test
    fun record_appendsInOrder_andLeavesTheOldLogUnchanged() {
        val first = SaveLog().record("Day 2")
        val second = first.record("Day 2 in Goa")
        assertEquals(listOf("Day 2"), first.saves)
        assertEquals(listOf("Day 2", "Day 2 in Goa"), second.saves)
        assertEquals("Day 2 in Goa", second.last)
        assertEquals(2, second.count)
    }

    @Test
    fun blankSave_showsAsEmpty() {
        assertEquals(SavedCopyState.Empty, SaveLog().record("").copyState)
        assertEquals(SavedCopyState.Empty, SaveLog().record("   ").copyState)
    }

    @Test
    fun textSave_showsTheText() {
        assertEquals(SavedCopyState.Text, SaveLog().record("Day 2 in Goa").copyState)
    }

    @Test
    fun copyState_followsTheNewestSave_notTheFirst() {
        val log = SaveLog().record("Day 2 in Goa").record("")
        assertEquals(SavedCopyState.Empty, log.copyState)
        assertEquals(SavedCopyState.Text, log.record("Day 3").copyState)
    }
}
