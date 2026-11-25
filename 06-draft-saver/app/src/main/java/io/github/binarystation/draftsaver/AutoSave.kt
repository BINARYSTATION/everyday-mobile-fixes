package io.github.binarystation.draftsaver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay

/** How often the draft is saved while the screen is open. */
const val SAVE_EVERY_MS = 5_000L

/**
 * BEFORE: the bug, recreated on purpose.
 *
 * `LaunchedEffect(Unit)` starts its block once, on the first composition, and keeps that one
 * block running. The block captured [text] as it was at that moment: an empty string. Every
 * later composition passes the new text, but it only builds a new block that `LaunchedEffect`
 * ignores, because its key (`Unit`) never changes. So every save writes the first text.
 */
@Composable
fun AutoSaveBuggy(
    text: String,
    onSave: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        while (true) {
            delay(SAVE_EVERY_MS)
            onSave(text)
        }
    }
}

/**
 * AFTER: the fix.
 *
 * `rememberUpdatedState` keeps one `State` object for the life of this call site and writes the
 * newest value into it on every composition. The loop captures that `State`, not the value, so
 * each save reads the current text. The effect itself never restarts, so the 5 second rhythm
 * keeps going while the user types.
 *
 * [onSave] is wrapped the same way, as in the documentation's `LandingScreen` example, so a
 * caller that passes a new lambda is also picked up.
 */
@Composable
fun AutoSaveFixed(
    text: String,
    onSave: (String) -> Unit,
) {
    val latest by rememberUpdatedState(text)
    val save by rememberUpdatedState(onSave)
    LaunchedEffect(Unit) {
        while (true) {
            delay(SAVE_EVERY_MS)
            save(latest)
        }
    }
}
