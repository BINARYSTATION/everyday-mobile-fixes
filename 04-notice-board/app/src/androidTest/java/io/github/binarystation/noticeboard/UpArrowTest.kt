package io.github.binarystation.noticeboard

import android.content.Intent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The second face of the bug: a double tap on the Up arrow pops the back stack twice.
 * Without the guard that empties the stack and Navigation 3 throws "NavDisplay backstack cannot be empty"
 * (the BEFORE crash is shown with adb and logcat, see the README: it ends the process, so a test cannot assert it).
 */
class UpArrowTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun afterDoubleTapOnUpPopsOnlyOnce() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_GUARDED, true)
        ActivityScenario.launch<MainActivity>(intent).use {
            compose.waitForIdle()
            compose.mainClock.autoAdvance = false
            compose.onNodeWithTag("notice-row-1").performClick()
            compose.mainClock.advanceTimeBy(3_000)

            compose.onNodeWithTag("up-button").performClick()
            compose.mainClock.advanceTimeBy(100)
            compose.onNodeWithTag("up-button").performClick()
            compose.mainClock.advanceTimeBy(3_000)

            assertTrue(compose.onAllNodes(hasTestTag("mode-switch")).fetchSemanticsNodes().isNotEmpty())
            val chip = compose.onNodeWithTag("back-stack-chip").fetchSemanticsNode()
            val text = chip.config[SemanticsProperties.Text].first().text
            assertEquals("Back stack: 1", text)
        }
    }
}
