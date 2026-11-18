package io.github.binarystation.gallerygrid

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {
    private val viewModel: GalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // For scripted runs: adb shell am start -n <package>/.MainActivity --ez sampled true
        if (savedInstanceState == null && intent.getBooleanExtra(EXTRA_SAMPLED, false)) {
            viewModel.switchMode(sampled = true)
        }
        // Dark bars with light icons, drawn over the app's own background.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            GalleryTheme {
                GalleryScreen(viewModel)
            }
        }
    }

    private companion object {
        const val EXTRA_SAMPLED = "sampled"
    }
}
