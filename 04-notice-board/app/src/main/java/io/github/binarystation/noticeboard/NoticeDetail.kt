package io.github.binarystation.noticeboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed

/** Screen 2: one notice, an Up button and the same back stack chip. */
@Composable
fun NoticeDetailScreen(
    notice: Notice,
    backStack: MutableList<Route>,
    guarded: Boolean,
) {
    val accent = if (guarded) NoticeColors.Leaf else NoticeColors.Coral
    // A plain Column, not a Material Surface or Scaffold: a Surface swallows touches as soon as it is
    // on screen, which would also hide the double tap (see the README, "Why a plain Column").
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoticeColors.Background)
            .safeDrawingPadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val onUp: () -> Unit = if (guarded) {
                dropUnlessResumed { backStack.removeLastOrNull() }
            } else {
                { backStack.removeLastOrNull() }
            }
            Text(
                text = "< " + stringResource(R.string.action_up),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                modifier = Modifier
                    .clickable(onClick = onUp)
                    .padding(vertical = 10.dp, horizontal = 4.dp)
                    .testTag("up-button"),
            )
            Spacer(Modifier.weight(1f))
            BackStackChip(stackSize = backStack.size, accent = accent)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NoticeColors.Paper, RoundedCornerShape(8.dp))
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.posted_by, notice.postedBy),
                style = MaterialTheme.typography.labelMedium,
                color = NoticeColors.Background.copy(alpha = 0.6f),
            )
            Text(
                text = notice.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NoticeColors.Background,
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 12.dp)
                    .testTag("detail-title"),
            )
            Text(
                text = notice.body,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                color = NoticeColors.Background,
            )
        }
    }
}
