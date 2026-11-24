package io.github.binarystation.wishstar

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WishStarTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun tap_sendsTheWish_andTapAgainTakesItBack() {
        rule.setContent { WishTheme { WishStarScreen() } }

        rule.onNodeWithText("Tap the circle to make a wish.").assertExists()
        rule.onNodeWithTag(WishTags.BUTTON).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Delivery: 3-5 business days.", substring = true).assertExists()

        rule.onNodeWithTag(WishTags.BUTTON).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Tap the circle to make a wish.").assertExists()
    }

    @Test
    fun oneTap_composesTheScreenOnce_whileTheMorphAndBlurAnimate() {
        var compositions = 0
        rule.setContent { WishTheme { WishStarScreen(onComposed = { compositions++ }) } }
        rule.waitForIdle()
        assertCenterColor(WishColors.Cream, notThis = WishColors.Gold)
        val beforeTap = compositions

        // Drive the clock by hand: 90 frames (1.5 s at 60 Hz) covers the whole spring.
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag(WishTags.BUTTON).performClick()
        repeat(90) { rule.mainClock.advanceTimeByFrame() }
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        val perTap = compositions - beforeTap
        Log.i("WishStarTest", "screen compositions for one tap = $perTap (90 frames advanced)")
        // One composition for the flipped boolean. The animation frames themselves only
        // re-run the draw lambdas (shape outline, blur, rotation, color).
        assertEquals(1, perTap)
        // ...and the draw lambdas did run: the shape is now filled with the star's gold.
        assertCenterColor(WishColors.Gold, notThis = WishColors.Cream)
    }

    /**
     * Checks that the center of the button is [expected] and not [notThis]. The press ripple
     * can still be fading over the shape for a moment (it once read #F8C255 instead of
     * #FFC857), so this compares the distance to both colors instead of an exact value.
     */
    private fun assertCenterColor(expected: Color, notThis: Color) {
        val bitmap = rule.onNodeWithTag(WishTags.BUTTON).captureToImage().asAndroidBitmap()
        val actual = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        val toExpected = distance(actual, expected.toArgb())
        val toOther = distance(actual, notThis.toArgb())
        assertTrue(
            "center pixel #%08X: distance %d to expected, %d to the other color"
                .format(actual, toExpected, toOther),
            toExpected * 4 < toOther,
        )
    }

    private fun distance(a: Int, b: Int): Int = listOf(16, 8, 0).sumOf { shift ->
        kotlin.math.abs((a shr shift and 0xFF) - (b shr shift and 0xFF))
    }
}
