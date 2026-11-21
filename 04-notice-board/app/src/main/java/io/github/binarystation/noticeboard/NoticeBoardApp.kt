package io.github.binarystation.noticeboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

/** The two screens, as Navigation 3 keys. The back stack is a plain list of them. */
sealed interface Route

data object NoticeList : Route

data class NoticeDetail(val id: Int) : Route

/**
 * Owns the back stack and the BEFORE / AFTER flag.
 *
 * guarded = false: taps call the navigation code directly (BEFORE).
 * guarded = true: every tap is wrapped in dropUnlessResumed (AFTER).
 */
@Composable
fun NoticeBoardApp(startGuarded: Boolean = false) {
    val backStack = remember { mutableStateListOf<Route>(NoticeList) }
    var guarded by rememberSaveable { mutableStateOf(startGuarded) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<NoticeList> {
                NoticeListScreen(
                    backStack = backStack,
                    guarded = guarded,
                    onGuardedChange = { guarded = it },
                )
            }
            entry<NoticeDetail> { key ->
                NoticeDetailScreen(
                    notice = Notices.byId(key.id),
                    backStack = backStack,
                    guarded = guarded,
                )
            }
        },
    )
}
