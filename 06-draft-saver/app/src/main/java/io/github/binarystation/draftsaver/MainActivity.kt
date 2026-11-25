package io.github.binarystation.draftsaver

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // For scripted runs: adb shell am start -n <package>/.MainActivity --ez fixed true
        val startFixed = intent.getBooleanExtra(EXTRA_FIXED, false)
        // Dark bars with light icons, drawn over the app's own background.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            DraftTheme {
                DraftSaverApp(startFixed = startFixed)
            }
        }
    }

    companion object {
        /** Boolean extra: start in AFTER mode (the autosave reads the text through rememberUpdatedState). */
        const val EXTRA_FIXED = "fixed"
    }
}
