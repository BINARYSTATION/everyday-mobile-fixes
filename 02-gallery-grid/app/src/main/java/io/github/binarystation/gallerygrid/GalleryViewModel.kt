package io.github.binarystation.gallerygrid

import android.app.Application
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class DecodeMode { FULL_SIZE, SAMPLED }

/**
 * Draws the test photos on first launch, then decodes them one by one in the chosen mode.
 * Every decoded bitmap is kept, like a simple in-memory cache without a limit, so the sum
 * of their allocationByteCount is what the screen shows.
 *
 * The heavy work runs on Dispatchers.Default, but every state write happens on the main thread.
 * This view model can be created while the screen is composing, and state written from another
 * thread at that moment can be lost.
 */
class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    var mode by mutableStateOf(DecodeMode.FULL_SIZE)
        private set

    /** Empty until all test photos exist. */
    var photoFiles by mutableStateOf<List<File>>(emptyList())
        private set

    /** How many test photos are drawn so far (first launch only). */
    var drawnCount by mutableIntStateOf(0)
        private set

    /** Changes on every Reload, so the screen decodes the photos again. */
    var round by mutableIntStateOf(0)
        private set

    /** Photo index -> decoded bitmap. */
    val bitmaps = mutableStateMapOf<Int, Bitmap>()

    /** Photo indexes the system could not give memory for. */
    val failed = mutableStateListOf<Int>()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            val dir = File(application.filesDir, "photos")
            val files = withContext(Dispatchers.Default) {
                TestPhotos.ensure(dir) { done -> viewModelScope.launch { drawnCount = done } }
            }
            photoFiles = files
        }
    }

    val totalBytes: Long
        get() = bitmaps.values.sumOf { it.allocationByteCount.toLong() }

    fun switchMode(sampled: Boolean) {
        mode = if (sampled) DecodeMode.SAMPLED else DecodeMode.FULL_SIZE
    }

    fun reload() {
        round += 1
    }

    /** Drops the old bitmaps, then decodes every photo in the current mode, one after the other. */
    fun load(cellPx: Int) {
        loadJob?.cancel()
        bitmaps.clear()
        failed.clear()
        val files = photoFiles
        val sampled = mode == DecodeMode.SAMPLED
        loadJob = viewModelScope.launch {
            val startedAt = SystemClock.elapsedRealtime()
            files.forEachIndexed { index, file ->
                try {
                    val bitmap = withContext(Dispatchers.Default) {
                        if (sampled) {
                            PhotoDecoder.decodeSampled(file.path, cellPx)
                        } else {
                            PhotoDecoder.decodeFullSize(file.path)
                        }
                    }
                    bitmaps[index] = bitmap
                } catch (e: OutOfMemoryError) {
                    failed.add(index)
                }
            }
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            Log.i(TAG, "mode=$mode photos=${bitmaps.size} failed=${failed.size} bytes=$totalBytes millis=$elapsed")
        }
    }

    private companion object {
        const val TAG = "GalleryGrid"
    }
}
