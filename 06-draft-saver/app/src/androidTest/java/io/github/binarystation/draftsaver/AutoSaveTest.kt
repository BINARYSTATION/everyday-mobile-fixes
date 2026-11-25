package io.github.binarystation.draftsaver

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves the bug and the fix with the test clock paused (`mainClock.autoAdvance = false`), so
 * the 5 second save interval and the typing rhythm are exact, not wall-clock guesses.
 */
@RunWith(AndroidJUnit4::class)
class AutoSaveTest {

    @get:Rule
    val rule = createComposeRule()

    private val postcard = "Day 2 in Goa"

    @Test
    fun before_typeThenWaitTenSeconds_bothSavesStoreTheEmptyFirstText() {
        val saves = runScreen(fixed = false)

        Log.i(TAG, "BEFORE saves after 10 s = $saves")
        assertEquals(listOf("", ""), saves)
        rule.onNodeWithTag(DraftTags.SAVED_COPY).assertTextEquals("(empty)")
        rule.onNodeWithTag(DraftTags.SAVE_COUNT).assertTextEquals("Saves: 2")
    }

    @Test
    fun after_typeThenWaitTenSeconds_bothSavesStoreTheTypedText() {
        val saves = runScreen(fixed = true)

        Log.i(TAG, "AFTER saves after 10 s = $saves")
        assertEquals(listOf(postcard, postcard), saves)
        rule.onNodeWithTag(DraftTags.SAVED_COPY).assertTextEquals(postcard)
        rule.onNodeWithTag(DraftTags.SAVE_COUNT).assertTextEquals("Saves: 2")
    }

    /**
     * The other common answer is to key the effect on the text. That restarts the 5 second wait
     * on every key press, so while someone keeps typing nothing is saved at all.
     */
    @Test
    fun steadyTyping_oneKeyPerSecond_fixedKeepsSaving_keyedOnTextSavesNothing() {
        val fixedSaves = mutableListOf<String>()
        val keyedSaves = mutableListOf<String>()
        var text by mutableStateOf("")
        rule.setContent {
            AutoSaveFixed(text = text, onSave = { fixedSaves += it })
            AutoSaveKeyedOnText(text = text, onSave = { keyedSaves += it })
        }
        rule.mainClock.autoAdvance = false

        // Keys land at 0.5 s, 1.5 s, ... 11.5 s after the effects start: 12 keys, 12 characters,
        // never on a save tick, so each save sees an exact prefix.
        rule.mainClock.advanceTimeBy(500)
        postcard.forEach { char ->
            rule.runOnUiThread { text += char }
            rule.mainClock.advanceTimeBy(1_000)
        }
        Log.i(TAG, "after 12.5 s of typing: fixed = $fixedSaves, keyed = $keyedSaves")
        assertEquals(listOf("Day 2", "Day 2 in G"), fixedSaves)
        assertEquals(emptyList<String>(), keyedSaves)

        // Once the typing stops, the keyed version saves 5 s after the last key: it acts as a debounce.
        rule.mainClock.advanceTimeBy(SAVE_EVERY_MS)
        Log.i(TAG, "5 s after the last key: fixed = $fixedSaves, keyed = $keyedSaves")
        assertEquals(listOf("Day 2", "Day 2 in G", postcard), fixedSaves)
        assertEquals(listOf(postcard), keyedSaves)
    }

    /** Types the postcard into the real screen right away, then lets 10 s pass on the test clock. */
    private fun runScreen(fixed: Boolean): List<String> {
        val saves = mutableListOf<String>()
        rule.setContent {
            DraftTheme {
                DraftScreen(fixed = fixed, onFixedChange = {}, onSaved = { saves += it })
            }
        }
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag(DraftTags.EDITOR).performTextInput(postcard)
        rule.mainClock.advanceTimeBy(2 * SAVE_EVERY_MS + 100)
        rule.onNodeWithTag(DraftTags.EDITOR).assertTextContains(postcard)
        return saves
    }

    private companion object {
        const val TAG = "AutoSaveTest"
    }
}

/**
 * Shown for comparison only, not used by the app: the effect is keyed on [text], so every
 * change cancels the running loop and starts a new 5 second wait.
 */
@Composable
private fun AutoSaveKeyedOnText(
    text: String,
    onSave: (String) -> Unit,
) {
    LaunchedEffect(text) {
        while (true) {
            delay(SAVE_EVERY_MS)
            onSave(text)
        }
    }
}
