package io.github.binarystation.noticeboard

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // For scripted runs: adb shell am start -n <package>/.MainActivity --ez guarded true
        val startGuarded = intent.getBooleanExtra(EXTRA_GUARDED, false)
        // Dark bars with light icons, drawn over the app's own background.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            NoticeTheme {
                NoticeBoardApp(startGuarded = startGuarded)
            }
        }
    }

    companion object {
        /** Boolean extra: start in AFTER mode (taps wrapped in dropUnlessResumed). */
        const val EXTRA_GUARDED = "guarded"
    }
}
