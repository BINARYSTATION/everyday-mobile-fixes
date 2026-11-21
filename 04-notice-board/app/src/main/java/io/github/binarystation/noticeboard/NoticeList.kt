package io.github.binarystation.noticeboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed

/** Screen 1: the six notices, the BEFORE / AFTER switch and the back stack chip. */
@Composable
fun NoticeListScreen(
    backStack: MutableList<Route>,
    guarded: Boolean,
    onGuardedChange: (Boolean) -> Unit,
) {
    val accent = if (guarded) NoticeColors.Leaf else NoticeColors.Coral
    // Surface also sets the default text color for everything inside it.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 16.dp),
        ) {
            Header(stackSize = backStack.size, accent = accent)
            ModeCard(guarded = guarded, accent = accent, onChange = onGuardedChange)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                items(Notices.all, key = { it.id }) { notice ->
                    val onClick: () -> Unit = if (guarded) {
                        dropUnlessResumed { backStack.add(NoticeDetail(notice.id)) }
                    } else {
                        { backStack.add(NoticeDetail(notice.id)) }
                    }
                    NoticeRow(notice = notice, accent = accent, onClick = onClick)
                }
            }
        }
    }
}

@Composable
private fun Header(stackSize: Int, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.society),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        BackStackChip(stackSize = stackSize, accent = accent)
    }
}

/** Shows which mode is on and lets you flip it. */
@Composable
private fun ModeCard(guarded: Boolean, accent: Color, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(if (guarded) R.string.mode_after else R.string.mode_before),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                text = stringResource(
                    if (guarded) R.string.mode_after_detail else R.string.mode_before_detail,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
        Switch(
            checked = guarded,
            onCheckedChange = onChange,
            modifier = Modifier.testTag("mode-switch"),
        )
    }
}

@Composable
private fun NoticeRow(notice: Notice, accent: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .testTag("notice-row-${notice.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(NoticeColors.Pin, CircleShape),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
        ) {
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.posted_by, notice.postedBy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = ">",
            style = MaterialTheme.typography.titleLarge,
            color = accent,
            fontFamily = FontFamily.Monospace,
        )
    }
}
