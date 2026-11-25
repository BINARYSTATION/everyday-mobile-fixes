package io.github.binarystation.draftsaver

/**
 * Every text the autosave has stored, oldest first.
 *
 * Immutable, so the screen can keep it in a `State` and recompose when a save lands.
 * Plain Kotlin with no Android or Compose types, so it is unit tested on the JVM.
 */
data class SaveLog(val saves: List<String> = emptyList()) {

    val count: Int get() = saves.size

    val last: String? get() = saves.lastOrNull()

    /** A new log with [text] added at the end; this log is left unchanged. */
    fun record(text: String): SaveLog = copy(saves = saves + text)

    /** What the "Saved copy" card should show for the newest save. */
    val copyState: SavedCopyState
        get() = when {
            last == null -> SavedCopyState.NothingYet
            last.isNullOrBlank() -> SavedCopyState.Empty
            else -> SavedCopyState.Text
        }
}

enum class SavedCopyState {
    /** No save has happened yet. */
    NothingYet,

    /** The newest save stored a blank text. */
    Empty,

    /** The newest save stored real text. */
    Text,
}
