package io.github.binarystation.gallerygrid

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** The whole screen on the emulator: the totals are the platform's numbers, shown by the app. */
class GalleryScreenTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @Before
    fun drawTestPhotosFirst() {
        // With the photos already on disk, the screen goes straight to decoding at launch.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        TestPhotos.ensure(File(context.filesDir, "photos"))
    }

    @Test
    fun beforeShows576MegabytesAndAfterShows9() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForTotal("576 MB")
            compose.onNodeWithTag("bytes-0").assertTextEquals("48.0 MB")

            compose.onNodeWithTag("mode-switch").performClick()

            waitForTotal("9.0 MB")
            compose.onNodeWithTag("bytes-0").assertTextEquals("750 KB")
            compose.onNodeWithText("12 of 12 photos decoded").assertExists()
        }
    }

    private fun waitForTotal(text: String) {
        compose.waitUntil(timeoutMillis = 20_000) {
            compose.onAllNodes(hasTestTag("total") and hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
