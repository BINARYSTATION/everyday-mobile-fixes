package io.github.binarystation.noticeboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticesTest {

    @Test
    fun theBoardHasSixNotices() {
        assertEquals(6, Notices.all.size)
    }

    @Test
    fun everyNoticeHasItsOwnId() {
        assertEquals(Notices.all.size, Notices.all.map { it.id }.toSet().size)
    }

    @Test
    fun byIdFindsTheNotice() {
        assertEquals("Lift service at 2 pm", Notices.byId(2).title)
    }

    @Test(expected = NoSuchElementException::class)
    fun byIdFailsLoudlyForAnUnknownId() {
        Notices.byId(99)
    }

    @Test
    fun everyNoticeHasATitleAndABody() {
        Notices.all.forEach {
            assertTrue("title of ${it.id}", it.title.isNotBlank())
            assertTrue("body of ${it.id}", it.body.isNotBlank())
        }
    }

    @Test
    fun twoTapsOnTheSameRowMakeEqualKeys() {
        // This is why the bug is easy to miss: the back stack happily holds two equal keys.
        assertEquals(NoticeDetail(1), NoticeDetail(1))
        assertEquals(listOf(NoticeList, NoticeDetail(1), NoticeDetail(1)).size, 3)
    }
}
