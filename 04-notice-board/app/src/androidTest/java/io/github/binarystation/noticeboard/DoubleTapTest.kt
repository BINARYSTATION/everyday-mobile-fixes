package io.github.binarystation.noticeboard

import android.content.Intent
import android.util.Log
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Two taps on the same row while the screen transition is still running.
 *
 * The test clock is paused (mainClock.autoAdvance = false), so nothing here depends on timing luck:
 * the gap between the two taps is exactly the number of milliseconds the test advances.
 * One click itself takes one 16 ms frame on the test clock.
 */
class DoubleTapTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    /** What one run measured. */
    private data class Result(val stackAfterTaps: Int, val backPresses: Int, val topTitle: String)

    private fun launch(guarded: Boolean): ActivityScenario<MainActivity> {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_GUARDED, guarded)
        return ActivityScenario.launch(intent)
    }

    private fun textOf(tag: String): String =
        compose.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.Text].first().text

    private fun stackSize(): Int = textOf("back-stack-chip").substringAfter(": ").toInt()

    private fun onListScreen(): Boolean =
        compose.onAllNodes(hasTestTag("mode-switch")).fetchSemanticsNodes().isNotEmpty()

    /** Lets every transition finish (the longest one is 700 ms). */
    private fun settle() = compose.mainClock.advanceTimeBy(3_000)

    /** Taps row 1 [taps] times, [gapMillis] apart, settles, then presses Back until the list shows. */
    private fun run(guarded: Boolean, taps: Int, gapMillis: Long): Result {
        launch(guarded).use { scenario ->
            compose.waitForIdle()
            compose.mainClock.autoAdvance = false
            repeat(taps) { index ->
                compose.onNodeWithTag("notice-row-1").performClick()
                if (index < taps - 1) compose.mainClock.advanceTimeBy(gapMillis)
            }
            settle()
            val stack = stackSize()
            val title = textOf("detail-title")
            var presses = 0
            while (!onListScreen() && presses < 5) {
                scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                presses++
                settle()
            }
            Log.i(TAG, "guarded=$guarded taps=$taps gap=${gapMillis}ms -> stack=$stack backPresses=$presses")
            return Result(stack, presses, title)
        }
    }

    @Test
    fun singleTapOpensTheNoticeOnceInBothModes() {
        assertEquals(Result(2, 1, WATER_TANK), run(guarded = false, taps = 1, gapMillis = 0))
        assertEquals(Result(2, 1, WATER_TANK), run(guarded = true, taps = 1, gapMillis = 0))
    }

    @Test
    fun beforeDoubleTapStacksTheSameNoticeTwice() {
        for (gap in DOUBLE_TAP_GAPS) {
            assertEquals("gap $gap ms", Result(3, 2, WATER_TANK), run(guarded = false, taps = 2, gapMillis = gap))
        }
    }

    @Test
    fun afterDoubleTapStacksTheNoticeOnce() {
        for (gap in DOUBLE_TAP_GAPS) {
            assertEquals("gap $gap ms", Result(2, 1, WATER_TANK), run(guarded = true, taps = 2, gapMillis = gap))
        }
    }

    /** The limit of the guard: two taps inside one frame both see a RESUMED screen, so both get through. */
    @Test
    fun twoTapsInTheSameFrameGetThroughInBothModes() {
        assertEquals(Result(3, 2, WATER_TANK), run(guarded = false, taps = 2, gapMillis = 0))
        assertEquals(Result(3, 2, WATER_TANK), run(guarded = true, taps = 2, gapMillis = 0))
    }

    private companion object {
        const val TAG = "NoticeBoardTest"
        const val WATER_TANK = "Water tank cleaning on Sunday"

        /** Gaps between the end of the first tap and the start of the second one, in test-clock milliseconds. */
        val DOUBLE_TAP_GAPS = listOf(16L, 50L, 100L, 150L, 200L, 300L, 500L, 650L)
    }
}
